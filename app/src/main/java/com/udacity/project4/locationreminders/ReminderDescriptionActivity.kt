package com.udacity.project4.locationreminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }
    private var coroutineJob: Job = Job()
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    private val repository: ReminderDataSource by inject()

    private lateinit var geofencingClient: GeofencingClient

    private lateinit var item: ReminderDataItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        if (intent.hasExtra(EXTRA_ReminderDataItem)) {
            val reminderDataItem:ReminderDataItem = intent.getSerializableExtra(
                EXTRA_ReminderDataItem) as ReminderDataItem
            binding.reminderDataItem = reminderDataItem
            binding.executePendingBindings()
        }

        geofencingClient = LocationServices.getGeofencingClient(this)

        binding.reminderDataItem = item
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) delete()
        return super.onOptionsItemSelected(item)
    }

    private fun delete() {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
        val geofencePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            item.requestCode ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                CoroutineScope(coroutineContext).launch {
                    repository.deleteReminders(item.id)
                }
                val reminderIntent = Intent(baseContext, RemindersActivity::class.java)
                reminderIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(reminderIntent)
            }
            addOnFailureListener {
                Toast.makeText(baseContext, "Couldn't delete reminder", Toast.LENGTH_LONG).show()
            }
        }
    }
}