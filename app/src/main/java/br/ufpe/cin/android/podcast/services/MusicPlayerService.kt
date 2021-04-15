package br.ufpe.cin.android.podcast.services

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.utils.*
import java.io.File


class MusicPlayerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var pos = 0


    override fun onCreate() {
        super.onCreate()

        createChannel()

        //Permite ao usuário receber a notificação do serviço que está em execução no sistema
        //Ainda permite voltar à activity em que o serviço está sendo executado
        val intent = Intent(this, EpisodeDetailActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            .setOngoing(true)
            .setContentTitle("Service on going")
            .setContentText("Play Episode")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    //Este método vai iniciar o service a partir de um comando.
    // Pegará o valor passado pela intent ao clicar no botão play
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audio = intent?.getStringExtra("audio").toString()
        Log.i("MusicPlayerService", audio)

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(audio)
        mediaPlayer.prepare()
        mediaPlayer.start()

        mediaPlayer.isLooping = false

        //Ao finalizar o episódio, apaga o arquivo na pasta local
        mediaPlayer.setOnCompletionListener {
            val file = File(audio)
            file.delete()
        }

        //Não vai ser reiniciado automaticamenyr caso o service seja interrompido
        return START_NOT_STICKY
    }

    //Vai tocar o episódio caso ele ainda não esteja
    //Caso tenha sido pausado, começará de onde parou
    fun playMusic() {
        if (!mediaPlayer.isPlaying && pos == 0) {
            mediaPlayer.start()
        } else if (pos > 0){
            mediaPlayer.seekTo(pos)
        }
    }

    //Vai pausar o episódio e pegar a posição
    fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
           pos = mediaPlayer.currentPosition
        }
    }

    //Vai voltar o episódio do começo
    fun rewindMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(0)
        }
    }

    fun stopMusic(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
    }



    /*override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }*/

    override fun onBind(intent: Intent): IBinder {
        return musicBuinder
    }

    private val musicBuinder: IBinder = MusicBinder()

    inner class MusicBinder : Binder() {
        val service: MusicPlayerService
            get() = this@MusicPlayerService

    }

    //Cria no canal de notificação do sistema do usuário,
    // onde ele vai poder gerenciar o play do aplicativo na sua central de notificações
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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