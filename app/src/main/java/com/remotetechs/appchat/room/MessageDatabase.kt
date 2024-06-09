package com.remotetechs.appchat.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.remotetechs.appchat.model.Message

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun dao(): MessageDao

    companion object {
        private var INSTANCE: MessageDatabase? = null
        fun getCalculateDatabase(context: Context): MessageDatabase {
            var instance = INSTANCE
            if (instance != null) {
                return instance
            }
            synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    "table_message"
                ).build()
                instance = newInstance
                return newInstance
            }
        }
    }
}