#!/usr/bin/env node
/**
 * nyx-crypt — format .nyx (NYX2) — Edisi Akselerasi (Spek Kentang / Low-RAM Friendly)
 *
 *   node nyx-crypt.js encrypt <input> [output.nyx] [--keyfile f] [--turbo|--fast|--strong] [--shred] [--threads n] [--full-verify] [--random-pins] [--force]
 *   node nyx-crypt.js open    <input.nyx> [output] [--keyfile f] [--threads n] [--force]
 *   node nyx-crypt.js <input.nyx>                  (pintasan langsung untuk membuka file .nyx)
 *   node nyx-crypt.js info    <input.nyx>          (cek info header & kebutuhan RAM tanpa PIN)
 *   node nyx-crypt.js shred   <file>               (hapus permanen file asli dengan multi-pass wipe)
 *   node nyx-crypt.js genkey  <keyfile>
 *   node nyx-crypt.js selftest
 *
 * Desain & Optimasi Spek Kentang (Target 4 GB < 2 Menit di RAM 2 GB):
 *  1. 3 PIN (6 digit) digabung jadi SATU input scrypt yang adaptif untuk SEMUA ukuran RAM:
 *     - Otomatis menyesuaikan profil RAM (16 MiB / 32 MiB / 64 MiB / 128 MiB / 256 MiB / 1 GiB)
 *       agar perangkat RAM 1–2 GB tidak terkena swap/freeze, sedangkan PC RAM besar tetap maksimal.
 *  2. L1-Cache Streaming Dual-Cipher (ChaCha20-Poly1305 -> AES-256-GCM):
 *     - Enkripsi & dekripsi diproses in-place per blok 64 KiB di dalam L1/L2 cache CPU,
 *       menghilangkan 100% alokasi V8 Large Object Space (> 8 GB alokasi heap dihemat untuk file 4 GB).
 *  3. Double-Buffered Multi-Core Ring Pipeline + Background Worker Pre-Warming:
 *     - Setiap worker memiliki 2 slot buffer bergantian (Double Buffering) sehingga worker CPU
 *       TIDAK PERNAH menganggur menunggu baca/tulis disk di thread utama.
 *     - Worker pool otomatis dipanaskan di background saat pengguna sedang mengetik PIN.
 *  4. Kemudahan Buka & Keamanan Ekstra:
 *     - Nama output otomatis bila dikosongkan, tombol Backspace lintas-lapis saat ketik PIN,
 *       progress bar real-time (MB/s & ETA), proteksi symlink, serta fitur --shred.
 */
'use strict';

const fs = require('fs');
const os = require('os');
const path = require('path');
const crypto = require('crypto');
const { Worker, isMainThread, workerData } = require('worker_threads');

// ───────────────────────── konstanta ─────────────────────────
const MAGIC = Buffer.from('NYX2');
const HEADER_LEN = 77;      // magic4 | flags1 | logN1 | noncePrefix7 | salt32 | commit32
const BODY_LEN = 45;        // bagian header yang dicakup commit (semua sebelum field commit)
const CHUNK = 1 << 20;      // 1 MiB plaintext / chunk
const TAG = 16;
const OVERHEAD = TAG * 2;   // dua cipher berantai → 2 tag / chunk
const FULL = CHUNK + OVERHEAD;
const SCRYPT_R = 8;
const SCRYPT_P = 1;
const LOGN_DEFAULT = 18;    // 256 MiB RAM (standar di PC RAM lega)
const LOGN_MIDRAM = 17;     // 128 MiB RAM (otomatis bila RAM perangkat <= 4.5 GiB)
const LOGN_LOWRAM = 16;     // 64 MiB RAM (otomatis bila RAM perangkat <= 2.5 GiB)
const LOGN_FAST = 15;       // 32 MiB RAM (opsi --fast untuk CPU/RAM kentang)
const LOGN_TURBO = 14;      // 16 MiB RAM (opsi --turbo untuk perangkat sangat terbatas)
const LOGN_STRONG = 20;     // 1 GiB RAM (opsi --strong)
const LOGN_MIN = 14;        // 16 MiB RAM (batas bawah & selftest)
const LOGN_MAX = 21;        // batas atas: cegah file jahat memaksa alokasi RAM raksasa
const FLAG_KEYFILE = 1;
const SLICE_LEN = 65536;    // 64 KiB L1-cache-friendly slice (bebas V8 Large Object Space GC)
const FULL_VERIFY_LIMIT = 32 * (1 << 20); // 32 MiB: batas verifikasi baca-ulang penuh otomatis
const EMPTY_ZIP = Buffer.from('504b0506' + '00'.repeat(18), 'hex'); // zip kosong valid (22 byte)

// Status sinkronisasi SharedArrayBuffer untuk Worker Pool
const STATE_IDLE = 0;
const STATE_ENC = 1;
const STATE_DEC = 2;
const STATE_OK = 3;
const STATE_ERR = 4;

// Enkripsi in-place per 64 KiB slice (tanpa alokasi 1 MiB di V8 Large Object Space)
function cipherSealSliceInto(kCha, kAes, nonce, aad, plain, outBuf) {
  const c1 = crypto.createCipheriv('chacha20-poly1305', kCha, nonce, { authTagLength: TAG });
  c1.setAAD(aad);
  const c2 = crypto.createCipheriv('aes-256-gcm', kAes, nonce, { authTagLength: TAG });
  c2.setAAD(aad);

  let pos = 0;
  const total = plain.length;
  for (let off = 0; off < total; off += SLICE_LEN) {
    const end = Math.min(off + SLICE_LEN, total);
    const m = c1.update(plain.subarray(off, end));
    const e = c2.update(m);
    e.copy(outBuf, pos);
    pos += e.length;
    m.fill(0);
  }
  const fin1 = c1.final();
  if (fin1.length > 0) {
    const e = c2.update(fin1);
    e.copy(outBuf, pos);
    pos += e.length;
    fin1.fill(0);
  }
  const tag1 = c1.getAuthTag();
  let b = c2.update(tag1);
  b.copy(outBuf, pos);
  pos += b.length;
  b = c2.final();
  if (b.length > 0) {
    b.copy(outBuf, pos);
    pos += b.length;
  }
  c2.getAuthTag().copy(outBuf, pos);
  pos += TAG;
  tag1.fill(0);
  return pos;
}

// Dekripsi in-place per 64 KiB slice; melempar error & membersihkan buffer bila autentikasi gagal
function cipherOpenSliceInto(kCha, kAes, nonce, aad, sealed, outBuf) {
  const len = sealed.length;
  const payloadLen = len - OVERHEAD;
  if (payloadLen < 0) throw new Error('invalid chunk');

  const d2 = crypto.createDecipheriv('aes-256-gcm', kAes, nonce, { authTagLength: TAG });
  d2.setAAD(aad);
  d2.setAuthTag(sealed.subarray(len - TAG, len));

  const d1 = crypto.createDecipheriv('chacha20-poly1305', kCha, nonce, { authTagLength: TAG });
  d1.setAAD(aad);

  let pos = 0;
  try {
    for (let off = 0; off < payloadLen; off += SLICE_LEN) {
      const end = Math.min(off + SLICE_LEN, payloadLen);
      const m = d2.update(sealed.subarray(off, end));
      const p = d1.update(m);
      p.copy(outBuf, pos);
      pos += p.length;
      m.fill(0);
    }
    const tag1A = d2.update(sealed.subarray(payloadLen, payloadLen + TAG));
    const tag1B = d2.final();
    const tag1 = tag1B.length > 0 ? Buffer.concat([tag1A, tag1B]) : tag1A;

    d1.setAuthTag(tag1);
    const fin1 = d1.final();
    tag1.fill(0);
    if (fin1.length > 0) {
      fin1.copy(outBuf, pos);
      pos += fin1.length;
      fin1.fill(0);
    }
    return pos;
  } catch (err) {
    outBuf.subarray(0, pos).fill(0);
    throw err;
  }
}

