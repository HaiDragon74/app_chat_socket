package com.remotetechs.appchatclient.server

import android.content.Context
import android.util.Log
import com.remotetechs.appchatclient.fragment.MessageFragment
import com.remotetechs.appchatclient.fragment.MessageFragment.Companion.SERVER_PORT
import com.remotetechs.appchatclient.viewmodel.MessageViewModel
import com.remotetechs.appchatclient.viewmodel.NotificationViewModel
import kotlinx.coroutines.Runnable
import java.io.IOException
import java.net.Socket


class HttpClient(
    private val context: Context,
    private val notificationViewModel: NotificationViewModel,
    private val messageViewModel: MessageViewModel
) : Runnable {
    var socket: Socket? = null
    override fun run() {
        if (socket == null) {
            try {
                socket = Socket(MessageFragment.ipServer, SERVER_PORT)
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
        try {
            val socket = this.socket ?: return
            val commThread =
                CommunicationThread(context, messageViewModel, notificationViewModel, socket)
            Thread(commThread).start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}