package com.remotetechs.appchatclient.server

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.remotetechs.appchatclient.model.Message
import com.remotetechs.appchatclient.viewmodel.MessageViewModel
import com.remotetechs.appchatclient.viewmodel.NotificationViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.util.Base64

class CommunicationThread(
    private val context: Context,
    private val messageViewModel: MessageViewModel,
    private val notificationViewModel: NotificationViewModel,
    private val clientSocket: Socket
) : Runnable {
    private var input: BufferedReader? = null

    override fun run() {
        if (input == null) {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        while (true) {
            try {
                Thread.sleep(500)
                val read = input?.readLine()
                val messageReceive = runCatching {
                    Gson().fromJson(read, Message::class.java)
                }.getOrNull()
                if (read!=null) {
                    messageReceive?.let { notificationViewModel.showNotification(context,it) }
                    messageReceive?.let { messageViewModel.insertMessage(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}