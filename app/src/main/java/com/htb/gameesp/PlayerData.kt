package com.htb.gameesp

data class PlayerData(
    val x: Float,
    val y: Float,
    val z: Float,
    val health: Float,
    val healthMax: Float,
    val teamId: Int,
    val isBot: Boolean,
    val isDead: Boolean,
    val playerUID: Int,
    val currentWeapon: Int,
    val kills: Int,
    val name: String
)
