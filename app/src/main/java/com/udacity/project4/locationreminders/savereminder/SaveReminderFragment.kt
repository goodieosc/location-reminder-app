package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "GeoFenceLogs"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var location: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private lateinit var reminderId: String

    //Check the API level. This is needed to determine how to check for location permissions depending on the API level.
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    //A PendingIntent is a description of an Intent and target action to perform with it.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var geofenceList: MutableList<Geofence>

    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        //instantiate the geofencingClient
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            title = _viewModel.reminderTitle.value.toString()
            description = _viewModel.reminderDescription.value.toString()
            location = _viewModel.reminderSelectedLocationStr.value.toString()
            latitude = _viewModel.latitude.value!!
            longitude = _viewModel.longitude.value!!

            val reminder = ReminderDataItem(title,description,location,latitude,longitude)

            reminderId = reminder.id

            _viewModel.validateAndSaveReminder(reminder)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
            requestForegroundAndBackgroundLocationPermissions() //Function then calls another  function to add geofence.

            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    //Request permissions if not already granted.
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {

        //If the permissions have already been approved, you don’t need to ask again. Return out of the method.
        if (foregroundAndBackgroundLocationPermissionApproved()){
            Log.i(TAG,"Foreground and background permission already approved")
            checkDeviceLocationSettings()
            return
        }

        //The permissionsArray contains the permissions that are going to be requested.
        // Initially, add ACCESS_FINE_LOCATION since that will be needed on all API levels.
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        //You will need a resultCode. The code will be different depending on if the device is running Q or later
        // and will inform us if you need to check for one permission (fine location) or multiple permissions
        // (fine and background location) when the user returns from the permission request screen.
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.i(TAG, "Permissions requested: ${permissionsArray}")
        Log.i(TAG, "Permissions result code: ${resultCode}")

        //Request permissions passing in the current activity, the permissions array and the result code.
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissionsArray,
            resultCode
        )
    }

    //Check what permissions the app currently has for location settings.
    //[Returns a boolean if permissions are already granted or not]
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {

        //Check is fine_location access has been granted by the phone
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))

        //If the device is running Q or higher, check that the ACCESS_BACKGROUND_LOCATION permission is granted.
        //Return true if the device is running lower than Q where you don't need a permission to access location in the background.
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }

        Log.i(TAG, "Foreground permissions approved: $foregroundLocationApproved || Bakground permissions approved: $backgroundPermissionApproved")
        //Return true if the permissions are granted and false if not.
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    //Once the user responds to the permissions, you will need to handle their response.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")


        //Permissions can be denied in a few ways:
        //   If the grantResults array is empty, then the interaction was interrupted and the permission request was cancelled.
        //   If the grantResults array’s value at the LOCATION_PERMISSION_INDEX has a PERMISSION_DENIED it means that the user denied foreground permissions.
        //   If the request code equals REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE and the BACKGROUND_LOCATION_PERMISSION_INDEX is denied it means that the device is running API 29 or above and that background permissions were denied.
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            //Show a snackbar if the user clicks deny or cancel
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {

            //If permissions have been granted. Create the geofence.
            checkDeviceLocationSettings()
        }
    }

    private fun checkDeviceLocationSettings(resolve:Boolean = true) {

        //Create a LocationRequest
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        //Create a location request builder
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        //Use LocationServices to get the Settings Client and create a val called locationSettingsResponseTask to
        // check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        //Since the case we are most interested in here is finding out if the location settings are not satisfied,
        //add an onFailureListener() to the locationSettingsResponseTask.
        locationSettingsResponseTask.addOnFailureListener { exception ->

            //Check if the exception is of type ResolvableApiException and if so, try calling the startResolutionForResult()
            //method in order to prompt the user to turn on device location.
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)

                //If calling startResolutionForResult enters the catch block, print a log.
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.i(TAG, "Error getting location settings resolution: " + sendEx.message)
                }

            //If the exception is not of type ResolvableApiException, present a snackbar that alerts the user
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        //If the locationSettingsResponseTask does complete, check that it is successful, if so you will want to add the geofence.
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofence()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {

        //Build the geofence using the geofence builder
        val geofence = Geofence.Builder()
            .setRequestId(reminderId)
            .setCircularRegion(latitude,longitude, 100F)
            .setExpirationDuration(-1) //-1 is for never expire
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        //Build the geofence request. Set the initial trigger to INITIAL_TRIGGER_ENTER.
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence) //Pass in the geofence you just built
            .build()


        //add the new geofences.
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {

            //If adding the geofences is successful, let the user know through a toast that they were successful.
            addOnSuccessListener {
                Toast.makeText(requireActivity(), "Geofence added",
                    Toast.LENGTH_SHORT)
                    .show()
                Log.i(TAG, "Geofence successfully added ${geofence.requestId}")
            }

            //If adding the geofences fails, present a toast letting the user know that there was an issue in adding the geofences.
            addOnFailureListener {
                Toast.makeText(requireActivity(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.i(TAG,"Error adding geofence: ${it.message!!}")
                }
            }
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

}

