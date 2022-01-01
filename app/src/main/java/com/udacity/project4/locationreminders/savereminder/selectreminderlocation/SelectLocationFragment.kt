package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

//Data class for map zoom levels.
data class ZoomLevel(
    val WORLD: Float = 1f,
    val CONTINENT: Float = 5f,
    val CITY: Float = 10f,
    val STREET: Float = 15f,
    val BUILDING: Float = 20f
)

private const val TAG = "TrackLocation"
private val TAG2 = RemindersActivity::class.java.simpleName

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var poiMarker: Marker
    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        //Add the map setup implementation
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonSave.setOnClickListener {
            onLocationSelected()
        }

        //Get location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    // When the user confirms on the selected location,
    // send back the selected location details to the view model
    private fun onLocationSelected() {

        //Pop back to previous fragment.
        val navController = findNavController()
        navController.popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
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

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        // Add a marker in Singapore and move the camera
//        val flamingoValley = LatLng(1.3188355976591175, 103.92273898388306)
//        val zoomLevel = ZoomLevel().STREET
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(flamingoValley,zoomLevel))
//        map.addMarker(MarkerOptions().position(flamingoValley).title("Flamingo Valley"))

        //Put a marker to location that the user selected
        //setMapLongClick(map)

        setMapStyle(map)

        //Enable POI marking
        setPoiClick(map)

        //Enable to select any location on long click
        setMapLongClick(map)

        //Enable location tracking
        enableMyLocation()

        //Move map to current location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.

                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),ZoomLevel().STREET))
                }
            }
    }

    //Enable POI's to be clicked.
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            _viewModel.reminderSelectedLocationStr.value = poiMarker.title
            _viewModel.latitude.value = poiMarker.position.latitude
            _viewModel.longitude.value = poiMarker.position.longitude
            binding.buttonSave.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
            Log.i(TAG,"Permission granted to location")
        }
        else {
            Log.i(TAG,"Requesting location permissions")
            //Removed as for Activity, replaced with requestPermissions()
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        Log.i(TAG,"Reviewing permission request result...")
        Log.i(TAG, requestCode.toString())

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
                Log.i(TAG,"Location permission granted")
            } else {
                Log.i(TAG,"Location permission NOT granted")
                Snackbar.make(requireView(),"Location permission is needed to receive notifications",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
            _viewModel.reminderSelectedLocationStr.value = "Dropped pin"
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            binding.buttonSave.visibility = View.VISIBLE
        }
    }

}
