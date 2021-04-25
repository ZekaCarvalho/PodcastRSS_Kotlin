package br.ufpe.cin.android.podcast.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ufpe.cin.android.podcast.data.Episodio


@Database(entities = [Episodio::class], version = 1)
abstract class EpisodioDB : RoomDatabase(){

    abstract fun dao(): EpisodioDAO

    companion object{
        @Volatile
        private var INSTANCE : EpisodioDB? = null
        fun getInstance(c: Context) : EpisodioDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    c.applicationContext,
                    EpisodioDB::class.java,
                    "episodios.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}