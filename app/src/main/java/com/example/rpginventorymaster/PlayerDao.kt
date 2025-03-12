package com.example.rpginventorymaster

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>


    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player): Int

    @Delete
    suspend fun deletePlayer(player: Player): Int

    @Query("SELECT * FROM players")
    suspend fun getAllPlayersOnce(): List<Player>
}