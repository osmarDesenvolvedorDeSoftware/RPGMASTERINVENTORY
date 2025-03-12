package com.example.rpginventorymaster

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val hp: Int,
    val ca: Int,
    val initiative: Int,
    val gold: Int = 0 // Novo campo para gerenciar o dinheiro do jogador
)