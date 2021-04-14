package br.ufpe.cin.android.podcast.services

import android.app.*
import android.content.Context
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
import java.io.FileInputStream


class MusicPlayerService : Service() {

    private lateinit var mediaPlayer : MediaPlayer
    private var startNum = 0


    override fun onCreate() {
        super.onCreate()

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNum ++
        var audio = intent?.getStringExtra("audio").toString()
        var filePath = FileInputStream(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).path+"/"+ audio)
        Log.i("MusicPlayerService", filePath.toString())
        //mediaPlayer = MediaPlayer.create(this, filePath.fd);
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(filePath.fd);
        mediaPlayer.prepare();
        mediaPlayer.start()

        //Sinaliza o que fazer caso o service seja interrompido pelo sistema
        //START_NOT_STICKY - não é reiniciado automaticamente
        //START_STICKY - vai ser reiniciado automaticamente assim que possível, com intent nulo
        //START_REDELIVER_INTENT - vai ser reiniciado automaticamente assim que possível, com último intent usado para comando start
        return START_NOT_STICKY
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

    fun rewind() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(0)
        }
    }

    /*override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }*/

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