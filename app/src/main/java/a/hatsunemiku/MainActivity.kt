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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

import android.content.SharedPreferences
import android.view.View.GONE

class MainActivity : ComponentActivity() {

    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private var videoPosition: Int = 0 // Variable to store video position
    private var isVideoPlaying: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    // Use this constant for the storage permission request code
    private val STORAGE_PERMISSION_REQUEST_CODE = 123

    // Use this variable to track if the permission has been granted
    private var storagePermissionGranted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if the app has storage permissions
        if (checkStoragePermission()) {
            // Storage permission is already granted
            storagePermissionGranted = true
            // Continue with your app initialization or functionality
        } else {
            // Request storage permission
            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Set the content view based on the initial orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.portrait)
        } else {
            setContentView(R.layout.landscape)
        }

        val videoView = findViewById<VideoView>(R.id.video1_view)

        findViewById<Button>(R.id.video1_button).setOnClickListener {
            Log.d("BUTTONS", "User tapped the Supabutton")
            PickVideo(0)
        }
        // Set up SharedPreferences
        sharedPreferences = getPreferences(MODE_PRIVATE)

        if (storagePermissionGranted)
        {
            // Retrieve saved video URI
            val savedVideoUriString = sharedPreferences.getString("videoUri", null)
            if (savedVideoUriString != null) {
                val savedVideoUri = Uri.parse(savedVideoUriString)
                videoView.visibility = VideoView.VISIBLE
                videoView.setVideoURI(savedVideoUri)
                isVideoPlaying = true
                videoView.start()
            }
        }



        videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedVideoUri: Uri? = data.data
                    if (selectedVideoUri != null) {
                        Log.d("PICK_VIDEO", "Selected video URI: $selectedVideoUri")
                        // Save the selected video URI
                        saveVideoUri(selectedVideoUri)
                        videoView.visibility = VideoView.VISIBLE
                        videoView.setVideoURI(selectedVideoUri)
                        isVideoPlaying = true
                        videoView.start()
                    } else {
                        Log.d("PICK_VIDEO", "No video URI selected")
                    }
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions are automatically granted on versions lower than Marshmallow
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
                storagePermissionGranted = true
                // Continue with your app initialization or functionality
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Storage permission is required for the app to function properly.",
                    Toast.LENGTH_SHORT
                ).show()
                // Handle the case where the user denied the permission
            }
        }

    private fun saveVideoUri(videoUri: Uri) {
        // Save the video URI to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("videoUri", videoUri.toString())
        editor.apply()
    }

    private fun getSavedVideoUri(): Uri? {
        // Retrieve the saved video URI from SharedPreferences
        val savedVideoUriString = sharedPreferences.getString("videoUri", null)
        return if (savedVideoUriString != null) {
            Uri.parse(savedVideoUriString)
        } else {
            null
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
        outState.putBoolean("isVideoPlaying", isVideoPlaying)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore the saved position of the video
        videoPosition = savedInstanceState.getInt("videoPosition")
        isVideoPlaying = savedInstanceState.getBoolean("isVideoPlaying")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var videoView = findViewById<VideoView>(R.id.video1_view)
        // Save the current video position before changing the layout
        videoPosition = videoView.currentPosition

        // Set the new layout based on orientation
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.portrait)
        } else {
            setContentView(R.layout.landscape)
        }
        videoView = findViewById<VideoView>(R.id.video1_view)
        val PickVideoButton = findViewById<Button>(R.id.video1_button)
        PickVideoButton.visibility = GONE;
        videoView.visibility = VISIBLE
        // Restore the video position after changing the layout
        videoView.setVideoURI(getSavedVideoUri())
        videoView.seekTo(videoPosition)

        if (isVideoPlaying) {
            videoView.start()
        }

        findViewById<Button>(R.id.video1_button).setOnClickListener {
            Log.d("BUTTONS", "User tapped the Supabutton")
            PickVideo(0)
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