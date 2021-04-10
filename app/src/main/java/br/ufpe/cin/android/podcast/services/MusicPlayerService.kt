package br.ufpe.cin.android.podcast.services

import android.R.attr.data
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.DownloadActivity
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.utils.*
import java.io.File


class MusicPlayerService : Service() {

    private lateinit var mediaPlayer : MediaPlayer

    override fun onCreate() {
        super.onCreate()
        //Cria o media player com o arquivo salvo na entidade episódio

        /*Log.i("MUSICA = ", music.toString())
        mediaPlayer = MediaPlayer.create(this, music)*/

        //Não vai tocar em loop
        mediaPlayer.isLooping = false
        //Quando terminar de tocar, vai parar o service
        mediaPlayer.setOnCompletionListener {
            stopSelf()
        }

        createChannel()

        //Permite ao usuário receber a notificação do serviço que está em execução no sistema
        //Ainda permite voltar à activity em que o serviço está sendo executado
        val intent = Intent(this, DownloadActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification : Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            .setOngoing(true)
            .setContentTitle("Service on going")
            .setContentText("Play music")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }



    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    fun playMusic(){
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
        }
    }

    fun pauseMusic(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return musicBuinder
    }

    private val musicBuinder : IBinder = MusicBinder()

    inner class MusicBinder : Binder(){
        val service : MusicPlayerService
            get() = this@MusicPlayerService

    }

    fun createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                VERBOSE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}