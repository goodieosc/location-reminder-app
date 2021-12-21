package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 * Note: The broadcast receiver is how Android apps can send or receive broadcast messages from the Android system and other Android apps.
 */

/**
 * Returns the error string for a geofencing error code.
 */


private const val TAG = "GeoFenceLogs"

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //A Broadcast Receiver can receive many types of actions, but in our case we only care about when the geofence is entered.
        // Check that the intent’s action is of type ACTION_GEOFENCE_EVENT
        if (intent.action == ACTION_GEOFENCE_EVENT) {

            //Send a notification
            Log.i(TAG, "Broadcast received")
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        }
    }
}
