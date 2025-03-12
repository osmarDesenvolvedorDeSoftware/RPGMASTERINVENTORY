package com.example.rpginventorymaster

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE playerId = :playerId")
    fun getItemsForPlayer(playerId: Int): Flow<List<Item>>
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): Item?

    @Insert
    suspend fun insertItem(item: Item): Long // Retorna o ID inserido

    @Update
    suspend fun updateItem(item: Item): Int // Retorna número de linhas afetadas

    @Delete
    suspend fun deleteItem(item: Item): Int // Retorna número de linhas afetadas
}