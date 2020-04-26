package ru.debaser.projects.tribune.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        when (message.data["type"]) {
            "vote" -> NotificationHelper.voteNotification(applicationContext, message)
        }
    }
}