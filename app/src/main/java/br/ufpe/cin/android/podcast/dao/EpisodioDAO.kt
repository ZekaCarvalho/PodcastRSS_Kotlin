package br.ufpe.cin.android.podcast.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.Episodio

/*
* Regras para manipulação da base de dados
* */
@Dao
interface EpisodioDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Seria mais adequado usar REPLACE ?
    suspend fun insert(ep: Episodio)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(ep: Episodio)

    @Delete
    suspend fun delete(ep: Episodio)

    @Query("SELECT * FROM episodios")
    fun allEpisodios(): LiveData<List<Episodio>>

    @Query("SELECT * FROM episodios WHERE titulo LIKE :title")
    suspend fun searchByTitle(title: String): Episodio?

}