package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.dao.EpisodioDB
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.database.EpisodioRepository
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding
import br.ufpe.cin.android.podcast.download.DownloadAudioJIS
import br.ufpe.cin.android.podcast.model.EpisodioVMFactory
import br.ufpe.cin.android.podcast.model.EpisodioViewModel
import br.ufpe.cin.android.podcast.player.PlayerAudioService
import com.prof.rssparser.Article

class EpisodioViewHolder(private val binding: ItemfeedBinding) :
RecyclerView.ViewHolder(binding.root)
{
    var description = "DESCRIÇÃO - Houve um problema, a descrição não foi carregada"
    var linkArq = "LINK - Houve um problema, o link não foi carregado"
    var title = "TITULO - Houve um problema, o título não foi carregado"
    var linkEpisodio = "link Episodio"
    var dataPublicacao = "data de publicação"
    var posicaoAudio = 0

    lateinit var playerAudioService: PlayerAudioService
    lateinit var viewModel : EpisodioViewModel


    init {

        title = binding.itemTitle.text.toString()

        //[ITEM 2] - DIRECIONAR PARA EpisodeDetailActivity E EXIBIR DETALHES DO EPISÓDIO
        binding.root.setOnClickListener {
            val context = binding.itemTitle.context
            val intent = Intent(context, EpisodeDetailActivity::class.java)

            //Passa o valor de description pelo Intent
            intent.putExtra("description", description)
            intent.putExtra("link", linkArq)

            context.startActivity(intent)
        }

        binding.itemAction.setOnClickListener{
            val context = binding.itemAction.context

            val l = this.linkArq

            //[ITEM 8] - QUANDO O BOTÃO DE DOWNLOAD DE UM EPISÓDIO FOR CLICADO,
            // DELEGAR O DOWNLOAD DO ARQUIVO .MP3 PARA UM JOB INTENT SERVICE
            if( validURL(l) ){
                val intent = Intent(context, DownloadAudioJIS::class.java)

                intent.data = Uri.parse(l)
                intent.putExtra("title", title)

                Toast.makeText(context, "Download do áudio iniciado", Toast.LENGTH_SHORT).show()

                DownloadAudioJIS.enqueueWork(context, intent)

            }

            //[ITEM 10] - DISPARAR SERVICE QUE TOCA O EPISÓDIO
            // O PLAYER É RESPONSÁVEL POR GERENCIAR O INSTATE DE PLAY/PAUSE
            else if ( l.isNotEmpty() && l.isNotBlank()){
                        playerAudioService?.playPause(l, posicaoAudio)

                        if(playerAudioService?.isPlaying() == false){
                            val p = playerAudioService.audioPositionWhenReproducing()

                            if (p > 0 && p != null){
                                val episodioSalvo = Episodio( this.linkEpisodio, this.title, this.description,
                                    this.linkArq, this.dataPublicacao, this.posicaoAudio
                                )
                                viewModel.update(episodioSalvo)
                            }
                        }
                    }
        }
    }

    fun bindTo(ep: Episodio, pas: PlayerAudioService, vm : EpisodioViewModel) {
        binding.itemTitle.text = ep.titulo
        binding.itemDate.text = ep.dataPublicacao

        this.description = ep.descricao
        this.linkArq = ep.linkArquivo
        this.title = ep.titulo
        this.linkEpisodio = ep.linkEpisodio
        this.dataPublicacao = ep.dataPublicacao
        this.posicaoAudio = ep.posicaoAudio

        this.playerAudioService = pas
        this.viewModel = vm

        //[ITEM 9] - MUDAR O ÍCONE
        if (!validURL(ep.linkArquivo) ){
            binding.itemAction.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(binding.itemAction.context.resources,
                R.drawable.iconfinder_play, null),
                null,
                null,
                null
            )
            binding.itemAction.text = ""
        }
    }

    private fun validURL(url: String): Boolean {
        return (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))
    }


}