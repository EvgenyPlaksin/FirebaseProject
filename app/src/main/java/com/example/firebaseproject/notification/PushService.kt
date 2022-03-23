package com.example.firebaseproject.notification

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send registration to server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(INTENT_FILTER)
        message.data.forEach{entity ->
            intent.putExtra(entity.key, entity.value)
        }
        sendBroadcast(intent)
    }

companion object{
    const val INTENT_FILTER = "MESSAGE_EVENT"
    const val KEY_ACTION = "action"
    const val KEY_MESSAGE = "message"

    const val ACTION_SHOW_MESSAGE = "show_message"
}

}