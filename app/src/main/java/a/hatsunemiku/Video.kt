package a.hatsunemiku

import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class Video : AppCompatActivity() {
    private var videoPosition: Int = 0 // Variable to store video position
    private var isVideoPlaying: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getPreferences(MODE_PRIVATE)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.portrait)
        } else {
            setContentView(R.layout.landscape)
        }
        // Get random video URI and play it
        openRandomVideoInMovieFolder()
    }

    private fun playVideo(videoUri: Uri?) {

        if (videoUri != null) {
            val videobutton = findViewById<Button>(R.id.video1_button)
            videobutton.visibility = GONE
            val videoView = findViewById<VideoView>(R.id.video1_view)
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }

    private fun saveVideoUri(videoUri: Uri) {
        // Save the video URI to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("videoUri", videoUri.toString())
        editor.apply()
    }

    fun openRandomVideoInMovieFolder() {
        fullscreen()

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val PickVideoButton = findViewById<Button>(R.id.video1_button)
        PickVideoButton.visibility = View.GONE;
        val videoView = findViewById<VideoView>(R.id.video1_view)
        videoView.setOnErrorListener { mp, what, extra ->
            Log.e("VIDEO_ERROR", "MediaPlayer error: $what, $extra, ${MediaPlayer.MEDIA_ERROR_IO}")
            // Handle the error, e.g., show a message to the user
            true
        }
        // Specify the folder path
        val folderPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath

        // Get a list of files in the folder
        val fileList = getFilesInFolder(folderPath)

        // Select a random file from the list
        val random = Random()
        if (fileList.isNotEmpty())
        {
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
    }
    private fun getFilesInFolder2(folderPath: String): List<String> {
        val fileList = mutableListOf<String>()

        val contentResolver: ContentResolver = contentResolver

        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.RELATIVE_PATH
        )

        val selection =
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"

        val selectionArgs = arrayOf("%$folderPath%")

        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"

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
                    it.getString(it.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH))
                fileList.add(filePath)
            }
        }

        return fileList
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
    fun fullscreen()
    {
        // Make the app fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
    }

    override fun onResume() {
        super.onResume()
        fullscreen()
    }
    /*
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fullscreen()

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
        videoView.visibility = View.VISIBLE
        // Restore the video position after changing the layout
        videoView.setVideoURI(getSavedVideoUri())
        videoView.seekTo(videoPosition)

        if (isVideoPlaying) {
            videoView.start()
        }
        videoView.setOnErrorListener { mp, what, extra ->
            Log.e("VIDEO_ERROR", "MediaPlayer error: $what, $extra")
            // Handle the error, e.g., show a message to the user
            true
        }
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

    }*/
    private fun getSavedVideoUri(): Uri? {
        // Retrieve the saved video URI from SharedPreferences
        val savedVideoUriString = sharedPreferences.getString("videoUri", null)
        return if (savedVideoUriString != null) {
            Uri.parse(savedVideoUriString)
        } else {
            null
        }
    }
}