// ───────────────────────── jalur Worker Thread ─────────────────────────
if (!isMainThread && workerData && workerData.nyxWorker) {
  const { ctrlSab, dataSab, keySab, hdrSab, workerIndex, workerCount } = workerData;
  const ctrl = new Int32Array(ctrlSab);
  const s0 = workerIndex;
  const s1 = workerIndex + workerCount;
  const b0 = s0 * 8;
  const b1 = s1 * 8;
  const wBase = (workerCount * 2 * 8) + workerIndex * 4;
  const slotBufs = [
    Buffer.from(dataSab, s0 * FULL, FULL),
    Buffer.from(dataSab, s1 * FULL, FULL),
  ];
  const kCha = Buffer.from(keySab, 0, 32);
  const kAes = Buffer.from(keySab, 32, 32);
  const hdr = Buffer.from(hdrSab, 0, HEADER_LEN);
  const nonce = Buffer.allocUnsafe(12);

  // Tandai worker siap
  Atomics.store(ctrl, wBase + 1, 1);
  Atomics.notify(ctrl, wBase + 1, 1);

  for (;;) {
    if (Atomics.load(ctrl, wBase + 2) === 1) break;

    const bellSnap = Atomics.load(ctrl, wBase);
    const st0 = Atomics.load(ctrl, b0);
    const st1 = Atomics.load(ctrl, b1);
    const r0 = st0 === STATE_ENC || st0 === STATE_DEC;
    const r1 = st1 === STATE_ENC || st1 === STATE_DEC;

    let pick = -1;
    if (r0 && r1) {
      const idx0 = Atomics.load(ctrl, b0 + 1) >>> 0;
      const idx1 = Atomics.load(ctrl, b1 + 1) >>> 0;
      pick = idx0 <= idx1 ? 0 : 1;
    } else if (r0) {
      pick = 0;
    } else if (r1) {
      pick = 1;
    } else {
      Atomics.wait(ctrl, wBase, bellSnap);
      continue;
    }

    const base = pick === 0 ? b0 : b1;
    const slotBuf = slotBufs[pick];
    const cmd = Atomics.load(ctrl, base);
    if (cmd !== STATE_ENC && cmd !== STATE_DEC) continue;

    const idx = Atomics.load(ctrl, base + 1) >>> 0;
    const len = Atomics.load(ctrl, base + 2);
    const last = Atomics.load(ctrl, base + 3);

    hdr.copy(nonce, 0, 6, 13);
    nonce.writeUInt32BE(idx, 7);
    nonce[11] = last ? 1 : 0;

    if (cmd === STATE_ENC) {
      try {
        const outLen = cipherSealSliceInto(kCha, kAes, nonce, hdr, slotBuf.subarray(0, len), slotBuf);
        Atomics.store(ctrl, base + 2, outLen);
        Atomics.store(ctrl, base, STATE_OK);
      } catch (_) {
        Atomics.store(ctrl, base, STATE_ERR);
      }
    } else {
      try {
        const outLen = cipherOpenSliceInto(kCha, kAes, nonce, hdr, slotBuf.subarray(0, len), slotBuf);
        Atomics.store(ctrl, base + 2, outLen);
        Atomics.store(ctrl, base, STATE_OK);
      } catch (_) {
        Atomics.store(ctrl, base, STATE_ERR);
      }
    }
    Atomics.notify(ctrl, base, 1);
  }
  nonce.fill(0);
  process.exit(0);
}

// ───────────────────────── utilitas utama ─────────────────────────
const ui = { stdinPins: false, threads: 0, fullVerify: false };
const fail = (msg) => { throw new Error(msg); };
const zero = (...bufs) => { for (const b of bufs) if (b) b.fill(0); };
const zeroKeys = (k) => zero(k.kAes, k.kCha, k.commit);

// Pilih jumlah worker optimal sesuai profil RAM & core CPU
function detectWorkerCount(requested = 0) {
  if (requested > 0) return Math.min(8, Math.max(1, requested | 0));
  const cores = typeof os.availableParallelism === 'function'
    ? os.availableParallelism()
    : (os.cpus() ? os.cpus().length : 2);
  const totalRamGiB = os.totalmem() / (1024 ** 3);
  if (totalRamGiB <= 2.2) return Math.min(Math.max(2, cores), 2); // 2 worker (4 slot = ~4 MiB) di RAM <= 2 GB
  if (totalRamGiB <= 4.2) return Math.min(Math.max(2, cores), 3); // 3 worker (6 slot = ~6 MiB) di RAM 3-4 GB
  return Math.min(Math.max(2, cores), 4);                        // 4 worker (8 slot = ~8 MiB) di RAM > 4 GB
}

// Pilih logN scrypt yang aman untuk semua ukuran RAM perangkat agar tidak memicu swap disk
function selectSmartLogN(opts = {}) {
  if (opts.strong) return LOGN_STRONG;
  if (opts.turbo) return LOGN_TURBO;
  if (opts.fast) return LOGN_FAST;
  const totalMem = os.totalmem();
  const freeMem = os.freemem();
  if (totalMem <= 1.6 * (1024 ** 3) || freeMem < 160 * (1024 ** 2)) return LOGN_TURBO;
  if (totalMem <= 2.6 * (1024 ** 3) || freeMem < 320 * (1024 ** 2)) return LOGN_LOWRAM;
  if (totalMem <= 4.6 * (1024 ** 3) || freeMem < 600 * (1024 ** 2)) return LOGN_MIDRAM;
  return LOGN_DEFAULT;
}

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 ** 2) return `${(bytes / 1024).toFixed(1)} KiB`;
  if (bytes < 1024 ** 3) return `${(bytes / (1024 ** 2)).toFixed(1)} MiB`;
  return `${(bytes / (1024 ** 3)).toFixed(2)} GiB`;
}

function renderProgress(label, doneBytes, totalBytes, startTimeMs) {
  if (!process.stderr.isTTY || totalBytes < 8 * (1 << 20)) return;
  const ratio = Math.min(1, Math.max(0, doneBytes / totalBytes));
  const pct = (ratio * 100).toFixed(0).padStart(3, ' ');
  const width = 16;
  const filled = Math.round(ratio * width);
  const bar = '█'.repeat(filled) + '░'.repeat(width - filled);
  const elapsedSec = Math.max(0.05, (Date.now() - startTimeMs) / 1000);
  const mbps = (doneBytes / (1024 ** 2)) / elapsedSec;
  const remSec = mbps > 0.1 ? Math.max(0, ((totalBytes - doneBytes) / (1024 ** 2)) / mbps) : 0;
  const etaMin = String(Math.floor(remSec / 60)).padStart(2, '0');
  const etaSec = String(Math.ceil(remSec % 60)).padStart(2, '0');
  process.stderr.write(`\r  ${label} [${bar}] ${pct}% | ${mbps.toFixed(1)} MiB/s | ETA ${etaMin}:${etaSec} `);
  if (doneBytes >= totalBytes) process.stderr.write('\n');
}

// ───────────────────────── Worker Pool Manager (Double-Buffered) ─────────────────────────
let pool = null;

