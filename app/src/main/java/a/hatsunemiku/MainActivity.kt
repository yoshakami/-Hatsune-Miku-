package a.hatsunemiku

import a.hatsunemiku.ui.theme.HatsuneMikuTheme
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private var videoPosition: Int = 0 // Variable to store video position

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view based on the initial orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.portrait)
        } else {
            setContentView(R.layout.landscape)
        }

        findViewById<Button>(R.id.video1_button).setOnClickListener {
            Log.d("BUTTONS", "User tapped the Supabutton")
            PickVideo(0)
        }
        videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedVideoUri: Uri? = data.data
                    if (selectedVideoUri != null) {
                        // Now you can do something with the selected video URI
                        Log.d("PICK_VIDEO", "Selected video URI: $selectedVideoUri")

                        // Example: Display the video in a VideoView
                        val videoView = findViewById<VideoView>(R.id.video1_view)
                        videoView.visibility = VISIBLE;
                        videoView.setVideoURI(selectedVideoUri)
                        videoView.start()
                    } else {
                        // Handle the case where no video URI was selected
                        Log.d("PICK_VIDEO", "No video URI selected")
                    }
                }
            }
        }
    }
    fun PickVideo(buttonId: Int) {
        val videoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoPickerLauncher.launch(videoPickerIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current position of the video
        outState.putInt("videoPosition", videoPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore the saved position of the video
        videoPosition = savedInstanceState.getInt("videoPosition")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
            // Save the current video position before changing the layout
            videoPosition = getCurrentVideoPosition()

            // Set the new layout based on orientation
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setContentView(R.layout.portrait)
            } else {
                setContentView(R.layout.landscape)
            }

            // Restore the video position after changing the layout
            restoreVideoPosition()

            findViewById<Button>(R.id.video1_button).setOnClickListener {
                android.util.Log.d("BUTTONS", "User tapped the Supabutton")
                PickVideo(0)
            }
    }

    // Helper method to get the current position of the video
    private fun getCurrentVideoPosition(): Int {
        // Implement this based on your video view, for example:
        // return videoView.currentPosition
        return 0
    }

    // Helper method to restore the video position
    private fun restoreVideoPosition() {
        // Implement this based on your video view, for example:
        findViewById<VideoView>(R.id.video1_view).seekTo(videoPosition)
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

fun PickVideo(modifier: Int, activity: Activity) {
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