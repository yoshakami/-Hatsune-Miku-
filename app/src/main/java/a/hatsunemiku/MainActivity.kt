package a.hatsunemiku

import a.hatsunemiku.ui.theme.HatsuneMikuTheme
import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent { HatsuneMikuApp() }
        setContentView(R.layout.activity_main) // Set your traditional XML layout
        findViewById<Button>(R.id.video1_button)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the Supabutton")
                PickVideo(R.id.video1_button)
            }
    }
}

@Composable
fun HatsuneMikuApp() {
    HatsuneMikuTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Your Composable UI elements go here
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
fun LaunchVideo(modifier: Int)
{
    val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        // val video1_uri: Uri = setDataSource(applicationContext, it)
        prepare()
        start()
    }
    mediaPlayer.start() // no need to call prepare(); create() does that for you
    //Greeting("Android")
    /*
                    }*/

}

fun PickVideo(modifier: Int) {

    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        // type = "application/pdf"
        type = "*/*"
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        // putExtra(DocumentsContract.EXTRA_INITIAL_URI, 0)
    }
    // startActivityForResult()
    // startActivityFromFragment(intent)
    //val activity: Activity = Activity()
    //var chooseFile: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

    /* chooseFile = Intent.createChooser(chooseFile, "Choose a file")
    startActivityForResult(activity, chooseFile)
    val uri: Uri = Uri.fromFile(data.toString())
    val src = uri.path */
}
fun PickVideo(activity: Activity, modifier: Int) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "video/*" // Set the type to video files
    }

    activity.startActivityForResult(intent, 0)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HatsuneMikuTheme {
        Greeting("Android")
    }
}