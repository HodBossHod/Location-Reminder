package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var myMap: GoogleMap
    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    private var locationMarker: Marker? =null
    private val APPLICATION_ID="com.udacity.project4"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        _viewModel .selectedPOI.observe(viewLifecycleOwner) { binding.saveLocationButton.isEnabled = true }

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        val zoom = 17f
        var latitude = 30.0
        var longitude = 120.0
        var latLng = LatLng(latitude, longitude)
        locationMarker?.let {
            controlMarker(LatLng(it.position.latitude, it.position.longitude))
        }
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        setMapLongClick(myMap)
        setPoiClick(myMap)
        setMapStyle(myMap)
        enableMyLocation()
    }
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    private fun controlMarker(latLng: LatLng){
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            latLng.latitude,
            latLng.longitude
        )
        locationMarker?.remove()
        locationMarker= myMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(String.format("Lat: %1$.2f, Long: %2$.2f",
                    latLng.latitude,
                    latLng.longitude))
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )!!
        val poi=PointOfInterest(locationMarker!!.position,locationMarker!!.id,locationMarker!!.title!!)
        locationMarker?.title=poi.name
        _viewModel.selectedPOI.value=poi
    }
    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            controlMarker(latLng)
        }
    }
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            locationMarker?.remove()
            locationMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            locationMarker ?.showInfoWindow()
            val poi=PointOfInterest(locationMarker !!.position,locationMarker !!.id,locationMarker !!.title!!)
            _viewModel.selectedPOI.value=poi
        }
    }

    @Suppress("Missing Permission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }else{
                Snackbar.make(
                    requireView(),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            }
        }
    }


    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            myMap.isMyLocationEnabled=true
        }

        else {
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val lastLocationTask = fusedLocationProviderClient.lastLocation
        // On completion, zoom to the user location and add marker
        lastLocationTask.addOnCompleteListener(requireActivity()) {

            if (it.isSuccessful) {
                val taskResult = it.result
                taskResult?.run {

                    val latLng = LatLng(latitude, longitude)
                    myMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            15f
                        )
                    )
                    controlMarker(latLng)
                }
            }
        }
    }


    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        locationMarker?.let {
            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude
            _viewModel.reminderSelectedLocationStr.value = it.title
        }
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            myMap.mapType=GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            myMap.mapType=GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            myMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            myMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

}