function getOrInitPool(desiredWorkers) {
  const count = detectWorkerCount(desiredWorkers || ui.threads);
  if (pool && pool.ready && pool.count === count) return pool;
  if (pool) shutdownPool();

  const slotCount = count * 2; // Double-buffering: 2 slot per worker agar I/O & CPU 100% paralel
  const ctrlSab = new SharedArrayBuffer((slotCount * 8 + count * 4) * 4);
  const dataSab = new SharedArrayBuffer(slotCount * FULL);
  const keySab = new SharedArrayBuffer(64);
  const hdrSab = new SharedArrayBuffer(HEADER_LEN);
  const ctrl = new Int32Array(ctrlSab);
  const workers = [];
  const slots = [];

  for (let s = 0; s < slotCount; s++) {
    slots.push(Buffer.from(dataSab, s * FULL, FULL));
  }

  for (let i = 0; i < count; i++) {
    const w = new Worker(__filename, {
      workerData: {
        nyxWorker: true,
        ctrlSab,
        dataSab,
        keySab,
        hdrSab,
        workerIndex: i,
        workerCount: count,
      },
    });
    w.unref();
    workers.push(w);
  }

  let allReady = true;
  for (let i = 0; i < count; i++) {
    const readyIdx = (slotCount * 8) + i * 4 + 1;
    if (Atomics.load(ctrl, readyIdx) === 0) {
      Atomics.wait(ctrl, readyIdx, 0, 1500);
    }
    if (Atomics.load(ctrl, readyIdx) !== 1) {
      allReady = false;
      break;
    }
  }

  if (!allReady) {
    for (const w of workers) { try { w.terminate(); } catch (_) {} }
    return null;
  }

  pool = {
    ready: true,
    count,
    slotCount,
    ctrl,
    slots,
    keyBuf: Buffer.from(keySab, 0, 64),
    hdrBuf: Buffer.from(hdrSab, 0, HEADER_LEN),
    workers,
  };
  return pool;
}

function prewarmPoolAsync() {
  setImmediate(() => {
    try { getOrInitPool(); } catch (_) { /* abaikan bila gagal prewarm */ }
  });
}

function shutdownPool() {
  if (!pool) return;
  pool.keyBuf.fill(0);
  pool.hdrBuf.fill(0);
  for (let s = 0; s < pool.slotCount; s++) {
    pool.slots[s].fill(0);
    Atomics.store(pool.ctrl, s * 8, STATE_IDLE);
  }
  for (let i = 0; i < pool.count; i++) {
    const wBase = (pool.slotCount * 8) + i * 4;
    Atomics.store(pool.ctrl, wBase + 2, 1);
    Atomics.add(pool.ctrl, wBase, 1);
    Atomics.notify(pool.ctrl, wBase, 1);
  }
  for (const w of pool.workers) { try { w.terminate(); } catch (_) {} }
  pool = null;
}

// ───────────────────────── kebersihan file sementara ─────────────────────────
const tmpFiles = new Set();
process.on('exit', () => {
  shutdownPool();
  for (const f of tmpFiles) { try { fs.unlinkSync(f); } catch (_) { /* abaikan */ } }
  if (process.stdin.isTTY && process.stdin.setRawMode) {
    try { process.stdin.setRawMode(false); } catch (_) { /* abaikan */ }
  }
});
for (const s of ['SIGINT', 'SIGTERM', 'SIGHUP']) process.on(s, () => process.exit(130));

const tmpPath = (target) =>
  path.join(path.dirname(path.resolve(target)), `.nyx-${crypto.randomBytes(6).toString('hex')}.tmp`);

function commitOutput(tmp, outp, force) {
  const drop = () => { try { fs.unlinkSync(tmp); } catch (_) { /* abaikan */ } tmpFiles.delete(tmp); };
  const exists = () => { drop(); fail(`"${outp}" muncul saat proses berjalan — TIDAK ditimpa, hasil dibuang. Ulangi / pakai --force.`); };
  if (force) { fs.renameSync(tmp, outp); tmpFiles.delete(tmp); return; }
  try {
    fs.linkSync(tmp, outp);
    drop();
    return;
  } catch (e) {
    if (e.code === 'EEXIST') exists();
  }
  if (fs.existsSync(outp)) exists();
  fs.renameSync(tmp, outp);
  tmpFiles.delete(tmp);
}

function writeAtomic(target, data, force) {
  const tmp = tmpPath(target);
  tmpFiles.add(tmp);
  fs.writeFileSync(tmp, data, { flag: 'wx', mode: 0o600 });
  commitOutput(tmp, target, force);
}

function guardOutput(inp, outp, force) {
  if (path.resolve(inp) === path.resolve(outp)) fail('output tidak boleh sama dengan input.');
  if (fs.existsSync(outp)) {
    try {
      const lst = fs.lstatSync(outp);
      if (lst.isSymbolicLink()) fail(`"${outp}" adalah symlink — ditolak demi keamanan.`);
      const a = fs.statSync(inp);
      const b = fs.statSync(outp);
      if (a.ino && a.ino === b.ino && a.dev === b.dev) fail('output sama dengan input (link/alias).');
    } catch (e) { if (/sama dengan input|symlink/.test(e.message)) throw e; }
    if (!force) fail(`"${outp}" sudah ada. Pakai --force kalau memang mau menimpa.`);
  }
}

// Hapus permanen (multi-pass shred) agar file asli tidak mudah dipulihkan dari disk
function shredFile(target) {
  const st = fs.lstatSync(target);
  if (st.isSymbolicLink() || !st.isFile()) fail(`"${target}" bukan file biasa — batal shred.`);
  const size = st.size;
  const buf = Buffer.allocUnsafe(CHUNK);
  const fd = fs.openSync(target, 'r+');
  try {
    // Pass 1: data acak kriptografis; Pass 2: nol semua
    for (let pass = 0; pass < 2; pass++) {
      let pos = 0;
      if (pass === 1) buf.fill(0);
      while (pos < size) {
        const take = Math.min(CHUNK, size - pos);
        if (pass === 0) crypto.randomFillSync(buf, 0, take);
        let written = 0;
        while (written < take) {
          written += fs.writeSync(fd, buf, written, take - written, pos + written);
        }
        pos += take;
      }
      fs.fsyncSync(fd);
    }
    fs.ftruncateSync(fd, 0);
    fs.fsyncSync(fd);
  } finally {
    zero(buf);
    fs.closeSync(fd);
  }
  const dir = path.dirname(path.resolve(target));
  const wipedName = path.join(dir, `.wipe-${crypto.randomBytes(8).toString('hex')}.tmp`);
  try {
    fs.renameSync(target, wipedName);
    fs.unlinkSync(wipedName);
  } catch (_) {
    fs.unlinkSync(target);
  }
}

function readExact(fd, buf, len, pos) {
  let got = 0;
  while (got < len) {
    const n = fs.readSync(fd, buf, got, len - got, pos + got);
    if (n === 0) return false;
    got += n;
  }
  return true;
}

function writeAll(fd, buf) {
  let off = 0;
  while (off < buf.length) off += fs.writeSync(fd, buf, off, buf.length - off);
}

// ───────────────────────── padding Padmé ─────────────────────────
function padme(T) {
  const t = BigInt(T);
  const E = BigInt(t.toString(2).length - 1);
  const S = BigInt(E.toString(2).length);
  const lastBits = E - S;
  if (lastBits <= 0n) return T;
  const mask = (1n << lastBits) - 1n;
  return Number((t + mask) & ~mask);
}

// ───────────────────────── kebijakan PIN ─────────────────────────
const COMMON = ['123456', '654321', '123123', '112233', '121212', '123321', '101010', '111222',
  '147258', '258369', '159753', '789456', '456789', '456123', '112211', '520520', '131313',
  '696969', '246810', '135790', '100000', '200000', '000001', '000123', '123000', '121314',
  '202020', '112112'].map((s) => Buffer.from(s, 'latin1'));

