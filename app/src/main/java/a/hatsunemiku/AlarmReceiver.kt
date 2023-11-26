package a.hatsunemiku

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the alarm trigger here
        // For example, start an activity immediately
        val startIntent = Intent(context, Video::class.java)
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(startIntent)
    }
}
