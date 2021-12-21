package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(R.string.geofence_not_available)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(R.string.geofence_too_many_geofences)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(R.string.geofence_too_many_pending_intents)
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val TAG = "GeoFenceLogs"

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    @SuppressLint("LongLogTag")
    override fun onHandleWork(intent: Intent) {
        //handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        //Log the error message if not successfully.
        if (geofencingEvent.hasError()) {
            val errorMessage = errorMessage(this, geofencingEvent.errorCode)
            Log.i(TAG, errorMessage)
            return
        }

        //Call @sendNotification
        sendNotification(geofencingEvent.triggeringGeofences)
        Log.i(TAG, "Geofence entered")
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        Log.i(TAG,"Sending notification ${triggeringGeofences}")


        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()

        //Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {

            for (i in triggeringGeofences) {

            //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(i.toString())

                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                            )
                        )
                    }
            }
        }
    }
}