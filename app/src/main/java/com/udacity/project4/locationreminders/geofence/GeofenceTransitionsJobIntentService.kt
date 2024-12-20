package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private val TAG = "GeofenceTransitionsJobIntentService"
    private val remindersLocalRepository: ReminderDataSource by inject()

    companion object {
        private const val JOB_ID = 573

        // TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        // TODO: handle the geofencing transition events and
        //  send a notification to the user when he enters the geofence area
        // TODO call @sendNotification

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) return
        }

        if (geofencingEvent != null) {
            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                when {
                    geofencingEvent.triggeringGeofences!!.isNotEmpty() -> geofencingEvent.triggeringGeofences?.let {
                        sendNotification(
                            it
                        )
                    }
                    else -> return
                }
            }
        }

    }

    // TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        val requestId = triggeringGeofences[0].requestId
        //Get the instance of the local repository
        val remindersLocalRepository: ReminderDataSource by inject()
      // Interaction to the repository has to be a coroutine scope to run in the background
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
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
                        reminderDTO.id,
                        reminderDTO.deleteFlag,
                        reminderDTO.requestCode ?: 0
                    )
                )
            }
        }
    }
}