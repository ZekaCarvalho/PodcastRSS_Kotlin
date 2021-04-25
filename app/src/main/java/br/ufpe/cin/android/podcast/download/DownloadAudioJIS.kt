package br.ufpe.cin.android.podcast.download

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.app.JobIntentService
import br.ufpe.cin.android.podcast.dao.EpisodioDB
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.database.EpisodioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadAudioJIS: JobIntentService() {
    companion object {
        private val JOB_ID = 4321
        val FINISH = "br.ufpe.cin.android.podcast.download.DownloadAudioJIS"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, DownloadAudioJIS::class.java, JOB_ID, intent)
        }
    }

   //Copiado da aula
    override fun onHandleWork(intent: Intent) {
        try {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            root.mkdirs()

            val intentData = intent.data
            val output = File(root, intentData!!.lastPathSegment)

            if (output.exists()) {
                output.delete()
            }

            val url = URL(intentData.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)

            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len: Int
                while (`in`.read(buffer).also { len = it } >= 0) {
                    out.write(buffer, 0, len)
                }

                out.flush()
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            // O requisito 8 especificava que o BD deveria ser atualizado...
            val scope = CoroutineScope(Dispatchers.Main.immediate)

            scope.launch {
                val repositorio = EpisodioRepository(EpisodioDB.getInstance(applicationContext).dao())

                val ep = repositorio.searchByTitle(intent.getStringExtra("title")!!)

                repositorio.update(
                    Episodio(ep!!.linkEpisodio, ep.titulo, ep.descricao, output.path, ep.dataPublicacao, ep.posicaoAudio)
                )
            }

            sendBroadcast(Intent(FINISH))

        } catch (e: IOException) {
            Log.e(javaClass.name, "ERRO: Algo deu errado durante download", e)
        }
    }
}