package com.remotetechs.appchat.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.CursorWindow
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.remotetechs.appchat.R
import com.remotetechs.appchat.adapter.MessageAdapter
import com.remotetechs.appchat.databinding.ActivityMainBinding
import com.remotetechs.appchat.model.Message
import com.remotetechs.appchat.room.MessageDatabase
import com.remotetechs.appchat.server.HttpClient
import com.remotetechs.appchat.viewmodel.MessageViewModel
import com.remotetechs.appchat.viewmodel.MessageViewModelFactory
import com.remotetechs.appchat.viewmodel.NotificationViewModel
import com.remotetechs.appchat.viewmodel.Repository
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var messageAdapter: MessageAdapter
    private var httpClient: HttpClient? = null
    private lateinit var serverThread: Thread
    private lateinit var messageViewModel: MessageViewModel

    @Inject
    lateinit var notificationViewModel: NotificationViewModel
    private lateinit var handler: Handler
    private var message = Message()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        message.name = "Server"
        messageAdapter = MessageAdapter()
        handler = Handler(Looper.getMainLooper())
        binding.recyclerMessage.layoutManager = LinearLayoutManager(this)
        binding.recyclerMessage.adapter = messageAdapter
        (binding.recyclerMessage.layoutManager as LinearLayoutManager).stackFromEnd = true
        val messageViewModelFactory =
            MessageViewModelFactory(Repository(MessageDatabase.getCalculateDatabase(this)))
        messageViewModel = ViewModelProvider(
            this, messageViewModelFactory
        )[MessageViewModel::class.java]
        clientAccept()
        askNotificationPermission()
        initializeCursorSize()
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun initializeCursorSize() {
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun clientAccept() {
        messageViewModel.liveDataAccept.observe(this) {
            binding.tvStartServer.text = it.toString()
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_start_server -> {
                binding.btnStartServer.visibility = View.GONE
                binding.tvStartServer.visibility = View.VISIBLE
                binding.tvStartServer.text = getString(R.string.server_started)
                showMessage()
                httpClient = HttpClient(this, messageViewModel, notificationViewModel)
                serverThread = Thread(httpClient)
                serverThread.start()
            }

            R.id.btn_send -> {
                val msg = binding.edtMessage.text.toString().trim()
                message.message = msg
                message.time = getTime()
                messageViewModel.insertMessage(message)
                val messageJson = Gson().toJson(message) ?: return
                if (httpClient == null) return
                sendMessage(messageJson)
                binding.edtMessage.text = null
            }

            R.id.btn_insert_image -> openLibraryAndroid()
        }
    }


    private fun sendMessage(messageJson: String) {
        Thread {
            try {
                val listClientSocket = httpClient?.listSocket ?: return@Thread
                listClientSocket.forEach { clientSocket ->
                    val out = PrintWriter(clientSocket.getOutputStream(), true)
                    out.println(messageJson)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showMessage() {
        messageViewModel.realMessage()
        messageViewModel.liveData.observe(this) { listMessage ->
            messageAdapter.setListMessage(listMessage as MutableList<Message>)
            binding.recyclerMessage.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun openLibraryAndroid() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        someActivityResultLauncher.launch(Intent.createChooser(intent, "file"))
    }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val byteArray = uriToByteArray(this, uri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            message.image = Base64.getEncoder().encodeToString(byteArray)
                        } else message.image = android.util.Base64.encodeToString(
                            byteArray, android.util.Base64.DEFAULT
                        )
                        message.message = null
                        messageViewModel.insertMessage(message)
                        val messageJson = Gson().toJson(message)
                        sendMessage(messageJson)
                    }
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }

    private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val buffer = ByteArrayOutputStream()
            val byteArray = ByteArray(1024)
            var bytesRead: Int
            if (inputStream != null) {
                while (inputStream.read(byteArray).also { bytesRead = it } != -1) {
                    buffer.write(byteArray, 0, bytesRead)
                }
            }
            return buffer.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

    private fun getTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("PERMISSION_GRANTED", "PERMISSION_GRANTED")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission has not been granted", Toast.LENGTH_SHORT)
        }
    }

    override fun onDestroy() {
        httpClient?.serverSocket?.close()
        super.onDestroy()
    }
}