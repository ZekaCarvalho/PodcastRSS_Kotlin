package br.ufpe.cin.android.podcast.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import br.ufpe.cin.android.podcast.dao.EpisodioDAO
import br.ufpe.cin.android.podcast.data.Episodio

class EpisodioRepository(private val dao: EpisodioDAO) {

    @WorkerThread
    suspend fun insert(ep: Episodio) {
        dao.insert(ep)
    }

    @WorkerThread
    suspend fun update(ep: Episodio) {
        dao.update(ep)
    }

    @WorkerThread
    suspend fun delete(ep: Episodio) {
        dao.delete(ep)
    }

    @WorkerThread
     fun allEpisodios(): LiveData<List<Episodio>> {
        return dao.allEpisodios()
    }

    @WorkerThread
    suspend fun searchByTitle(title: String): Episodio? {
        return dao.searchByTitle(title)
    }
}