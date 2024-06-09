package com.remotetechs.appchatclient.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_message")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    var name:String?=null,
    var message: String?=null,
    var image:String?=null,
    var ip: String? = null,
    var time:String?=null
)