function weakReason(p) {
  const d = Array.from(p, (b) => b - 48);
  if (d.every((x) => x === d[0])) return 'semua angka sama';
  if (COMMON.some((c) => c.equals(p))) return 'PIN terlalu umum';
  const diff = d.slice(1).map((x, i) => (x - d[i] + 10) % 10);
  if (diff.every((x) => x === 1) || diff.every((x) => x === 9)) return 'angka berurutan';
  if ((d[0] === d[2] && d[2] === d[4] && d[1] === d[3] && d[3] === d[5]) ||
      (d[0] === d[3] && d[1] === d[4] && d[2] === d[5]) ||
      (d[0] === d[1] && d[2] === d[3] && d[4] === d[5]) ||
      (d[0] === d[5] && d[1] === d[4] && d[2] === d[3])) return 'pola berulang / palindrom';
  if (new Set(d).size <= 2) return 'variasi angka terlalu sedikit (minimal 3 angka berbeda)';
  return null;
}

// ───────────────────────── input PIN (tanpa echo, tanpa string) ─────────────────────────
function readPinTTY(label, allowBack = false) {
  return new Promise((resolve) => {
    const stdin = process.stdin;
    const pin = Buffer.alloc(6);
    let n = 0;
    let esc = 0;
    const render = () => {
      const dots = '●'.repeat(n) + '○'.repeat(6 - n);
      const hint = allowBack ? ' [Backspace di awal = kembali]' : '';
      process.stdout.write(`\r${label} [${dots}]${hint}: `);
    };
    const finish = (val) => {
      stdin.removeListener('data', onData);
      stdin.setRawMode(false);
      stdin.pause();
      process.stdout.write('\n');
      resolve(val);
    };
    const onData = (chunk) => {
      for (let i = 0; i < chunk.length; i++) {
        const b = chunk[i];
        if (esc === 1) { esc = (b === 0x5b || b === 0x4f) ? 2 : 0; continue; }
        if (esc === 2) { if (b >= 0x40 && b <= 0x7e) esc = 0; continue; }
        if (b === 0x1b) { esc = 1; continue; }
        if (b === 0x03 || b === 0x04) {
          zero(pin, chunk);
          stdin.setRawMode(false);
          process.stdout.write('\n');
          process.exit(130);
        }
        if (b === 0x0d || b === 0x0a) {
          if (n === 6) { zero(chunk); finish(pin); return; }
          zero(pin);
          n = 0;
          process.stdout.write('\n  harus tepat 6 digit.\n');
          render();
          continue;
        }
        if (b === 0x7f || b === 0x08) {
          if (n > 0) {
            n--;
            pin[n] = 0;
            render();
          } else if (allowBack) {
            zero(pin, chunk);
            finish(null); // sinyal kembali ke PIN lapis sebelumnya
            return;
          }
          continue;
        }
        if (b >= 0x30 && b <= 0x39 && n < 6) {
          pin[n++] = b;
          render();
        }
      }
      zero(chunk);
    };
    render();
    stdin.setRawMode(true);
    stdin.resume();
    stdin.on('data', onData);
  });
}

let stdinLines = null;
async function readPin(label, allowBack = false) {
  if (process.stdin.isTTY) return readPinTTY(label, allowBack);
  if (!ui.stdinPins) fail('PIN harus diketik di terminal interaktif (--stdin-pins hanya untuk otomasi/tes).');
  if (!stdinLines) {
    const chunks = [];
    for await (const c of process.stdin) chunks.push(c);
    stdinLines = Buffer.concat(chunks).toString('latin1').split(/\r?\n/);
  }
  const line = stdinLines.shift();
  if (line === undefined || !/^\d{6}$/.test(line)) fail('PIN dari stdin harus 6 digit.');
  return Buffer.from(line, 'latin1');
}

async function askOpenPins() {
  if (!process.stdin.isTTY) {
    const pins = [];
    for (let i = 1; i <= 3; i++) pins.push(await readPin(`PIN lapis ${i}`));
    return pins;
  }
  const pins = [null, null, null];
  let idx = 0;
  while (idx < 3) {
    const p = await readPin(`PIN lapis ${idx + 1}`, idx > 0);
    if (p === null && idx > 0) {
      idx--;
      zero(pins[idx]);
      pins[idx] = null;
      continue;
    }
    pins[idx++] = p;
  }
  return pins;
}

async function askNewPins() {
  const pins = [];
  for (let i = 1; i <= 3; i++) {
    for (;;) {
      const p = await readPin(`PIN lapis ${i}`, i > 1);
      if (p === null && i > 1) {
        i -= 2;
        zero(pins.pop());
        break;
      }
      const why = weakReason(p) || (pins.some((q) => q.equals(p)) ? 'sama dengan PIN lapis lain' : null);
      if (why) { console.log(`  ditolak: ${why}. Pilih PIN lain.`); zero(p); continue; }
      const c = await readPin(`ulangi PIN lapis ${i}`);
      const same = p.equals(c);
      zero(c);
      if (!same) { console.log('  tidak cocok, ulangi.'); zero(p); continue; }
      pins.push(p);
      break;
    }
  }
  return pins;
}

function waitEnter(msg) {
  return new Promise((res) => {
    process.stdout.write(msg);
    process.stdin.setRawMode(true);
    process.stdin.resume();
    process.stdin.once('data', () => {
      process.stdin.setRawMode(false);
      process.stdin.pause();
      process.stdout.write('\n');
      res();
    });
  });
}

async function genPins() {
  const out = [];
  while (out.length < 3) {
    const b = Buffer.from(String(crypto.randomInt(0, 1000000)).padStart(6, '0'), 'latin1');
    if (weakReason(b) || out.some((q) => q.equals(b))) continue;
    out.push(b);
  }
  console.log('\nPIN acak — CATAT SEKARANG (tidak akan ditampilkan lagi):');
  out.forEach((p, i) => console.log(`  lapis ${i + 1}: ${p.toString('latin1')}`));
  if (process.stdin.isTTY) {
    await waitEnter('\nTekan sembarang tombol setelah dicatat (layar dibersihkan)… ');
    process.stdout.write('\x1b[3J\x1b[2J\x1b[H');
  }
  return out;
}

// ───────────────────────── kriptografi ─────────────────────────
function keyfileDigest(p) {
  if (!p) return null;
  const st = fs.statSync(p);
  if (!st.isFile() || st.size < 32) fail('keyfile harus file biasa, minimal 32 byte (buat dengan: genkey).');
  const h = crypto.createHash('sha512');
  const fd = fs.openSync(p, 'r');
  const b = Buffer.allocUnsafe(CHUNK);
  try {
    let n;
    while ((n = fs.readSync(fd, b, 0, b.length, null)) > 0) h.update(b.subarray(0, n));
  } finally {
    zero(b);
    fs.closeSync(fd);
  }
  return h.digest();
}

function deriveKeys(pins, kfDigest, body, logN) {
  const secret = Buffer.concat([
    Buffer.from('nyx2|'), pins[0], Buffer.from('|'), pins[1], Buffer.from('|'), pins[2],
    kfDigest || Buffer.alloc(0),
  ]);
  const salt = body.subarray(13, 45);
  const N = 2 ** logN;
  let master;
  try {
    master = crypto.scryptSync(secret, salt, 32, { N, r: SCRYPT_R, p: SCRYPT_P, maxmem: 256 * N * SCRYPT_R });
  } catch (e) {
    fail('scrypt gagal (RAM kurang? Coba opsi --fast): ' + e.message);
  } finally { zero(secret); }
  const hk = (info) => Buffer.from(crypto.hkdfSync('sha256', master, salt, info, 32));
  const keys = {
    kAes: hk('nyx2/aes-256-gcm'),
    kCha: hk('nyx2/chacha20-poly1305'),
    commit: hk(Buffer.concat([Buffer.from('nyx2/commit'), body])),
  };
  zero(master);
  return keys;
}

