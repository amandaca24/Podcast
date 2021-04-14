package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.ufpe.cin.android.podcast.utils.KEY_IMAGEFILE_URI
import br.ufpe.cin.android.podcast.utils.KEY_LINK_URI
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadEpisodeWorker(c : Context, params: WorkerParameters) : Worker(c,params) {

    override fun doWork(): Result {
        try {
            //pegando o campo associado com KEY_LINK_URI dos dados passados como entrada
            val resourceUri = Uri.parse(inputData.getString(KEY_LINK_URI))
            Log.i("RESOURCE URI = ", resourceUri.toString())

            val root =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            root.mkdirs()

            val output = File(root, resourceUri.lastPathSegment)
            if (output.exists()) {
                output.delete()
            }
            Log.i("ARQUIVO DOWNLOAD = ", output.toString())
            Log.i("LINK DOWNLOAD", resourceUri.toString())
            val url = URL(resourceUri.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = 0
                while (`in`.read(buffer).also { len = it } >= 0) {
                    out.write(buffer, 0, len)
                }
                out.flush()
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            //se chegou aqui, deu tudo certo
            val outputData = workDataOf(KEY_IMAGEFILE_URI to output.absolutePath)
            return Result.success(outputData)

        } catch (e: IOException) {
            Log.e(javaClass.name, "Download Exception", e)
            return Result.failure()
        }
    }
}