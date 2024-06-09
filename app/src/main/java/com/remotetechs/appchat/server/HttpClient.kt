package com.remotetechs.appchat.server

import android.content.Context
import android.util.Log
import com.remotetechs.appchat.viewmodel.MessageViewModel
import com.remotetechs.appchat.viewmodel.NotificationViewModel
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class HttpClient(
    private val context: Context,
    private val messageViewModel: MessageViewModel,
    private val notificationViewModel: NotificationViewModel
) : Runnable {
    var socket: Socket? = null
    val serverSocket: ServerSocket by lazy { ServerSocket(PORT) }
    val listSocket: MutableList<Socket> = mutableListOf()

    override fun run() {
        while (true) {
            try {
                socket = serverSocket.accept()
                Log.d("AAAAAAAAAAAA",socket.toString())
                socket?.let {
                    listSocket.add(it)
                    val commThread = CommunicationThread(
                        context,
                        messageViewModel,
                        notificationViewModel,
                        it,
                        listSocket,
                    )
                    Thread(commThread).start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val PORT = 8889
    }
}