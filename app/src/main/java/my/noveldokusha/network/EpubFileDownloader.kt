package my.noveldokusha.network
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import my.noveldokusha.services.EpubImportService
import my.noveldokusha.utils.NotificationsCenter
import my.noveldokusha.utils.title
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class EpubFileDownloader {
    val notificationId = "DownloadingPdf".hashCode();
    val channelId = "Downloading file ";
    val channelName = "Download Pdf";
    var notificationTitle = "Downloading Ashara PDFs";


    suspend fun downloadAndSaveEpubFile(context: Context) {
        val epubUrls =
            arrayOf(
                "https://api.icladdis.com/EpubSsaved in truststore /home/gsharew/.config/Google/AndroidStudimallSizes/Amhara.epub",
                "https://api.icladdis.com/EpubSmallSizes/Tigrigna.epub",
//                "https://api.icladdis.com/EpubSmallSizes/Afar.epub",
                "https://api.icladdis.com/EpubSmallSizes/English.epub",
                "https://api.icladdis.com/EpubSmallSizes/Oromia.epub",
//                "https://api.icladdis.com/EpubSmallSizes/Sidama.epub",
//                "https://api.icladdis.com/EpubSmallSizes/Somalia.epub",
            );
        showImportNotification(context);
        for (url: String in epubUrls) {
            val urlPath = withContext(Dispatchers.IO) {
                URLDecoder.decode(URL(url).path, "UTF-8")
            }
            val originalFileName = urlPath.substring(urlPath.lastIndexOf('/') + 1)
            val file = File(context.getExternalFilesDir(null), originalFileName);
            val fileExists = file.exists();
//            Timber.tag("original names is : ").i(originalFileName);
            if (!fileExists) {
                coroutineScope {
                    withContext(Dispatchers.Default) {
                        downloadNow(
                            context,
                            url,
                            originalFileName
                        )
                    }
                }
            }
        }

//        NotificationsCenter(context).close(notificationId);

    }

    private suspend fun downloadNow(context: Context, epubUrl: String, filename: String) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(300000, TimeUnit.MILLISECONDS) // Connection timeout
                    .readTimeout(300000, TimeUnit.MILLISECONDS)    // Read timeout
                    .writeTimeout(300000, TimeUnit.MILLISECONDS)   // Write timeout
                    .build();
                val request = Request.Builder()
                    .url(epubUrl)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val file = File(context.getExternalFilesDir(null), filename);
//                    Timber.tag("uri is ").i(file.absolutePath);

                        val outputStream = FileOutputStream(file)
                        val inputStream = response.body.byteStream()
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output, bufferSize = 8192)
                            }
                        }
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                        EpubImportService.start(ctx = context, uri = getFileUri(context, file));
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        // Handle failure
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //// Function to get the URI for a file using FileProvider
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context.applicationContext,
            context.packageName + ".provider",
            file
        )
    }


    private fun showImportNotification(context: Context) {
        NotificationsCenter(context).showNotification(
            notificationId = notificationId,
            channelId = channelId,
            channelName = channelName,
        ) {
            setProgress(100, 0, true)
            title = notificationTitle
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }
    }
}