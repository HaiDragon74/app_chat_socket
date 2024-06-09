package com.remotetechs.appchatclient.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.remotetechs.appchatclient.model.Message

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun dao(): ChatDao

    companion object {
        private var INSTANCE: ChatDatabase? = null
        fun getCalculateDatabase(context: Context): ChatDatabase {
            var instance = INSTANCE
            if (instance != null) {
                return instance
            }
            synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "table_message"
                ).build()
                instance = newInstance
                return newInstance
            }
        }
    }
}