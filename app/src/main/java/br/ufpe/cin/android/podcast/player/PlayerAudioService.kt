package br.ufpe.cin.android.podcast.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.MainActivity
import br.ufpe.cin.android.podcast.R
import java.io.File

class PlayerAudioService : Service() {

    private lateinit var mediaPlayer : MediaPlayer
    private val audioBinder : IBinder = AudioBinder()
    private lateinit var ultimoCaminhoArquivo: String
    private var posicaoAudioATUAL : Int = 0

    companion object {
        const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
        const val NOTIFICATION_ID = 1
        const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Mostra notificação sempre que começa a reproduzir"
        const val VERBOSE_NOTIFICATION_CHANNEL_NAME = "Notificação detalhada"
    }


    override fun onCreate() {
        super.onCreate()

        ultimoCaminhoArquivo = ""

        mediaPlayer = MediaPlayer()


        //[ITEM 11] - APAGA O ARQUIVO QUANDO TERMINA DE TOCAR
        mediaPlayer.setOnCompletionListener {
            File(ultimoCaminhoArquivo).delete()
            //PRECISO ATUALIZAR O REPOSITÓRIO??? (Informando que apaguei o arquivo),
            // acho que sim, mas vai ficar para a próxima realease.
        }

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){

            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                VERBOSE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.iconfinder_play)
            .setOngoing(true)
            .setContentTitle("Serviço de música executando")
            .setContentText("Clique para voltar à lista de episódios")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //Não vai ser reiniciado automaticamente caso o service seja interrompido
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()

    }

    override fun onBind(intent: Intent): IBinder {
        return audioBinder
    }

    inner class AudioBinder: Binder(){
        val service : PlayerAudioService
        get() = this@PlayerAudioService
    }

    fun playPause(caminhoArquivo: String, posicaoAudio: Int){

        if (!mediaPlayer.isPlaying){
            if ( caminhoArquivo != ultimoCaminhoArquivo){
                //reseto para que não reclame das operações abaixo
                mediaPlayer.reset()

                mediaPlayer.setDataSource(caminhoArquivo)
                mediaPlayer.prepare()

                ultimoCaminhoArquivo = caminhoArquivo
            }

            mediaPlayer.start()

            if (posicaoAudio > 0){
                mediaPlayer.seekTo(posicaoAudio)
            }
        } else {
            posicaoAudioATUAL = mediaPlayer.currentPosition
            mediaPlayer.pause()
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun audioPositionWhenReproducing(): Int {
        return posicaoAudioATUAL
    }

}