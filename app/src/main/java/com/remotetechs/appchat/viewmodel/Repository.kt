package com.remotetechs.appchat.viewmodel

import com.remotetechs.appchat.model.Message
import com.remotetechs.appchat.room.MessageDatabase
import kotlinx.coroutines.flow.Flow

class Repository(private val messageDatabase: MessageDatabase) {
    suspend fun insertMessage(message: Message) {
        messageDatabase.dao().insertMessage(message)
    }

    fun realMessage(): Flow<List<Message>> {
        return messageDatabase.dao().realMessage()
    }

}