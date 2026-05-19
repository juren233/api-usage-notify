package com.juren233.usagenotify.notification

import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import com.juren233.usagenotify.MainActivity
import com.juren233.usagenotify.R
import kotlin.math.max
import kotlin.math.roundToInt
import javax.inject.Inject

data class BalanceThresholds(
    val sufficient: Double = 10.0,
    val danger: Double = 2.5,
    val insufficient: Double = 1.0,
) {
    fun normalized(): BalanceThresholds {
        val values = listOf(sufficient, danger, insufficient)
            .map { if (it.isFinite()) max(0.0, it) else 0.0 }
            .sortedDescending()
        return BalanceThresholds(
            sufficient = values[0],
            danger = values[1],
            insufficient = values[2],
        )
    }
}

internal fun resolveLiveUpdateStateIconRes(
    balance: Double,
    thresholds: BalanceThresholds,
): Int {
    val normalizedThresholds = thresholds.normalized()
    return when {
        balance <= normalizedThresholds.insufficient -> R.drawable.ic_live_update_insufficient
        balance <= normalizedThresholds.danger -> R.drawable.warning_24px
        else -> R.drawable.ic_live_update_sufficient
    }
}

class LiveUpdateNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val moneyIcon by lazy(LazyThreadSafetyMode.NONE) { createMoneyIcon() }
    private val fireIcon by lazy(LazyThreadSafetyMode.NONE) { createFireIcon() }
    private val warningIcon by lazy(LazyThreadSafetyMode.NONE) { createWarningIcon() }

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val SCALE = 100

        fun calculateBalanceProgress(totalBalance: Double, thresholds: BalanceThresholds): Int {
            val normalized = thresholds.normalized()
            val maxProgress = normalized.sufficient.toProgressUnits()
            if (maxProgress <= 0) return 0
            return (maxProgress - totalBalance.toProgressUnits())
                .coerceIn(0, maxProgress)
        }

        fun calculateSegmentLengths(thresholds: BalanceThresholds): List<Int> {
            val normalized = thresholds.normalized()
            val sufficient = normalized.sufficient.toProgressUnits()
            val danger = normalized.danger.toProgressUnits().coerceAtMost(sufficient)
            val insufficient = normalized.insufficient.toProgressUnits().coerceAtMost(danger)
            return listOf(
                (sufficient - danger).coerceAtLeast(1),
                (danger - insufficient).coerceAtLeast(1),
                insufficient.coerceAtLeast(1),
            )
        }

        fun calculatePointPositions(thresholds: BalanceThresholds): List<Int> {
            val normalized = thresholds.normalized()
            val sufficient = normalized.sufficient.toProgressUnits()
            return listOf(
                0,
                sufficient - normalized.danger.toProgressUnits(),
                sufficient - normalized.insufficient.toProgressUnits(),
                sufficient,
            )
        }

        private fun Double.toProgressUnits(): Int {
            if (!isFinite() || this <= 0.0) return 0
            return (this * SCALE)
                .roundToInt()
        }
    }

    fun buildNotification(
        balance: Double,
        siteName: String,
        thresholds: BalanceThresholds = BalanceThresholds(),
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val normalizedThresholds = thresholds.normalized()
        val balanceText = "$${String.format("%.2f", balance)}"
        val progress = calculateBalanceProgress(balance, normalizedThresholds)
        val segmentLengths = calculateSegmentLengths(normalizedThresholds)
        val pointPositions = calculatePointPositions(normalizedThresholds)
        val sufficientProgress = normalizedThresholds.sufficient.toProgressUnits()
        val isIndeterminate = sufficientProgress <= 0
        val style = NotificationCompat.ProgressStyle()
            .setStyledByProgress(true)
            .setProgressIndeterminate(isIndeterminate)
            .setProgress(progress)
            .setProgressStartIcon(moneyIcon)
            .setProgressTrackerIcon(fireIcon)
            .setProgressEndIcon(warningIcon)
            .addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(segmentLengths[0])
                    .setId(1)
                    .setColor(Color.parseColor("#16A34A")),
            )
            .addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(segmentLengths[1])
                    .setId(2)
                    .setColor(Color.parseColor("#FACC15")),
            )
            .addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(segmentLengths[2])
                    .setId(3)
                    .setColor(Color.parseColor("#DC2626")),
            )
            .addProgressPoint(
                NotificationCompat.ProgressStyle.Point(pointPositions[0])
                    .setId(1)
                    .setColor(Color.parseColor("#16A34A")),
            )
            .addProgressPoint(
                NotificationCompat.ProgressStyle.Point(pointPositions[1])
                    .setId(2)
                    .setColor(Color.parseColor("#FACC15")),
            )
            .addProgressPoint(
                NotificationCompat.ProgressStyle.Point(pointPositions[2])
                    .setId(3)
                    .setColor(Color.parseColor("#DC2626")),
            )
            .addProgressPoint(
                NotificationCompat.ProgressStyle.Point(pointPositions[3])
                    .setId(4)
                    .setColor(Color.parseColor("#6B7280")),
            )

        val title = when {
            balance <= normalizedThresholds.insufficient -> "${siteName}当前余额不足，即将断粮！"
            balance <= normalizedThresholds.danger -> "${siteName}当前余额有点危险，请及时充值"
            else -> "${siteName}当前余额充足"
        }

        val builder = NotificationCompat.Builder(context, NotificationChannels.LIVE_UPDATE_CHANNEL_ID)
            .setSmallIcon(resolveLiveUpdateStateIconRes(balance, normalizedThresholds))
            .setContentTitle(title)
            .setContentText(balanceText)
            .setShortCriticalText(balanceText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setStyle(style)
            .setProgress(sufficientProgress, progress, isIndeterminate)

        if (Build.VERSION.SDK_INT >= 36) {
            builder.setRequestPromotedOngoing(true)
        }

        return builder.build()
    }

    fun update(
        balance: Double,
        siteName: String,
        thresholds: BalanceThresholds = BalanceThresholds(),
    ) {
        val notification = buildNotification(balance, siteName, thresholds)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createMoneyIcon(): IconCompat = createEmojiIcon("💰")

    private fun createFireIcon(): IconCompat = createEmojiIcon("🔥")

    private fun createWarningIcon(): IconCompat = createEmojiIcon("⚠️")

    private fun createEmojiIcon(emoji: String): IconCompat {
        val density = context.resources.displayMetrics.density
        val size = (32 * density).roundToInt().coerceAtLeast(32)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = size * 0.72f
        }
        val baseline = size / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(emoji, size / 2f, baseline, paint)
        return IconCompat.createWithBitmap(bitmap)
    }

}
