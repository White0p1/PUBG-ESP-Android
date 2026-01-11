package com.htb.gameesp

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList

object GameDataProvider {

    private var job: Job? = null
    private var callback: ((List<PlayerData>) -> Unit)? = null
    private val players = CopyOnWriteArrayList<PlayerData>()
    private val handler = Handler(Looper.getMainLooper())

    private val offsets
        get() = if (RootUtils.is64Bit()) GameOffsets.x64 else GameOffsets.x32

    fun start(context: Context, onUpdate: (List<PlayerData>) -> Unit) {
        callback = onUpdate
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                updatePlayers(context)
                delay(16) // ~60 FPS
            }
        }
    }

    fun stop() {
        job?.cancel()
        callback = null
    }

    private suspend fun updatePlayers(context: Context) {
        val newPlayers = if (RootUtils.isDeviceRooted()) {
            readRootedData(context)
        } else {
            readNonRootedData(context)
        }

        players.clear()
        players.addAll(newPlayers)

        withContext(Dispatchers.Main) {
            callback?.invoke(players.toList())
        }
    }

    private fun readRootedData(context: Context): List<PlayerData> {
        val gamePid = getGamePid()
        if (gamePid == -1) return emptyList()

        val baseAddr = 0x7F000000L
        val playerCount = try {
            RootUtils.readMemoryInt(gamePid, baseAddr + offsets.PlayerNum.toLong())
        } catch (_: Exception) {
            0
        }

        val result = mutableListOf<PlayerData>()

        repeat(playerCount) { i ->
            val playerBase = baseAddr + offsets.PlayerNum + (i * 0x100)

            try {
                val x = RootUtils.readMemoryFloat(gamePid, playerBase + offsets.RelativeLocation)
                val y = RootUtils.readMemoryFloat(gamePid, playerBase + offsets.RelativeLocation + 4)
                val z = RootUtils.readMemoryFloat(gamePid, playerBase + offsets.RelativeLocation + 8)

                val health = RootUtils.readMemoryFloat(gamePid, playerBase + offsets.Health)
                val healthMax = RootUtils.readMemoryFloat(gamePid, playerBase + offsets.HealthMax)
                val teamId = RootUtils.readMemoryInt(gamePid, playerBase + offsets.TeamId)
                val isBot = RootUtils.readMemoryInt(gamePid, playerBase + offsets.IsBot) != 0
                val isDead = RootUtils.readMemoryInt(gamePid, playerBase + offsets.IsDead) != 0
                val playerUID = RootUtils.readMemoryInt(gamePid, playerBase + offsets.PlayerUID)
                val currentWeapon = RootUtils.readMemoryInt(gamePid, playerBase + offsets.CurrentWeapon)
                val kills = RootUtils.readMemoryInt(gamePid, playerBase + offsets.Kills)

                result.add(
                    PlayerData(
                        x = x,
                        y = y,
                        z = z,
                        health = health,
                        healthMax = healthMax,
                        teamId = teamId,
                        isBot = isBot,
                        isDead = isDead,
                        playerUID = playerUID,
                        currentWeapon = currentWeapon,
                        kills = kills,
                        name = "Player$i"
                    )
                )
            } catch (_: Exception) {
                // skip invalid player
            }
        }

        return result
    }

    private fun readNonRootedData(context: Context): List<PlayerData> {
        return listOf(
            readFromDebugFiles(),
            readFromSharedPrefs(),
            readFromSockets()
        ).flatten()
    }

    private fun getGamePid(): Int {
        val packages = listOf(
            "com.tencent.ig", // PUBG Mobile Global
            "com.pubg.krmobile",
            "com.vng.pubgmobile"
        )
        packages.forEach { pkg ->
            val output = RootUtils.executeRoot("pidof $pkg")
            if (!output.isNullOrEmpty()) return output.trim().toIntOrNull() ?: -1
        }
        return -1
    }

    private fun readFromDebugFiles(): List<PlayerData> = emptyList()
    private fun readFromSharedPrefs(): List<PlayerData> = emptyList()
    private fun readFromSockets(): List<PlayerData> = emptyList()
}
