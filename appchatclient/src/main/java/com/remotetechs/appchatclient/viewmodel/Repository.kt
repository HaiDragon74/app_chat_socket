package com.remotetechs.appchatclient.viewmodel

import com.remotetechs.appchatclient.model.Message
import com.remotetechs.appchatclient.room.ChatDatabase
import kotlinx.coroutines.flow.Flow

class Repository(private val chatDatabase: ChatDatabase) {
    suspend fun insertMessage(message: Message){
        chatDatabase.dao().insertMessage(message)
    }
    fun realMessage():Flow<List<Message>>{
        return chatDatabase.dao().realMessage()
    }

}