package com.htb.gameesp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter

object ESPManager {
    fun startESP(context: Context) {
        val intent = Intent(context, ESPService::class.java)
        context.startForegroundService(intent)
    }
}
