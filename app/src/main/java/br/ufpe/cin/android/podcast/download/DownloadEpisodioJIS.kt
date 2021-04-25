package br.ufpe.cin.android.podcast.download

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.preference.PreferenceManager
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.dao.EpisodioDB
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.database.EpisodioRepository
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//[ITEM 7] - UTILIZAR O JOB INTENT SERVICE PARA DOWNLOAD + PROCESSAMENTO DO XML E ARMAZENAMENTO EM BD
class DownloadEpisodioJIS: JobIntentService() {

    companion object {
        private val JOB_ID = 1234
        val FINISH = "br.ufpe.cin.android.podcast.download.DownloadEpisodioJSI"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, DownloadEpisodioJIS::class.java, JOB_ID, intent)
        }
    }


    override fun onHandleWork(intent: Intent) {
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        val parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()

        //[ITEM 3.2] - CASO A URL DE SharedPreferences ESTEJA INDISPONÍVEL,
        // SETA O LINK INICIAL, PARA QUE A APLICAÇÃO NÃO QUEBRE
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                try {
                    parser.getChannel(linkRssFeed())
                } catch (e: Exception) {
                   // Toast.makeText(applicationContext, "Link RSS inválido!!", Toast.LENGTH_SHORT).show()
                    parser.getChannel(getString(R.string.link_inicial))
                }
            }

            val repositorio = EpisodioRepository(EpisodioDB.getInstance(applicationContext).dao())

            // Converto os 'articles' para 'episodios' e faço o insert (no repositório) utilizando viewModel
            channel.articles.forEach {

                val posicaoAudio = 0

                //[ITEM 5] - PERSISTE OS DADOS DOS EPISÓDIOS APÓS O DOWNLOAD
                repositorio.insert(Episodio(
                    it.link ?: "", it.title ?: "",
                    it.description ?: "", it.audio ?: "",
                    it.pubDate ?: "", posicaoAudio))
            }
            sendBroadcast(Intent(FINISH))
        }

    }

    //[ITEM 3.1] - URL PADRÃO, CASO NÃO TENHA NENHUM FEED ADICIONADO
    // Busca o link através da chave 'rssfeed', se não encontrar retorna o link default
    private fun linkRssFeed(): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("rssfeed", getString(R.string.link_inicial))!!
    }

}