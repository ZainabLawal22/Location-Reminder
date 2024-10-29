package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    // Initialize the Geofencing client
    private lateinit var geofencingClient: GeofencingClient

    private lateinit var reminderDataItem: ReminderDataItem

    companion object {
        private const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.project4.action.ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_RADIUS = 1000f
        private const val REQUEST_LOCATION_ON = 20
        private const val PERMISSION_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 30
        private const val PERMISSION_FOREGROUND_RESULT_CODE = 40
    }


    // Create a PendingIntent Variable for the Geofence
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(), 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_save_reminder, container, false
            )

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the lifecycle owner for data binding
        binding.lifecycleOwner = this

        // Navigate to SelectLocationFragment when the user clicks "Select Location"
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        // Initialize the GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        // Handle the "Save Reminder" button click
        binding.saveReminder.setOnClickListener {
            // Retrieve reminder data from the ViewModel
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // Validate and save the reminder data using ViewModel
//            val reminderDTO = _viewModel.validateAndSaveReminder(
//                ReminderDataItem(
//                    title = title,
//                    description = description,
//                    latitude = latitude,
//                    longitude = longitude,
//                    location = location
//                )
//            )
            reminderDataItem = ReminderDataItem(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                location = location
            )

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                checkPermissionAndStartGeofencing()
            }


//            if (reminderDTO != null) {
//
//                addGeofenceForClue(reminderDTO)
//            }
        }
    }


    private fun checkPermissionAndStartGeofencing() {
        if (locationPermissionGranted()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            foregroundBackgroundLocationPermissions()
        }
    }

    private fun locationPermissionGranted(): Boolean {
        val backgroundPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return backgroundPermission && fineAndCoarseLocationPermission()
    }

    private fun fineAndCoarseLocationPermission(): Boolean {
        val fineLocationGranted = context?.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        return fineLocationGranted && coarseLocationGranted
    }

    private fun foregroundBackgroundLocationPermissions() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                PERMISSION_FOREGROUND_AND_BACKGROUND_RESULT_CODE
            }

            else -> PERMISSION_FOREGROUND_RESULT_CODE
        }
        requestPermissions(
            permissionsArray,
            requestCode
        )
    }

    @SuppressLint("VisibleForTests")
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this@SaveReminderFragment.requireActivity(),
                        REQUEST_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        ContentValues.TAG,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.saveReminder,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addNewGeofence()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_FOREGROUND_AND_BACKGROUND_RESULT_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            Snackbar.make(
                binding.saveReminder,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                foregroundBackgroundLocationPermissions()
            }.show()
        }
    }

    @SuppressLint("MissingPermission", "VisibleForTests")
    private fun addNewGeofence() {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        _viewModel.saveReminder(reminderDataItem)
                    }
                    addOnFailureListener {
                        _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
                    }
                }
            }
        }
        _viewModel.onClear()
    }


//    // Function to add a Geofence for the saved reminder
//    private fun addGeofenceForClue(reminder: ReminderDTO) {
//
//        val geofence = Geofence.Builder()
//            .setRequestId(reminder.id)
//            .setCircularRegion(
//                reminder.latitude!!,
//                reminder.longitude!!,
//                100f
//            )
//            .setExpirationDuration(TimeUnit.HOURS.toMillis(1))
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//            .build()
//
//
//        val geofencingRequest = GeofencingRequest.Builder()
//            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//            .addGeofence(geofence)
//            .build()
//
//
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            return
//        }
//
//        // Add the Geofence to the GeofencingClient
//        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
//            addOnSuccessListener {
//                Snackbar.make(
//                    binding.root,
//                    "Geofence Added",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//                // Toast.makeText(requireContext(), "Geofence Added", Toast.LENGTH_SHORT).show()
//            }
//            addOnFailureListener {
//                Snackbar.make(
//                    binding.root,
//                    "Failed to add Geofence",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//                //  Toast.makeText(requireContext(), "Failed to add Geofence", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}
