package com.ydh.salvio.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PrCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = TokenDataStore(context)
        val token = dataStore.token.first() ?: return Result.success()
        val watchedRepos = dataStore.watchedRepos.first()
        if (watchedRepos.isEmpty()) return Result.success()

        val repo = (context.applicationContext as SalvioApplication).githubRepository(token)

        watchedRepos.forEach { fullName ->
            val parts = fullName.split("/")
            if (parts.size != 2) return@forEach
            val (owner, repoName) = parts

            val prs = repo.getPullRequests(owner, repoName, "open").getOrElse { return@forEach }
            val currentCount = prs.size
            val lastCount = dataStore.getLastPrCount(fullName)

            if (currentCount > lastCount) {
                val newCount = currentCount - lastCount
                sendNotification(
                    title = "새 PR - $repoName",
                    body = "${newCount}개의 새 Pull Request가 열렸습니다."
                )
            }
            dataStore.saveLastPrCount(fullName, currentCount)
        }

        return Result.success()
    }

    private fun sendNotification(title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "salvio_pr_channel"

        val channel = NotificationChannel(channelId, "PR 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "새로운 Pull Request 알림"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val WORK_NAME = "pr_check_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrCheckWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
