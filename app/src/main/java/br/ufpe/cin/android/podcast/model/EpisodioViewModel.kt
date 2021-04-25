package br.ufpe.cin.android.podcast.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.database.EpisodioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
*  Armazena e gerencia dados relacionados à IU considerando o ciclo de vida.
*  A classe ViewModel permite que os dados sobrevivam às mudanças de configuração,
*  como a rotação da tela.
* */

/*
 * O ViewModel deve ser utilizado implementando o padrão Factory
 *  */
class EpisodioViewModel(val repositorio : EpisodioRepository) : ViewModel() {
    val episodios = repositorio.allEpisodios()

    fun insert(ep: Episodio){
        // Informo que estarei rodando esse método dentro do escopo de um corrotina e
        // que será uma operação de IO
        viewModelScope.launch(Dispatchers.IO){
            repositorio.insert(ep)
        }
    }

    fun update(ep: Episodio){
        viewModelScope.launch(Dispatchers.IO){
            repositorio.update(ep)
        }
    }

    fun delete(ep: Episodio){
        viewModelScope.launch(Dispatchers.IO){
            repositorio.delete(ep)
        }
    }

    fun searchByTitle(title: String) : Episodio? {
        var ep : Episodio? = null
        viewModelScope.launch(Dispatchers.IO){
            ep = repositorio.searchByTitle(title)
        }
        return ep
    }

}