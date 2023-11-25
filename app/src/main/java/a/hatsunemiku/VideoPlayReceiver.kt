import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.widget.VideoView

class VideoPlayReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Acquire a wake lock
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "YourApp:VideoPlayWakeLock"
            )
            wakeLock.acquire()

            // Play a random video from the movie folder
            playRandomVideo(context)

            // Release the wake lock when video playback is complete
            wakeLock.release()
        }
    }

    private fun playRandomVideo(context: Context) {
        // Example: Get a random video file URI from the movie folder
        val videoUri = GetRandomFileFromMovieFolder(context)

        // Check if the URI is not null before attempting to play
        if (videoUri != null) {
            val videoView = VideoView(context)
            videoView.setVideoURI(videoUri)
            videoView.setOnPreparedListener { mp: MediaPlayer -> mp.isLooping = true }
            videoView.start()
        }
    }
}
