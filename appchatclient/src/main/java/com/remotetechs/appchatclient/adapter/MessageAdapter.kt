package com.remotetechs.appchatclient.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.manager.Lifecycle
import com.remotetechs.appchatclient.R
import com.remotetechs.appchatclient.fragment.MessageFragment
import com.remotetechs.appchatclient.model.Message
import com.remotetechs.appchatclient.viewmodel.MessageViewModel
import java.util.Base64

class MessageAdapter(private val messageViewModel: MessageViewModel,private val lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<ViewHolder>() {
    private val TYPE_USER = 1
    private val TYPE_RECEIVER = 2
    private var listMessage: MutableList<Message> = mutableListOf()
    fun setListMessage(listMessage: MutableList<Message>) {
        this.listMessage = listMessage
        notifyDataSetChanged()
    }

    fun addMessage(mess: Message) {
        this.listMessage.add(mess)
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : ViewHolder(itemView) {
        private val tvItemMessageUser: TextView = itemView.findViewById(R.id.tv_item_message_user)
        private val tvItemNameUser: TextView = itemView.findViewById(R.id.tv_item_name_user)
        private val imgMSendUser: ImageView = itemView.findViewById(R.id.img_send_user)
        private val linearItemMessageUser: LinearLayout = itemView.findViewById(R.id.linear_item_message_user)
        private val tvTimeItemMessageUser: TextView = itemView.findViewById(R.id.tv_time_item_message_user)

        fun bind(item: Message) {
            if (item.name==""){
                tvItemNameUser.visibility=View.GONE
            }else
                tvItemNameUser.visibility=View.VISIBLE
            if (item.image == null) {
                imgMSendUser.visibility = View.GONE
            }else{
                imgMSendUser.visibility = View.VISIBLE
            }
            if (item.message==null){
                tvItemMessageUser.visibility=View.GONE
                linearItemMessageUser.visibility=View.GONE
                tvItemNameUser.visibility=View.GONE
            }else{
                tvItemMessageUser.visibility=View.VISIBLE
                linearItemMessageUser.visibility=View.VISIBLE
                tvItemNameUser.visibility=View.GONE
            }
            Glide.with(itemView.context).load(item.image?.let { base64ToByteArray(it) }).into(imgMSendUser)
            tvItemMessageUser.text = item.message
            tvItemNameUser.text=item.name
            tvTimeItemMessageUser.text=item.time
        }
        private fun base64ToByteArray(base64String: String): ByteArray {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getDecoder().decode(base64String)
            } else {
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            }
        }
    }

    class ReceiverViewHolder(itemView: View) : ViewHolder(itemView) {
        private val tvItemMessageReceiver: TextView = itemView.findViewById(R.id.tv_item_message_receiver)
        private val tvItemNameReceiver: TextView = itemView.findViewById(R.id.tv_item_name_receiver)
        private val imgSendReceiver: ImageView = itemView.findViewById(R.id.img_send_receiver)
        private val tvTimeItemMessageReceiver: TextView = itemView.findViewById(R.id.tv_time_item_message_receiver)

        fun bind(item: Message) {
            if (item.name==""){
                tvItemNameReceiver.visibility=View.GONE
            }else
                tvItemNameReceiver.visibility=View.VISIBLE
            if (item.image == null) {
                imgSendReceiver.visibility = View.GONE
            }else
                imgSendReceiver.visibility = View.VISIBLE
            if (item.message==null){
                tvItemMessageReceiver.visibility=View.GONE
            }else{
                tvItemMessageReceiver.visibility=View.VISIBLE
            }
            Glide.with(itemView.context).load(item.image?.let { base64ToByteArray(it) }).into(imgSendReceiver)
            tvItemMessageReceiver.text = item.message
            tvItemNameReceiver.text=item.name
            tvTimeItemMessageReceiver.text=item.time
        }
        private fun base64ToByteArray(base64String: String): ByteArray {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getDecoder().decode(base64String)
            } else {
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var nameUser=""
        messageViewModel.liveDataName.observe(lifecycleOwner){
            nameUser=it
        }
        return if (listMessage.getOrNull(position)?.name ==nameUser) {
            TYPE_USER
        } else TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewUser =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        val viewReceiver =
            LayoutInflater.from(parent.context).inflate(R.layout.item_receiver, parent, false)
        return if (viewType == TYPE_USER) {
            UserViewHolder(viewUser)
        } else ReceiverViewHolder(viewReceiver)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as? UserViewHolder)?.bind(listMessage[position])
        (holder as? ReceiverViewHolder)?.bind(listMessage[position])
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }
}