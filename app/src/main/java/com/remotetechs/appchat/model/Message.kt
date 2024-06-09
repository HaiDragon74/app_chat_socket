package com.remotetechs.appchat.model

import android.graphics.Bitmap
import android.icu.text.CaseMap.Title
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_message_server")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String? = null,
    var message: String? = null,
    var image: String? = null,
    var time: String? = null
)