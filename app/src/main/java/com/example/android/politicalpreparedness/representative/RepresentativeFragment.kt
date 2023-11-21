package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.utlis.Constance
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class RepresentativeFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding
    private val viewModel: RepresentativeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "The viewModel is not available until onViewCreated() is called"
        }
        ViewModelProvider(this, RepresentativeViewModel.RepresentativeViewModelFactory(activity.application))[RepresentativeViewModel::class.java]
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var locationProvider: FusedLocationProviderClient
    private  var retrieveLocationRetry = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_representative,
            container, false
        )
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()

    }

    private fun initializeUI() {
        locationProvider = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.updateStateValue(binding.spinnerState.selectedItem as String)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.updateStateValue(binding.spinnerState.selectedItem as String)
            }
        }

        val representativeListAdapter = RepresentativeListAdapter()
        binding.rvRepresentations.adapter = representativeListAdapter
        viewModel.representatives.observe(viewLifecycleOwner, Observer { representatives ->
            representativeListAdapter.submitList(representatives)
        })

        binding.btnLocation.setOnClickListener {
            requestUserLocationAndFetchAddress()
        }
        binding.btnSearch.setOnClickListener {
            //viewModel.findMyRepresentatives()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestUserLocationAndFetchAddress() {
        when {
            isFineLocationPermissionGranted() -> {
                enableLocationServiceAndGetUserLocation()
            }
            shouldShowLocationPermissionRationale() -> {
                showLocationPermissionRationale()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun shouldShowLocationPermissionRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showLocationPermissionRationale() {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.allow_location_to_fetch_your_address,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(android.R.string.ok) {
                requestLocationPermission()
            }.show()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constance.REQUEST_LOCATION_PERMISSION
        )
    }

    private fun enableLocationServiceAndGetUserLocation(needResolve: Boolean = true) {
        val builder = buildLocationSettingsRequest()

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            handleLocationSettingsFailure(exception, needResolve)
        }

        locationSettingsResponseTask.addOnCompleteListener {
            handleLocationSettingsSuccess(it)
        }
    }

    private fun buildLocationSettingsRequest(): LocationSettingsRequest.Builder {
        return LocationSettingsRequest.Builder().addLocationRequest(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
        )
    }

    private fun handleLocationSettingsFailure(exception: Exception, needResolve: Boolean) {
        if (exception is ResolvableApiException && needResolve) {
            startLocationResolution(exception)
        } else {
            showLocationServicesDeniedSnackbar()
        }
    }

    private fun startLocationResolution(exception: ResolvableApiException) {
        startIntentSenderForResult(
            exception.resolution.intentSender,
            Constance.REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
        )
    }

    private fun showLocationServicesDeniedSnackbar() {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.enable_location_services_to_use_this_feature, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            enableLocationServiceAndGetUserLocation()
        }.show()
    }

    private fun handleLocationSettingsSuccess(task: Task<LocationSettingsResponse>) {
        if (task.isSuccessful) {
            retrieveLocationRetry = 0
            fetchLocationAndUpdateAddress()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndUpdateAddress() {
        locationProvider.lastLocation
            .addOnSuccessListener { location: Location? ->
                handleLocationSuccess(location)
            }
    }

    private fun handleLocationSuccess(location: Location?) {
        if (location != null) {
            val address = geoCodeLocation(location)
            viewModel.updateAddress(address)
            selectSpinnerState(address)
            retrieveLocationRetry = 0
        } else {
            handleNullLocation()
        }
    }

    private fun handleNullLocation() {
        // Sometime, it can't get location immediately, need some delay
        if (retrieveLocationRetry < Constance.RETRIE_LOCATION_MAXIMUM) {
            retrieveLocationRetry += 1
            scheduleLocationRetry()
        }
    }

    private fun scheduleLocationRetry() {
        handler.postDelayed({
            fetchLocationAndUpdateAddress()
        }, Constance.RETRIE_LOCATION_DELAY)
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = createGeocoder()
        return getFirstAddressFromLocation(geocoder, location)
    }

    private fun createGeocoder(): Geocoder {
        return Geocoder(requireContext(), Locale.getDefault())
    }

    private fun getFirstAddressFromLocation(geocoder: Geocoder, location: Location): Address {
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
            .map { mapToAddress(it) }
            .first()
    }

    private fun mapToAddress(address: android.location.Address): Address {
        return Address(
            address.thoroughfare,
            address.subThoroughfare,
            address.locality,
            address.adminArea,
            address.postalCode
        )
    }

    private fun selectSpinnerState(address: Address) {
        val states = resources.getStringArray(R.array.states)
        binding.spinnerState.setSelection(
            if (states.contains(address.state)) {
                states.indexOf(address.state)
            } else {
                0
            }
        )
    }
    private fun isFineLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constance.REQUEST_TURN_DEVICE_LOCATION_ON -> {
                enableLocationServiceAndGetUserLocation(false)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constance.REQUEST_LOCATION_PERMISSION -> handleLocationPermissionResult(grantResults)
        }
    }

    private fun handleLocationPermissionResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleLocationPermissionGranted()
        } else {
            //showLocationPermissionDeniedSnackbar()
        }
    }

    private fun handleLocationPermissionGranted() {
        requestUserLocationAndFetchAddress()
    }

//    private fun showLocationPermissionDeniedSnackbar() {
//        Snackbar.make(
//            requireActivity().findViewById(android.R.id.content),
//            R.string.location_permission_denied_explanation,
//            Snackbar.LENGTH_INDEFINITE
//        )
//            .setAction(R.string.settings) {
//                startApplicationDetailsSettings()
//            }.show()
//    }
//
//    private fun startApplicationDetailsSettings() {
//        startActivity(Intent().apply {
//            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        })
//    }


    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            //TODO: Request Location permissions
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        return false
    }



//    private fun geoCodeLocation(location: Location): Address {
//        val geocoder = Geocoder(context, Locale.getDefault())
//        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
//            .map { address ->
//                Address(
//                    address.thoroughfare,
//                    address.subThoroughfare,
//                    address.locality,
//                    address.adminArea,
//                    address.postalCode
//                )
//            }
//            .first()
//    }
//
//    private fun hideKeyboard() {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
//    }

    /**
     * biến: retrieveLocationRetry,RETRIE_LOCATION_MAXIMUM,RETRIE_LOCATION_DELAY,REQUEST_TURN_DEVICE_LOCATION_ON
    function: requestUserLocationAndFetchAddress,fetchLocationAndUpdateAddress,geoCodeLocation,selectSpinnerState
     */
}