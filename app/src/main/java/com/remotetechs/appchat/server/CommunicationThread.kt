package com.remotetechs.appchat.server

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.remotetechs.appchat.model.Message
import com.remotetechs.appchat.viewmodel.MessageViewModel
import com.remotetechs.appchat.viewmodel.NotificationViewModel
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Base64

class CommunicationThread(
    private val context: Context,
    private val messageViewModel: MessageViewModel,
    private val notificationViewModel: NotificationViewModel,
    private val clientSocket: Socket,
    private val listSocket: MutableList<Socket>
) : Runnable {
    private var input: BufferedReader? = null

    override fun run() {
        if (input == null) {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            messageViewModel.accept(clientSocket.toString())
        }
        while (true) {
            Thread.sleep(500)
            val messageJson = input?.readLine()
            val messageReceive = runCatching {
                Gson().fromJson(messageJson, Message::class.java)
            }.getOrNull()
            if (messageJson!=null){
                try {
                    messageReceive?.let { notificationViewModel.showNotification(context,it) }
                    messageReceive?.let { messageViewModel.insertMessage(it) }
                    listSocket.forEach { clientSocket ->
                        if (!checkClient(this.clientSocket, clientSocket)) {
                            val out = PrintWriter(clientSocket.getOutputStream(), true)
                            out.println(Gson().toJson(messageReceive))
                        }
                    }
                }catch (err:Exception){
                    err.printStackTrace()
                }
            }
        }
    }

    private fun checkClient(socket1: Socket, socket2: Socket): Boolean {
        return socket1.inetAddress.hostAddress == socket2.inetAddress.hostAddress && socket1.port == socket2.port
    }
}