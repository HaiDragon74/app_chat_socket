package com.remotetechs.appchatclient.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.CursorWindow
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.remotetechs.appchatclient.adapter.MessageAdapter
import com.remotetechs.appchatclient.databinding.FragmentMessageBinding
import com.remotetechs.appchatclient.model.Message
import com.remotetechs.appchatclient.room.ChatDatabase
import com.remotetechs.appchatclient.server.HttpClient
import com.remotetechs.appchatclient.viewmodel.MessageViewModel
import com.remotetechs.appchatclient.viewmodel.MessageViewModelFactory
import com.remotetechs.appchatclient.viewmodel.NotificationViewModel
import com.remotetechs.appchatclient.viewmodel.Repository
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
class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageViewModel: MessageViewModel
    private var listMessage = mutableListOf<Message>()

    companion object {
        const val SERVER_PORT = 8889
        var ipServer:String=""
    }

    private var httpClient: HttpClient? = null
    private lateinit var handler: Handler
    private lateinit var thread: Thread
    private var message = Message()
    private var nameUser: String = ""

    @Inject
    lateinit var notificationViewModel: NotificationViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        handler = Handler(Looper.getMainLooper())
        thread = Thread()
        val messageViewModelFactory =
            MessageViewModelFactory(Repository(ChatDatabase.getCalculateDatabase(requireActivity())))
        messageViewModel = ViewModelProvider(
            requireActivity(), messageViewModelFactory
        )[MessageViewModel::class.java]
        binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        binding.recyclerMessageReceiver.layoutManager = LinearLayoutManager(requireActivity())
        (binding.recyclerMessageReceiver.layoutManager as LinearLayoutManager).stackFromEnd = true
        messageAdapter = MessageAdapter(messageViewModel, requireActivity())
        binding.recyclerMessageReceiver.adapter = messageAdapter
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameUser = arguments?.getString("name_user") ?: return
        ipServer = arguments?.getString("ip_server") ?: return
        messageViewModel.checkName(nameUser)
        message.name = nameUser
        message.ip = getLocalIpAddress()
        if (httpClient == null) {
            httpClient = HttpClient(requireActivity(), notificationViewModel, messageViewModel)
            thread = Thread(httpClient)
            thread.start()
        }
        binding.btnInsertImage.setOnClickListener {
            openLibraryAndroid()
        }
        onClick()
        showMessage()
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
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
            Toast.makeText(
                requireActivity(),
                "Notifications permission granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireActivity(),
                "Notification permission has not been granted",
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun onClick() {
        binding.btnSendData.setOnClickListener {
            val clientMessage = binding.edtMessageUser.text.toString().trim()
            message.message = clientMessage
            message.time = getTime()
            val messageJson = Gson().toJson(message)
            messageViewModel.insertMessage(message)
            sendMessage(messageJson)
            binding.edtMessageUser.text = null
            message.image = null
        }
    }

    private fun sendMessage(messageJson: String) {
        Thread {
            try {
                val socket = httpClient?.socket ?: return@Thread
                val outputStream = socket.getOutputStream()
                val out = PrintWriter(outputStream, true)
                out.println(messageJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun getTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun showMessage() {
        messageViewModel.realMessage()
        messageViewModel.liveData.observe(requireActivity()) { listMessage ->
            messageAdapter.setListMessage(listMessage as MutableList<Message>)
            binding.recyclerMessageReceiver.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun openLibraryAndroid() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        someActivityResultLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"))
    }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val byteArray = uriToByteArray(requireActivity(), uri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            message.image = Base64.getEncoder().encodeToString(byteArray)
                        } else
                            message.image = android.util.Base64.encodeToString(
                                byteArray,
                                android.util.Base64.DEFAULT
                            )
                        message.message = null
                        message.time = getTime()
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

    private fun getLocalIpAddress(): String {
        val wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress: Int = wifiInfo.ipAddress
        val ipAddressString = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )

        return ipAddressString
    }

    override fun onDestroy() {
        httpClient?.socket?.close()
        super.onDestroy()
    }
}