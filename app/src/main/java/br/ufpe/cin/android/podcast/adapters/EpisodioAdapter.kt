package br.ufpe.cin.android.podcast.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding
import br.ufpe.cin.android.podcast.model.EpisodioViewModel
import br.ufpe.cin.android.podcast.player.PlayerAudioService
import com.prof.rssparser.Article


class EpisodioAdapter (
    private val listaEpisodios: List<Episodio>,
    private val inflater : LayoutInflater,
    private val pas: PlayerAudioService,
    private val vm : EpisodioViewModel) :
    ListAdapter<Episodio, EpisodioViewHolder>(Differ)

{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodioViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        return EpisodioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodioViewHolder, position: Int) {
       holder.bindTo(listaEpisodios[position], pas, vm)
    }

    override fun getItemCount(): Int = listaEpisodios.size


    private object Differ: DiffUtil.ItemCallback<Episodio>() {
        override fun areItemsTheSame(oldItem: Episodio, newItem: Episodio): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Episodio, newItem: Episodio): Boolean {
            return (oldItem.titulo == newItem.titulo) &&
                    (oldItem.linkEpisodio == newItem.linkEpisodio) &&
                    (oldItem.descricao == newItem.descricao) &&
                    (oldItem.linkArquivo == newItem.linkArquivo) &&
                    (oldItem.dataPublicacao == newItem.dataPublicacao)
        }

    }

}
