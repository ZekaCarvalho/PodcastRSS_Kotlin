package br.ufpe.cin.android.podcast

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.EpisodioAdapter
import br.ufpe.cin.android.podcast.dao.EpisodioDB
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.database.EpisodioRepository
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.download.DownloadAudioJIS
import br.ufpe.cin.android.podcast.download.DownloadEpisodioJIS
import br.ufpe.cin.android.podcast.model.EpisodioVMFactory
import br.ufpe.cin.android.podcast.model.EpisodioViewModel
import br.ufpe.cin.android.podcast.player.PlayerAudioService


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val viewModel : EpisodioViewModel by viewModels{
        EpisodioVMFactory(
            EpisodioRepository(
                EpisodioDB.getInstance(this).dao()
            )
        )
    }

    private lateinit var adapter: EpisodioAdapter

    private var playerService : PlayerAudioService? = null
    private var isBound = false

    private val onDownloadComplete = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val listaDeEpisodios = mutableListOf<Episodio>()

            viewModel.episodios.value?.forEach{ //[ITEM 6] - O APP RECUPERA O FEED DO REPOSITÓRIO
                listaDeEpisodios.add(it)
            }

            //[ITEM 1] - UTILIZAÇÃO DE RECYCLEVIEW PARA LISTAR ELEMENTOS
            val rvItemFeed = binding.episodios

            rvItemFeed.apply{
                layoutManager = LinearLayoutManager(context) //LinearLayoutManager - Organização dos elementos
                addItemDecoration( //ItemDecoration - Orientação de disposição dos elementos.
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL) //Divisores para elementos verticalmente dispostos
                )
                adapter = EpisodioAdapter( //Adapter - Objeto que busca um elemento da coleção e renderiza
                    listaDeEpisodios,
                    layoutInflater,
                    playerService!!,
                    viewModel
                )
            }

            Toast.makeText(context, "Download Feed OK!", Toast.LENGTH_SHORT).show()
        }
    }

    private val onAudioDownloaded = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(context, "Download de áudio concluido", Toast.LENGTH_SHORT).show()
        }
    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
           isBound = true
            val binder = service as PlayerAudioService.AudioBinder
            playerService = binder.service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            playerService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startService(Intent(this, PlayerAudioService::class.java))

        adapter = EpisodioAdapter(mutableListOf(), layoutInflater, PlayerAudioService(), viewModel)

        DownloadEpisodioJIS.enqueueWork(this, Intent(this, DownloadEpisodioJIS::class.java))

        // Caso o viewModel observe qualquer alteração nos dados, ele notifica o adapter
        viewModel.episodios.observe(this,  Observer{ adapter.submitList(it.toList()) })

        if (!isBound) {
            playerServiceBind()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(onDownloadComplete, IntentFilter(DownloadEpisodioJIS.FINISH))
        registerReceiver(onAudioDownloaded, IntentFilter(DownloadAudioJIS.FINISH))
    }

    override fun onPause() {
        unregisterReceiver(onDownloadComplete)
        unregisterReceiver(onAudioDownloaded)
        super.onPause()
    }

    override fun onStop() {
        if (isBound){
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }

/*
    override fun onStart() {
        super.onStart()

        // Este trecho de código foi utilizado até o requisito 6. A partir do 7, ele foi movido e
        // adaptado download.DownloadEpisodioJIS
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                try {
                    parser.getChannel(linkRssFeed())
                } catch (e : Exception){
                    //fun Context.toast(message: CharSequence) = Toast.makeText(this, "Link RSS inválido!", Toast.LENGTH_LONG).show()
                    parser.getChannel(getString(R.string.link_inicial))
                }
            }

            val listaDeEpisodios = mutableListOf<Episodio>()

            // Converto os 'articles' para 'episodios' e faço o insert (no repositório) utilizando viewModel
            channel.articles.forEach{
                val ep = Episodio(it.link ?: "", it.title ?: "",
                    it.description ?: "", it.audio ?: "", it.pubDate ?: "")
                viewModel.insert(ep)
                listaDeEpisodios.add(ep)
            }
        }
    }*/


    //[ITEM 4] - CRIAÇÃO OptionsMenu PARA ALTERAR A SharedPreference
    // Mesmo conceito do onCreate de cima, só que para o menu.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_shared_preference, menu)
        return true
    }

    // Verifica se o item tem o id 'edit_shared_preference', para que PrefereciasActivity seja iniciada
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.edit_shared_preference -> {
                startActivity(Intent(this, PreferenciasActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun playerServiceBind(){
        val intent = Intent(this, PlayerAudioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }





}