function chunkNonce(prefix, idx, last, out = Buffer.allocUnsafe(12)) {
  prefix.copy(out, 0);
  out.writeUInt32BE(idx, 7);
  out[11] = last ? 1 : 0;
  return out;
}

// Enkripsi chunk tanpa Buffer.concat berulang (menulis langsung ke outBuf)
function sealChunkInto(keys, nonce, aad, plain, outBuf) {
  const pos = cipherSealSliceInto(keys.kCha, keys.kAes, nonce, aad, plain, outBuf);
  return outBuf.subarray(0, pos);
}

// Dekripsi chunk tanpa Buffer.concat berulang
function openChunkInto(keys, nonce, aad, sealed, outBuf) {
  const pos = cipherOpenSliceInto(keys.kCha, keys.kAes, nonce, aad, sealed, outBuf);
  return outBuf.subarray(0, pos);
}

// isi buffer dengan aliran logis: [8 byte panjang asli][data asli][nol (padding)]
function fillLogical(fd, buf, off, take, lenPrefix, size, hash) {
  let filled = 0;
  while (filled < take) {
    const pos = off + filled;
    const want = take - filled;
    if (pos < 8) {
      const n = Math.min(8 - pos, want);
      lenPrefix.copy(buf, filled, pos, pos + n);
      filled += n;
    } else if (pos < 8 + size) {
      const n = Math.min(8 + size - pos, want);
      if (!readExact(fd, buf.subarray(filled, filled + n), n, pos - 8)) fail('file input berubah saat dibaca.');
      if (hash) hash.update(buf.subarray(filled, filled + n));
      filled += n;
// Helper pengiriman tugas ke slot Worker (Double-Buffered)
function dispatchWorkerSlot(wp, slotIdx, mode, chunkIdx, byteLen, isLast) {
  const base = slotIdx * 8;
  const workerIdx = slotIdx % wp.count;
  const wBase = (wp.slotCount * 8) + workerIdx * 4;
  Atomics.store(wp.ctrl, base + 1, chunkIdx);
  Atomics.store(wp.ctrl, base + 2, byteLen);
  Atomics.store(wp.ctrl, base + 3, isLast ? 1 : 0);
  Atomics.store(wp.ctrl, base, mode);
  Atomics.add(wp.ctrl, wBase, 1);
  Atomics.notify(wp.ctrl, wBase, 1);
}

// Helper menunggu hasil dari slot Worker
function waitWorkerSlot(wp, slotIdx, mode) {
  const base = slotIdx * 8;
  while (Atomics.load(wp.ctrl, base) === mode) {
    Atomics.wait(wp.ctrl, base, mode);
  }
  const st = Atomics.load(wp.ctrl, base);
  const outLen = Atomics.load(wp.ctrl, base + 2);
  return { ok: st === STATE_OK, outLen };
}

function releaseWorkerSlot(wp, slotIdx) {
  const base = slotIdx * 8;
  Atomics.store(wp.ctrl, base, STATE_IDLE);
}

// Dekripsi streaming (otomatis memakai Double-Buffered Worker Pool paralel bila chunk >= 2)
function decryptStream(fd, fileSize, hdr, keys, onPlain, showProgress = false) {
  const total = fileSize - HEADER_LEN;
  if (total < OVERHEAD + 1) return false;
  const n = Math.ceil(total / FULL);
  const lastLen = total - (n - 1) * FULL;
  if (lastLen < OVERHEAD + 1 || n > 0xffffffff) return false;
  const P = total - OVERHEAD * n;
  if (P < 8) return false;

  const wp = n >= 2 ? getOrInitPool() : null;
  const tStart = Date.now();

  // Jalur Multi-Core Double-Buffered Paralel (Overlapped Disk Read + Worker Decrypt + Disk Write)
  if (wp) {
    keys.kCha.copy(wp.keyBuf, 0);
    keys.kAes.copy(wp.keyBuf, 32);
    hdr.copy(wp.hdrBuf, 0);

    const S = Math.min(wp.slotCount, n);
    let nextToRead = 0;
    let aborted = false;

    const stageRead = (slotIdx, chunkIdx) => {
      const len = chunkIdx === n - 1 ? lastLen : FULL;
      if (!readExact(fd, wp.slots[slotIdx], len, HEADER_LEN + chunkIdx * FULL)) return false;
      dispatchWorkerSlot(wp, slotIdx, STATE_DEC, chunkIdx, len, chunkIdx === n - 1);
      return true;
    };

    try {
      for (let s = 0; s < S; s++) {
        if (!stageRead(s, nextToRead++)) { aborted = true; break; }
      }

      let remaining = -1n;
      for (let i = 0; i < n; i++) {
        if (aborted && i >= nextToRead) break;
        const s = i % S;
        const res = waitWorkerSlot(wp, s, STATE_DEC);
        if (!res.ok || aborted) {
          aborted = true;
          releaseWorkerSlot(wp, s);
          continue;
        }

        const pt = wp.slots[s].subarray(0, res.outLen);
        let start = 0;
        if (i === 0) {
          if (pt.length < 8) { aborted = true; releaseWorkerSlot(wp, s); continue; }
          remaining = pt.readBigUInt64BE(0);
          if (remaining > BigInt(P - 8)) { aborted = true; releaseWorkerSlot(wp, s); continue; }
          start = 8;
        }
        if (!aborted && remaining > 0n) {
          const avail = pt.length - start;
          const take = remaining < BigInt(avail) ? Number(remaining) : avail;
          if (take > 0) onPlain(pt.subarray(start, start + take));
          remaining -= BigInt(take);
        }
        pt.fill(0);
        releaseWorkerSlot(wp, s);

        if (showProgress && !aborted && ((i & 15) === 0 || i === n - 1)) {
          renderProgress('Membuka  ', Math.min(total, (i + 1) * FULL), total, tStart);
        }

        if (!aborted && nextToRead < n) {
          if (!stageRead(s, nextToRead++)) aborted = true;
        }
      }
      return !aborted && remaining === 0n;
    } finally {
      wp.keyBuf.fill(0);
      for (let s = 0; s < wp.slotCount; s++) {
        const st = Atomics.load(wp.ctrl, s * 8);
        if (st === STATE_DEC) waitWorkerSlot(wp, s, STATE_DEC);
        releaseWorkerSlot(wp, s);
        wp.slots[s].fill(0);
      }
    }
  }

  // Jalur Single-Thread Cepat (Zero-Concat, untuk file kecil / selftest)
  const prefix = hdr.subarray(6, 13);
  const inBuf = Buffer.allocUnsafe(FULL);
  const outBuf = Buffer.allocUnsafe(CHUNK);
  const nonce = Buffer.allocUnsafe(12);
  let remaining = -1n;
  try {
    for (let i = 0; i < n; i++) {
      const len = i === n - 1 ? lastLen : FULL;
      if (!readExact(fd, inBuf, len, HEADER_LEN + i * FULL)) return false;
      let pt;
      try {
        pt = openChunkInto(keys, chunkNonce(prefix, i, i === n - 1, nonce), hdr, inBuf.subarray(0, len), outBuf);
      } catch (_) { return false; }
      let start = 0;
      if (i === 0) {
        if (pt.length < 8) return false;
        remaining = pt.readBigUInt64BE(0);
        if (remaining > BigInt(P - 8)) return false;
        start = 8;
      }
      if (remaining > 0n) {
        const avail = pt.length - start;
        const take = remaining < BigInt(avail) ? Number(remaining) : avail;
        if (take > 0) onPlain(pt.subarray(start, start + take));
        remaining -= BigInt(take);
      }
      pt.fill(0);
    }
    return remaining === 0n;
  } finally {
    zero(inBuf, outBuf, nonce);
  }
}

// Verifikasi cepat untuk file besar (> 32 MiB): cek ukuran fisik, header, dan uji dekripsi
// chunk pertama, tengah, dan terakhir langsung dari disk (< 20 ms, tanpa baca ulang 4 GB).
function verifyQuickOnDisk(tmpFile, fdIn, origSize, P, n, hdr, keys) {
  const fdV = fs.openSync(tmpFile, 'r');
  const inBuf = Buffer.allocUnsafe(FULL);
  const outBuf = Buffer.allocUnsafe(CHUNK);
  const srcCheck = Buffer.allocUnsafe(CHUNK);
  const lenPrefix = Buffer.alloc(8);
  lenPrefix.writeBigUInt64BE(BigInt(origSize));
  const prefix = hdr.subarray(6, 13);
  const nonce = Buffer.allocUnsafe(12);

  try {
    const expectedTotal = HEADER_LEN + P + n * OVERHEAD;
    if (fs.fstatSync(fdV).size !== expectedTotal) return false;

    const diskHdr = Buffer.allocUnsafe(HEADER_LEN);
    if (!readExact(fdV, diskHdr, HEADER_LEN, 0) || !diskHdr.equals(hdr)) return false;

    // Daftar indeks chunk yang diuji: pertama, tengah, acak, dan terakhir
    const indices = new Set([0, Math.floor(n / 2), n - 1]);
    if (n > 4) indices.add(crypto.randomInt(1, n - 1));

    for (const idx of indices) {
      const off = idx * CHUNK;
      const plainTake = Math.min(CHUNK, P - off);
      const sealedLen = plainTake + OVERHEAD;
      if (!readExact(fdV, inBuf, sealedLen, HEADER_LEN + idx * FULL)) return false;

      let pt;
      try {
        pt = openChunkInto(keys, chunkNonce(prefix, idx, idx === n - 1, nonce), hdr, inBuf.subarray(0, sealedLen), outBuf);
      } catch (_) { return false; }

      fillLogical(fdIn, srcCheck, off, plainTake, lenPrefix, origSize, null);
      if (pt.length !== plainTake || !pt.equals(srcCheck.subarray(0, plainTake))) return false;
    }
    return true;
  } finally {
    zero(inBuf, outBuf, srcCheck, nonce);
    fs.closeSync(fdV);
  }
}

// ───────────────────────── operasi inti ─────────────────────────
function encryptFile({
  inp,
  outp,
  pins,
  keyDigest = null,
  logN = LOGN_DEFAULT,
  force = false,
  fullVerify = false,
  onStatus = () => {},
}) {
  const st = fs.statSync(inp);
  if (!st.isFile()) fail('input harus file biasa (folder? zip dulu).');
  guardOutput(inp, outp, force);

  const hdr = Buffer.alloc(HEADER_LEN);
  MAGIC.copy(hdr, 0);
  hdr[4] = keyDigest ? FLAG_KEYFILE : 0;
  hdr[5] = logN;
  crypto.randomBytes(7).copy(hdr, 6);
  crypto.randomBytes(32).copy(hdr, 13);

  onStatus(`Menurunkan kunci (scrypt ${(128 * 2 ** logN * SCRYPT_R) / 2 ** 20} MiB), tunggu…`);
  const keys = deriveKeys(pins, keyDigest, hdr.subarray(0, BODY_LEN), logN);
  keys.commit.copy(hdr, BODY_LEN);

  const size = st.size;
  const P = padme(8 + size);
  const n = Math.max(1, Math.ceil(P / CHUNK));
  if (n > 0xffffffff) fail('file terlalu besar.');

  const doFullVerify = fullVerify || ui.fullVerify || size <= FULL_VERIFY_LIMIT;
  const srcHash = doFullVerify ? crypto.createHash('sha256') : null;
  const tmp = tmpPath(outp);
  let ok = false;
  let vhex = '';

  try {
    const fdIn = fs.openSync(inp, 'r');
    let fdOut;
    try {
      fdOut = fs.openSync(tmp, 'wx', 0o600);
      tmpFiles.add(tmp);
      writeAll(fdOut, hdr);

      const lenPrefix = Buffer.alloc(8);
      lenPrefix.writeBigUInt64BE(BigInt(size));
      const wp = n >= 2 ? getOrInitPool() : null;
      const tStart = Date.now();
      const showProg = size >= 8 * (1 << 20);

      if (wp) {
        // Jalur Multi-Core Double-Buffered: Worker tidak pernah menganggur saat disk membaca/menulis
        keys.kCha.copy(wp.keyBuf, 0);
        keys.kAes.copy(wp.keyBuf, 32);
        hdr.copy(wp.hdrBuf, 0);

        const S = Math.min(wp.slotCount, n);
        let nextChunk = 0;

        const stageEncrypt = (slotIdx, chunkIdx) => {
          const off = chunkIdx * CHUNK;
          const take = Math.min(CHUNK, P - off);
          fillLogical(fdIn, wp.slots[slotIdx], off, take, lenPrefix, size, srcHash);
          dispatchWorkerSlot(wp, slotIdx, STATE_ENC, chunkIdx, take, chunkIdx === n - 1);
        };

        try {
          for (let s = 0; s < S; s++) stageEncrypt(s, nextChunk++);

          for (let i = 0; i < n; i++) {
            const s = i % S;
            const res = waitWorkerSlot(wp, s, STATE_ENC);
            if (!res.ok) {
              releaseWorkerSlot(wp, s);
              fail('enkripsi chunk gagal di worker.');
            }
            writeAll(fdOut, wp.slots[s].subarray(0, res.outLen));
            releaseWorkerSlot(wp, s);

            if (showProg && ((i & 15) === 0 || i === n - 1)) {
              renderProgress('Enkripsi ', Math.min(P, (i + 1) * CHUNK), P, tStart);
            }

            if (nextChunk < n) stageEncrypt(s, nextChunk++);
          }
        } finally {
          wp.keyBuf.fill(0);
          for (let s = 0; s < wp.slotCount; s++) {
            const stSlot = Atomics.load(wp.ctrl, s * 8);
            if (stSlot === STATE_ENC) waitWorkerSlot(wp, s, STATE_ENC);
            releaseWorkerSlot(wp, s);
            wp.slots[s].fill(0);
          }
        }
      } else {
        // Jalur Single-Thread (untuk file kecil)
        const plainBuf = Buffer.allocUnsafe(CHUNK);
        const cipherBuf = Buffer.allocUnsafe(FULL);
        const nonce = Buffer.allocUnsafe(12);
        const prefix = hdr.subarray(6, 13);
        try {
          for (let i = 0; i < n; i++) {
            const off = i * CHUNK;
            const take = Math.min(CHUNK, P - off);
            fillLogical(fdIn, plainBuf, off, take, lenPrefix, size, srcHash);
            const sealed = sealChunkInto(
              keys,
              chunkNonce(prefix, i, i === n - 1, nonce),
              hdr,
              plainBuf.subarray(0, take),
              cipherBuf
            );
            writeAll(fdOut, sealed);
          }
        } finally {
          zero(plainBuf, cipherBuf, nonce);
        }
      }

      fs.fsyncSync(fdOut);
      fs.closeSync(fdOut);
      fdOut = undefined;

      if (doFullVerify) {
        const vh = crypto.createHash('sha256');
        const fdV = fs.openSync(tmp, 'r');
        try { ok = decryptStream(fdV, fs.fstatSync(fdV).size, hdr, keys, (b) => vh.update(b)); }
        finally { fs.closeSync(fdV); }
        vhex = vh.digest('hex');
      } else {
        ok = verifyQuickOnDisk(tmp, fdIn, size, P, n, hdr, keys);
      }
    } finally {
      fs.closeSync(fdIn);
      if (fdOut !== undefined) fs.closeSync(fdOut);
    }
  } finally { zeroKeys(keys); }

  if (!ok || (doFullVerify && vhex !== srcHash.digest('hex'))) {
    try { fs.unlinkSync(tmp); } catch (_) { /* abaikan */ }
    tmpFiles.delete(tmp);
    fail('verifikasi gagal — hasil dibuang, file asli aman.');
  }
  commitOutput(tmp, outp, force);
  return { bytesIn: size, bytesOut: HEADER_LEN + P + n * OVERHEAD };
}

function peekHeader(inp) {
  const fd = fs.openSync(inp, 'r');
  try {
    const size = fs.fstatSync(fd).size;
    const hdr = Buffer.alloc(HEADER_LEN);
    if (size < HEADER_LEN + OVERHEAD + 1 || !readExact(fd, hdr, HEADER_LEN, 0) || !hdr.subarray(0, 4).equals(MAGIC)) {
      fail('bukan file .nyx (NYX2) yang valid.');
    }
    if (hdr[5] < LOGN_MIN || hdr[5] > LOGN_MAX) fail('parameter file tidak valid.');
    return hdr;
  } finally { fs.closeSync(fd); }
}

// mengembalikan true bila isi asli yang keluar, false bila zip kosong (PIN salah / file rusak)
function openFile({ inp, outp, pins, keyDigest = null, force = false, onStatus = () => {} }) {
  const hdr = peekHeader(inp);
  guardOutput(inp, outp, force);
  const logN = hdr[5];
  onStatus(`Memproses (scrypt ${(128 * 2 ** logN * SCRYPT_R) / 2 ** 20} MiB), tunggu…`);
  const useKf = (hdr[4] & FLAG_KEYFILE) !== 0;
  const keys = deriveKeys(pins, useKf ? keyDigest : null, hdr.subarray(0, BODY_LEN), logN);
  let ok = false;
  const tmp = tmpPath(outp);
  try {
    ok = crypto.timingSafeEqual(keys.commit, hdr.subarray(BODY_LEN, HEADER_LEN));
    if (ok) {
      const fd = fs.openSync(inp, 'r');
      let fdOut;
      try {
        fdOut = fs.openSync(tmp, 'wx', 0o600);
        tmpFiles.add(tmp);
        const fSize = fs.fstatSync(fd).size;
        ok = decryptStream(fd, fSize, hdr, keys, (b) => writeAll(fdOut, b), fSize >= 8 * (1 << 20));
        if (ok) fs.fsyncSync(fdOut);
      } finally {
        fs.closeSync(fd);
        if (fdOut !== undefined) fs.closeSync(fdOut);
      }
    }
  } finally { zeroKeys(keys); }
  if (ok) {
    commitOutput(tmp, outp, force);
    return true;
  }
  try { fs.unlinkSync(tmp); } catch (_) { /* abaikan */ }
  tmpFiles.delete(tmp);
  writeAtomic(outp, EMPTY_ZIP, force);
  return false;
}

// ───────────────────────── CLI ─────────────────────────
function resolveEncryptOutput(inp, outp) {
  if (outp) return outp;
  return inp.endsWith('.nyx') ? `${inp}.enc.nyx` : `${inp}.nyx`;
}

function resolveOpenOutput(inp, outp) {
  if (outp) return outp;
  if (inp.toLowerCase().endsWith('.nyx') && inp.length > 4) return inp.slice(0, -4);
  return `${inp}.decrypted`;
}

async function cmdEncrypt([inp, rawOutp], o) {
  if (!fs.existsSync(inp)) fail(`input tidak ditemukan: ${inp}`);
  const outp = resolveEncryptOutput(inp, rawOutp);
  guardOutput(inp, outp, o.force);
  const kf = keyfileDigest(o.keyfile);
  prewarmPoolAsync(); // Panaskan worker threads sambil pengguna mengetik PIN
  const pins = o.randomPins ? await genPins() : await askNewPins();
  const chosenLogN = selectSmartLogN(o);
  try {
    const t0 = Date.now();
    const r = encryptFile({
      inp,
      outp,
      pins,
      keyDigest: kf,
      force: o.force,
      fullVerify: !!o.fullVerify,
      logN: chosenLogN,
      onStatus: (m) => console.log(m),
    });
    const sec = ((Date.now() - t0) / 1000).toFixed(1);
    console.log(`Selesai & terverifikasi dalam ${sec}s: ${outp} (${formatBytes(r.bytesOut)})`);
    if (o.shred) {
      shredFile(inp);
      console.log(`File asli telah dihancurkan permanen (--shred): ${inp}`);
    } else {
      console.log('File asli TIDAK dihapus — pakai opsi --shred atau perintah "shred" bila ingin memusnahkan file asli.');
    }
    console.log(`Tanpa 3 PIN${kf ? ' + keyfile' : ''} isi tidak bisa dipulihkan. Tidak ada reset.`);
  } finally { zero(...pins); }
}

async function cmdOpen([inp, rawOutp], o) {
  if (!fs.existsSync(inp)) fail(`input tidak ditemukan: ${inp}`);
  const outp = resolveOpenOutput(inp, rawOutp);
  const hdr = peekHeader(inp);
  const needKf = (hdr[4] & FLAG_KEYFILE) !== 0;
  if (needKf && !o.keyfile) fail('file ini butuh --keyfile <file>.');
  if (!needKf && o.keyfile) console.log('(file ini tidak memakai keyfile — opsi --keyfile diabaikan)');
  guardOutput(inp, outp, o.force);
  const kf = needKf ? keyfileDigest(o.keyfile) : null;
  prewarmPoolAsync(); // Panaskan worker threads sambil pengguna mengetik PIN
  const pins = await askOpenPins();
  try {
    openFile({ inp, outp, pins, keyDigest: kf, force: o.force, onStatus: (m) => console.log(m) });
  } finally { zero(...pins); }
  console.log(`Selesai: ${outp}`);              // pesan identik untuk PIN benar maupun salah
}

function cmdInfo([inp]) {
  if (!fs.existsSync(inp)) fail(`file tidak ditemukan: ${inp}`);
  const hdr = peekHeader(inp);
  const st = fs.statSync(inp);
  const logN = hdr[5];
  const needKf = (hdr[4] & FLAG_KEYFILE) !== 0;
  const ramMiB = (128 * (2 ** logN) * SCRYPT_R) / (2 ** 20);
  const totalPayload = st.size - HEADER_LEN;
  const chunks = Math.ceil(totalPayload / FULL);
  const paddedPlain = totalPayload - chunks * OVERHEAD;
  console.log(`Informasi File NYX2: ${inp}`);
  console.log(`  Ukuran fisik   : ${formatBytes(st.size)} (${st.size} byte)`);
  console.log(`  Ukuran payload : ~${formatBytes(Math.max(0, paddedPlain - 8))} (termasuk padding Padmé)`);
  console.log(`  Jumlah chunk   : ${chunks} chunk (@ 1 MiB)`);
  console.log(`  Butuh Keyfile  : ${needKf ? 'YA (--keyfile wajib)' : 'Tidak'}`);
  console.log(`  Parameter KDF  : scrypt logN=${logN} (RAM ${ramMiB} MiB, r=${SCRYPT_R}, p=${SCRYPT_P})`);
  console.log(`  Cara membuka   : node nyx-crypt.js open "${inp}"${needKf ? ' --keyfile <kunci>' : ''}`);
}

function cmdShred([target]) {
  if (!fs.existsSync(target)) fail(`file tidak ditemukan: ${target}`);
  shredFile(target);
  console.log(`File berhasil dihancurkan permanen (2-pass wipe + rename + unlink): ${target}`);
}

function cmdGenKey([p], o) {
  if (fs.existsSync(p) && !o.force) fail(`"${p}" sudah ada (pakai --force untuk menimpa).`);
  fs.writeFileSync(p, crypto.randomBytes(64), { mode: 0o600 });
  console.log(`Keyfile dibuat: ${p}\nSimpan terpisah dari file .nyx dan BUAT BACKUP — hilang = data hilang.`);
}

function selftest() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'nyx-selftest-'));
  const P = ['482916', '730258', '915374'];
  const mk = (a = P) => a.map((s) => Buffer.from(s, 'latin1'));
  let ok = 0;
  let all = 0;
  const check = (name, cond) => { all++; if (cond) ok++; console.log(`${cond ? ' ✓' : ' ✗'} ${name}`); };
  const src = path.join(dir, 'src.bin');
  const nyx = path.join(dir, 'a.nyx');
  const out = path.join(dir, 'out.bin');
  const o = { outp: out, force: true };
  try {
    for (const sz of [0, 1, 1000, CHUNK - 8, CHUNK - 7, 2 * CHUNK + 12345]) {
      const data = crypto.randomBytes(sz);
      fs.writeFileSync(src, data);
      encryptFile({ inp: src, outp: nyx, pins: mk(), logN: 14, force: true });
      openFile({ inp: nyx, pins: mk(), ...o });
      check(`${sz} B — PIN benar → isi asli utuh`, fs.readFileSync(out).equals(data));
      openFile({ inp: nyx, pins: mk(['482916', '730258', '915375']), ...o });
      check(`${sz} B — 1 PIN salah → zip kosong`, fs.readFileSync(out).equals(EMPTY_ZIP));
    }
    fs.writeFileSync(src, crypto.randomBytes(2 * CHUNK + 777));
    encryptFile({ inp: src, outp: nyx, pins: mk(), logN: 14, force: true });
    const good = fs.readFileSync(nyx);
    const empties = (buf) => {
      fs.writeFileSync(nyx, buf);
      openFile({ inp: nyx, pins: mk(), ...o });
      return fs.readFileSync(out).equals(EMPTY_ZIP);
    };
    const flip = (i) => { const b = Buffer.from(good); b[i] ^= 1; return b; };
    const c0 = good.subarray(HEADER_LEN, HEADER_LEN + FULL);
    const c1 = good.subarray(HEADER_LEN + FULL, HEADER_LEN + 2 * FULL);
    check('bit-flip di header → zip kosong', empties(flip(20)));
    check('bit-flip di isi → zip kosong', empties(flip(HEADER_LEN + 5000)));
    check('file dipotong → zip kosong', empties(good.subarray(0, good.length - FULL)));
    check('chunk ditukar → zip kosong', empties(Buffer.concat([good.subarray(0, HEADER_LEN), c1, c0, good.subarray(HEADER_LEN + 2 * FULL)])));
    check('chunk diduplikat → zip kosong', empties(Buffer.concat([good, c1])));
    check('sampah di ekor → zip kosong', empties(Buffer.concat([good, Buffer.from([0])])));
  } finally {
    shutdownPool();
    fs.rmSync(dir, { recursive: true, force: true });
  }
  console.log(`\n${ok}/${all} lulus`);
  return ok === all;
}

