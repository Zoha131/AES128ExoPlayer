package bd.com.shohay.encryptedvideord

import android.net.Uri
import android.os.Bundle
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import bd.com.shohay.encryptedvideord.ui.theme.EncryptedVideoRDTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSource
import java.io.File
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.SecretKeySpec

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EncryptedVideoRDTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AndroidView(
                        factory = {
                            PlayerView(it)
                        },
                        update = { playerView ->

                            val key = "Mucj6bWTaQpOUj31A34kHVzPCPGkPI0u5FEQRGPB1Os="

                            val player = SimpleExoPlayer.Builder(this).build()
                            playerView.player = player

                            val dataSourceFactory: DataSource.Factory = EncryptedDataSourceFactory(key)
                            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()

                            val default = DefaultDataSourceFactory(this)
                            val source =
                            try {
                                val path = "android.resource://" + packageName + "/" + R.raw.prog_index
                                val uri = Uri.parse(path)
                                val mediaItem = MediaItem.fromUri(uri)
                                val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                                player.setMediaSource(videoSource)
                                player.prepare()
                                player.playWhenReady = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.height(400.dp).width(400.dp)
                    )
                }
            }
        }
    }
}

class EncryptedDataSourceFactory(
    private val key: String
) : DataSource.Factory {
    override fun createDataSource(): EncryptedDataSource =
        EncryptedDataSource(key)
}

class EncryptedDataSource(private val key: String) : DataSource {
    private var inputStream: CipherInputStream? = null
    private lateinit var uri: Uri

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        try {
            val file = File(uri.path)
            val skeySpec = SecretKeySpec(key.toByteArray(), KeyProperties.KEY_ALGORITHM_AES)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            inputStream = CipherInputStream(file.inputStream(), cipher)
        } catch (e: Exception) {

        }
        return dataSpec.length
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int =
        if (readLength == 0) {
            0
        } else {
            inputStream?.read(buffer, offset, readLength) ?: 0
        }

    override fun getUri(): Uri? =
        uri

    @Throws(IOException::class)
    override fun close() {
        inputStream?.close()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EncryptedVideoRDTheme {
        Greeting("Android")
    }
}