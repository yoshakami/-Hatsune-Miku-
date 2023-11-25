package a.hatsunemiku

import VideoPlayReceiver
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
import android.provider.Settings
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Environment
import android.view.View.GONE
import androidx.annotation.RequiresApi
import java.util.Random
import android.content.ContentResolver
import android.view.View
import android.view.WindowManager
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.PowerManager

class MainActivity : ComponentActivity() {

    private lateinit var videoView: VideoView
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private var videoPosition: Int = 0 // Variable to store video position
    private var isVideoPlaying: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    // Use this variable to track if the permission has been granted
    private var storagePermissionGranted = false

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedVideoUri = data.data
                    if (selectedVideoUri != null) {
                        val videoView = findViewById<VideoView>(R.id.video1_view)
                        videoView.visibility = VideoView.VISIBLE
                        isVideoPlaying = true
                        videoView.setVideoURI(selectedVideoUri)
                        videoView.start()
                        saveVideoUri(selectedVideoUri)
                    }
                }
            }
        }
    private val requestStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission granted
                    launchSAFPickVideoIntent()
                    handleStoragePermissionGranted()
                    storagePermissionGranted = true
                    // Continue with your app initialization or functionality
                } else {
                    // Permission denied
                    requestManageExternalStoragePermission()
                    Toast.makeText(
                        this,
                        "Storage permission is required for the app to function properly.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Handle the case where the user denied the permission
                }
            }

    private val requestManageExternalStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // User granted MANAGE_EXTERNAL_STORAGE permission
                handleStoragePermissionGranted()
            } else {
                // User denied MANAGE_EXTERNAL_STORAGE permission
                // Handle accordingly
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if the app has storage permissions
        /*if (checkStoragePermission()) {
            // Storage permission is already granted
            storagePermissionGranted = true
            // Continue with your app initialization or functionality
        } else {
            // Request storage permission
            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }*/
        // Acquire wake lock
        acquireWakeLock()

        // Schedule video playback at 00:35
        scheduleVideoPlayback()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is granted, handle accordingly
            handleStoragePermissionGranted()
        } else {
            // If permission is not granted, request it
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
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
        // openRandomVideoInMovieFolder()
        /*
        //storagePermissionGranted = true
            // Retrieve saved video URI
            val savedVideoUriString = sharedPreferences.getString("videoUri", null)
            if (savedVideoUriString != null) {
                val savedVideoUri = Uri.parse(savedVideoUriString)
                videoView.visibility = VideoView.VISIBLE
                videoView.setVideoURI(savedVideoUri)
                isVideoPlaying = true
                videoView.start()
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // If permission is granted, launch the SAF pick video intent
                launchSAFPickVideoIntent()
            } else {
                // If permission is not granted, request it
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            } */




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
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "YourApp:WakeLock"
        )
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun scheduleVideoPlayback() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set the time to 00:35
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 35)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(this, VideoPlayReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule the alarm
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release wake lock when the activity is destroyed
        releaseWakeLock()
    }
    fun openRandomVideoInMovieFolder() {
        // Make the app fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val PickVideoButton = findViewById<Button>(R.id.video1_button)
        PickVideoButton.visibility = GONE;
        val videoView = findViewById<VideoView>(R.id.video1_view)
        // Specify the folder path
        val folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath

        // Get a list of files in the folder
        val fileList = getFilesInFolder(folderPath)

        // Select a random file from the list
        val random = Random()
        val randomFile = fileList[random.nextInt(fileList.size)]

        // Play the selected video file
        // Set the video URI and start playing
        val videoUri = Uri.parse(randomFile)
        isVideoPlaying = true

        saveVideoUri(videoUri)
        videoView.visibility = VideoView.VISIBLE
        videoView.setVideoURI(videoUri)
        videoView.start()
    }
    private fun getFilesInFolder(folderPath: String): List<String> {
        val fileList = mutableListOf<String>()

        val contentResolver: ContentResolver = contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA
        )

        val selection =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO +
                    " AND " + MediaStore.Files.FileColumns.DATA + " LIKE ?"

        val selectionArgs = arrayOf("$folderPath/%")

        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {
                val filePath =
                    it.getString(it.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                fileList.add(filePath)
            }
        }

        return fileList
    }
    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            requestManageExternalStoragePermissionLauncher.launch(intent)
        }
    }

    private fun handleStoragePermissionGranted() {
        // Your logic for handling full storage access
        // This is where you can perform operations that require full storage access
    }

    private fun launchSAFPickVideoIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*" // Specify the MIME type of videos you want to allow
        }
        pickVideoLauncher.launch(intent)
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