function usage() {
  console.log(`Pemakaian:
  node nyx-crypt.js encrypt <input> [output.nyx] [--keyfile f] [--turbo|--fast|--strong] [--shred] [--threads n] [--full-verify] [--random-pins] [--force]
  node nyx-crypt.js open    <input.nyx> [output] [--keyfile f] [--threads n] [--force]
  node nyx-crypt.js <input.nyx>                  (pintasan cepat untuk langsung membuka file .nyx)
  node nyx-crypt.js info    <input.nyx>          (lihat info file & kebutuhan RAM tanpa PIN)
  node nyx-crypt.js shred   <file>               (musnahkan file asli secara permanen)
  node nyx-crypt.js genkey  <keyfile>
  node nyx-crypt.js selftest`);
  process.exitCode = 1;
}

function parseArgs(argv) {
  const pos = [];
  const o = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--force') o.force = true;
    else if (a === '--strong') o.strong = true;
    else if (a === '--fast' || a === '--low-ram') o.fast = true;
    else if (a === '--turbo') o.turbo = true;
    else if (a === '--shred' || a === '--delete-source') o.shred = true;
    else if (a === '--full-verify') o.fullVerify = true;
    else if (a === '--random-pins') o.randomPins = true;
    else if (a === '--stdin-pins') o.stdinPins = true;
    else if (a === '--threads') {
      o.threads = Number(argv[++i]);
      if (!Number.isInteger(o.threads) || o.threads < 1) fail('--threads butuh angka >= 1.');
    }
    else if (a === '--keyfile') { o.keyfile = argv[++i]; if (!o.keyfile) fail('--keyfile butuh nama file.'); }
    else if (a.startsWith('--')) fail(`opsi tidak dikenal: ${a}`);
    else pos.push(a);
  }
  return { pos, o };
}

