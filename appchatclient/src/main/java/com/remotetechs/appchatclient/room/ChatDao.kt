package com.remotetechs.appchatclient.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.remotetechs.appchatclient.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    @Query("SELECT * FROM table_message")
    fun realMessage():Flow<List<Message>>
}