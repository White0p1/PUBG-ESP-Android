package com.htb.gameesp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class ESPCanvas @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val players = mutableListOf<PlayerData>()
    private var espEnabled = true
    private val paints = ESPPaints()

    fun updatePlayers(newPlayers: List<PlayerData>) {
        synchronized(players) {
            players.clear()
            players.addAll(newPlayers)
        }
        invalidate()
    }

    fun setESPEnabled(enabled: Boolean) {
        espEnabled = enabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!espEnabled) return

        val centerX = width / 2f
        val centerY = height / 2f

        synchronized(players) {
            for (player in players) {
                // World to Screen (simplified projection)
                val screenX = centerX + (player.x * 100f)
                val screenY = centerY - (player.y * 100f)
                val boxSize = 40f
                val headOffset = 60f

                // Player Box
                paints.boxPaint.color = getTeamColor(player.teamId)
                canvas.drawRect(
                    screenX - boxSize/2, screenY - headOffset,
                    screenX + boxSize/2, screenY + boxSize/2,
                    paints.boxPaint
                )

                // Health Bar
                val healthHeight = 40f * (player.health / 100f)
                paints.healthPaint.color = if (player.health > 50) Color.GREEN else Color.RED
                canvas.drawRect(
                    screenX + boxSize/2 + 5, screenY - headOffset + 5,
                    screenX + boxSize/2 + 15, screenY - headOffset + 5 + healthHeight,
                    paints.healthPaint
                )

                // Distance
                paints.textPaint.color = Color.WHITE
                paints.textPaint.textSize = 24f
                val distanceText = "${player.distance.toInt()}m"
                canvas.drawText(
                    distanceText,
                    screenX - boxSize/2 - 10,
                    screenY - headOffset - 10,
                    paints.textPaint
                )

                // Name (if available)
                if (player.name.isNotEmpty()) {
                    paints.textPaint.textSize = 20f
                    paints.textPaint.color = Color.CYAN
                    canvas.drawText(
                        player.name,
                        screenX - boxSize/2,
                        screenY + boxSize/2 + 25,
                        paints.textPaint
                    )
                }
            }
        }
    }

    private fun getTeamColor(teamId: Int): Int {
        return when (teamId % 4) {
            0 -> Color.RED
            1 -> Color.BLUE
            2 -> Color.GREEN
            3 -> Color.YELLOW
            else -> Color.WHITE
        }
    }
}

data class PlayerData(
    val x: Float, val y: Float, val z: Float,
    val health: Float,
    val distance: Float,
    val teamId: Int,
    val name: String = ""
)

class ESPPaints {
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    val healthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFakeBoldText = true
        textAlign = Paint.Align.RIGHT
    }
}
