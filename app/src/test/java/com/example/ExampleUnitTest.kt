package com.example

import com.example.data.MoriDownloaderEngine
import com.example.data.MoriPlatform
import com.example.data.MtExplorerEngine
import com.example.data.MtExplorerScope
import com.example.ui.MascotMood
import com.example.ui.MascotPersona
import com.example.ui.VaultTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun testVaultTabContainsMtExplorer() {
        val tabs = VaultTab.entries
        assertTrue("VaultTab must contain MT_EXPLORER", tabs.contains(VaultTab.MT_EXPLORER))
        assertTrue("VaultTab must contain MORI_DOWNLOADER", tabs.contains(VaultTab.MORI_DOWNLOADER))
        assertTrue("VaultTab must contain DASHBOARD", tabs.contains(VaultTab.DASHBOARD))
    }

    @Test
    fun testCosmicDragonMascotEmotionsAndPersonas() {
        // Test all 8 dynamic emotional states are available
        val moods = MascotMood.entries
        assertTrue(moods.contains(MascotMood.IDLE_BREATHE))
        assertTrue(moods.contains(MascotMood.TOUCH_REACTION))
        assertTrue(moods.contains(MascotMood.PIN_SHIELD))
        assertTrue(moods.contains(MascotMood.ALERT_FIRE))
        assertTrue(moods.contains(MascotMood.SUCCESS_LEAP))
        assertTrue(moods.contains(MascotMood.DOWNLOAD_HUNTER))
        assertTrue(moods.contains(MascotMood.MT_INSPECTING))
        assertTrue(moods.contains(MascotMood.STEALTH_SHADOW))

        // Backward compatibility
        assertTrue(moods.contains(MascotMood.WAVE_HELLO))
        assertTrue(moods.contains(MascotMood.SHIELD_EYES))

        // Personas
        assertEquals("Astra Galaxy Dragon", MascotPersona.COSMIC_DRAGON.label)
    }

    @Test
    fun testMoriPlatformRecognition() {
        val tiktokPlatform = MoriDownloaderEngine.detectPlatformFromUrl("https://www.tiktok.com/@creator/video/123456789")
        assertEquals(MoriPlatform.TIKTOK, tiktokPlatform)

        val igPlatform = MoriDownloaderEngine.detectPlatformFromUrl("https://www.instagram.com/reel/Cx123456789/")
        assertEquals(MoriPlatform.INSTAGRAM, igPlatform)

        val ytPlatform = MoriDownloaderEngine.detectPlatformFromUrl("https://youtu.be/dQw4w9WgXcQ")
        assertEquals(MoriPlatform.YOUTUBE, ytPlatform)

        val xPlatform = MoriDownloaderEngine.detectPlatformFromUrl("https://x.com/astro/status/987654321")
        assertEquals(MoriPlatform.TWITTER_X, xPlatform)
    }

    @Test
    fun testMtExplorerEngineHexDump() {
        val tempFile = File.createTempFile("test_mt_hex", ".bin").apply {
            writeBytes("ARCANUM_COSMIC_DRAGON_BYTE_TEST_12345".toByteArray())
            deleteOnExit()
        }

        val hex = MtExplorerEngine.readHexDump(tempFile, maxBytes = 64)
        assertNotNull(hex)
        assertTrue(hex.contains("00000000:"))
        assertTrue(hex.contains("ARCANUM_COSMIC_D"))
    }

    @Test
    fun testMtExplorerScopes() {
        val scopes = MtExplorerScope.entries
        assertEquals(2, scopes.size)
        assertTrue(scopes.contains(MtExplorerScope.DEVICE_ALL))
        assertTrue(scopes.contains(MtExplorerScope.VAULT_EXCLUSIVE))
    }
}