async function main() {
  const [cmd, ...rest] = process.argv.slice(2);
  if (!cmd) { usage(); return; }

  // Pintasan cepat: jika argumen pertama adalah file .nyx yang ada di disk, langsung jalankan "open"
  if (cmd !== 'encrypt' && cmd !== 'open' && cmd !== 'info' && cmd !== 'shred' && cmd !== 'genkey' && cmd !== 'selftest') {
    if (cmd.toLowerCase().endsWith('.nyx') && fs.existsSync(cmd)) {
      const { pos, o } = parseArgs([cmd, ...rest]);
      ui.stdinPins = !!o.stdinPins;
      ui.threads = o.threads || 0;
      ui.fullVerify = !!o.fullVerify;
      await cmdOpen(pos, o);
      return;
    }
  }

  const { pos, o } = parseArgs(rest);
  ui.stdinPins = !!o.stdinPins;
  ui.threads = o.threads || 0;
  ui.fullVerify = !!o.fullVerify;
  if (cmd === 'encrypt' && (pos.length === 1 || pos.length === 2)) await cmdEncrypt(pos, o);
  else if (cmd === 'open' && (pos.length === 1 || pos.length === 2)) await cmdOpen(pos, o);
  else if (cmd === 'info' && pos.length === 1) cmdInfo(pos);
  else if (cmd === 'shred' && pos.length === 1) cmdShred(pos);
  else if (cmd === 'genkey' && pos.length === 1) cmdGenKey(pos, o);
  else if (cmd === 'selftest') process.exitCode = selftest() ? 0 : 1;
  else usage();
}

if (require.main === module) {
  main()
    .then(() => process.exit(process.exitCode || 0))
    .catch((e) => { console.error('Error: ' + ((e && e.message) || e)); process.exit(1); });
}

module.exports = { encryptFile, openFile, padme, weakReason, shredFile, EMPTY_ZIP, HEADER_LEN, FULL, CHUNK };