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
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
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
        ViewModelProvider(
            this,
            RepresentativeViewModel.RepresentativeViewModelFactory(activity.application)
        )[RepresentativeViewModel::class.java]
    }

    private val sharedPreferences by lazy {
        activity?.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var locationProvider: FusedLocationProviderClient
    private var locationRequestCount = 0
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
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUpdateView()
        initializeUI()
    }

    private fun bindUpdateView() {
        viewModel.addressLine1.value = sharedPreferences?.getString("addressLine1", "")
        viewModel.addressLine2.value = sharedPreferences?.getString("addressLine2", "")
        viewModel.city.value = sharedPreferences?.getString("city", "")
        viewModel.state.value = sharedPreferences?.getString("state", "")
        viewModel.zip.value = sharedPreferences?.getString("zip", "")
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
                id: Long,
            ) {
                viewModel.updateStateValue(binding.spinnerState.selectedItem as String)
            }
        }

        val representativeListAdapter = RepresentativeListAdapter()
        binding.rvRepresentations.adapter = representativeListAdapter
        viewModel.local.observe(viewLifecycleOwner, Observer { representatives ->
            if(representatives!=null){
                representativeListAdapter.submitList(representatives.representatives)
            }
        })

        binding.btnLocation.setOnClickListener {
            retrieveUserLocationAddress()
        }
        binding.btnSearch.setOnClickListener {
            viewModel.searchRepresentatives()
        }
    }

    @SuppressLint("MissingPermission")
    private fun retrieveUserLocationAddress() {
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
            locationRequestCount = 0
            getUserLocationAddressAndUpdate()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocationAddressAndUpdate() {
        locationProvider.lastLocation
            .addOnSuccessListener { location: Location? ->
                handleLocationSuccess(location)
            }
    }

    private fun handleLocationSuccess(location: Location?) {
        if (location != null) {
            val address = geoCodeLocation(location)
            viewModel.updateAddress(address)
            setSpinnerSelection(address)
            locationRequestCount = 0
        } else {
            handleNullLocation()
        }
    }

    private fun handleNullLocation() {
        // Sometime, it can't get location immediately, need some delay
        if (locationRequestCount < Constance.LOCATION_REQUEST_MAX_COUNT) {
            locationRequestCount += 1
            scheduleLocationRetry()
        }
    }

    private fun scheduleLocationRetry() {
        handler.postDelayed({
            getUserLocationAddressAndUpdate()
        }, Constance.LOCATION_REQUEST_DELAY)
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

    private fun setSpinnerSelection(address: Address) {
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
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
        grantResults: IntArray,
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
            showLocationPermissionDeniedSnackbar()
        }
    }

    private fun handleLocationPermissionGranted() {
        retrieveUserLocationAddress()
    }

    private fun showLocationPermissionDeniedSnackbar() {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.grant_location_permission_to_enable_this_feature,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.setting) {
                startApplicationDetailsSettings()
            }.show()
    }

    private fun startApplicationDetailsSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun onStop() {
        super.onStop()
        val editor = sharedPreferences?.edit()
        editor?.let {
            it.putString("addressLine1",viewModel.addressLine1.value)
            it.putString("addressLine2", viewModel.addressLine2.value)
            it.putString("city", viewModel.city.value)
            it.putString("state", viewModel.state.value)
            it.putString("zip", viewModel.zip.value)
            it.apply()
        }
    }

}