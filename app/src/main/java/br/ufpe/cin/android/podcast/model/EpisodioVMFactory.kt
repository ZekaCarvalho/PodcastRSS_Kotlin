package br.ufpe.cin.android.podcast.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ufpe.cin.android.podcast.database.EpisodioRepository

class EpisodioVMFactory(private val repositorio: EpisodioRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(EpisodioViewModel::class.java)) {
            return EpisodioViewModel(repositorio) as T
        }

         throw IllegalArgumentException("ViewModel não encontrado!")
    }

}