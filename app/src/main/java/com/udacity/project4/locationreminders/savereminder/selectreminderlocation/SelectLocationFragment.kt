package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.DialogMessage
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val zoomLevel = 16F

    private val TAG = SelectLocationFragment::class.java.simpleName

    val reminderActivity: RemindersActivity by lazy {
        activity as RemindersActivity
    }
    private lateinit var locationManager: LocationManager

    var locationListener: LocationListener = LocationListener { location ->
        updateLocation(location)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this



        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        //add the map setup implementation
        //zoom to the user location after taking his permission
        //add style to the map
        //put a marker to location that the user selected

        //all this function after the user confirms on the selected location
        //onLocationSelected()
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

        binding.saveLocation.setOnClickListener {
            onLocationSelected()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    private fun updateLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoomLevel))
        locationManager.removeUpdates(locationListener)
    }

    override fun onStart() {
        super.onStart()
        setTitle(getString(R.string.title_activity_maps))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        val lat = 37.422160
        val lng = -122.084270
        val latLng = LatLng(lat, lng)

        val zoomLevel = zoomLevel
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        setMapClick(map)
        setMapPoiClick(map)
        setInfoWindowClick(map)
        setMapStyle(map)

    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            val snippet = getString(R.string.lat_long_snippet, latLng.latitude, latLng.longitude)
            map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )?.apply { showInfoWindow() }
            _viewModel.selectedPOI.value = null
            _viewModel.latLong.value = latLng
        }
    }

    private fun setMapPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
                ?.apply { showInfoWindow() }
            _viewModel.selectedPOI.value = poi
            _viewModel.latLong.value = poi.latLng
        }
    }

    private fun setInfoWindowClick(map: GoogleMap) {
        map.setOnInfoWindowClickListener {
            _viewModel.showDialogMessage.value = DialogMessage(
                requireContext(),
                getString(R.string.choose_a_location),
                getString(
                    R.string.choose_location_text,
                    if (_viewModel.selectedPOI.value != null) _viewModel.selectedPOI.value?.name
                        ?: "" else getString(
                        R.string.lat_long_snippet,
                        _viewModel.latLong.value?.latitude,
                        _viewModel.latLong.value?.longitude
                    )
                ),
                Pair(getString(R.string.select_button), DialogInterface.OnClickListener { _, _ ->
                    onLocationSelected()
                }),
                negativeButton = Pair(
                    getString(R.string.cancel_button),
                    DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
            )
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                context?.let {
                    MapStyleOptions.loadRawResourceStyle(
                        it,
                        R.raw.map_style
                    )
                }
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun onLocationSelected() {

        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            } catch (_: Exception) {
            }
        } else {
            reminderActivity.permissionCallback = { enableMyLocation() }
            reminderActivity.requestPermission()
        }
    }

    private fun onBackPressed() {
        _viewModel.selectedPOI.value = null
        _viewModel.latLong.value = null
        _viewModel.showToast.value = getString(R.string.selection_cancelled)
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

}