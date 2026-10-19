package com.tcglab.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HoldingEntity::class,
        DeckEntity::class,
        MetaEntity::class,
        ChatMessageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdings(): HoldingDao
    abstract fun decks(): DeckDao
    abstract fun meta(): MetaDao
    abstract fun chat(): ChatDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tcg_lab.db",
                ).build().also { instance = it }
            }
    }
}
