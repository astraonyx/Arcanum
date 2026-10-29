package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items WHERE vaultProfileId = :vaultId ORDER BY addedAt DESC")
    fun getItemsForVault(vaultId: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items ORDER BY addedAt DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VaultItemEntity>)

    @Update
    suspend fun updateItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM vault_items WHERE vaultProfileId = :vaultId")
    suspend fun getCountForVault(vaultId: Int): Int
}
