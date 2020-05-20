package com.example.ouroboros.activities.maps

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.ouroboros.R
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.SET_LOCATION_MAP
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.SHOW_LOCATION_MAP
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.UNKNOWN_REQUEST_MAP
import com.example.ouroboros.activities.maps.MapConstants.ZoomCodes.Companion.DEFAULT_ZOOM_MAP
import com.example.ouroboros.intent.LocationSerializable
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.UNKNOWN_ROLE
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.MAPS_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LATITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LONGITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.PRESSED
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var requestMap : Int = UNKNOWN_REQUEST_MAP
    private var requestRoleType : Int = UNKNOWN_ROLE
    private lateinit var resourceLocation : LatLng
    private lateinit var resourceMarker : Marker
    private var savePressed : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        requestRoleType = intent?.getIntExtra(ROLE_TYPE_REQUEST_CODE,UNKNOWN_ROLE)!!
        requestMap = intent?.getIntExtra(MAP_REQUEST_CODE,UNKNOWN_REQUEST_MAP)!!
        val resourceLocationSerializable : LocationSerializable = intent?.getSerializableExtra(LOCATION_SERIALIZABLE_CODE) as LocationSerializable
        if (resourceLocationSerializable != null){
            resourceLocation = LatLng(resourceLocationSerializable.latitude,resourceLocationSerializable.longitude)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.onCreate(savedInstanceState)
        mapFragment.getMapAsync(this)

        when(requestMap) {
            SHOW_LOCATION_MAP -> {
                bt_set_location.isEnabled = false
                bt_set_location.isVisible = false
            }
            SET_LOCATION_MAP -> {
                bt_set_location.isEnabled = true
                bt_set_location.isVisible = true
            }
        }

        bt_set_location.setOnClickListener {
            savePressed = true
            finish()
        }
    }

    private fun writeLocationPreferences(latitude : Double, longitude : Double, pressed : Boolean){
        val sharedPref : SharedPreferences = getSharedPreferences(MAPS_ACTIVITY_KEY, 0)
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putString(LATITUDE, latitude.toString())
        editor.putString(LONGITUDE, longitude.toString())
        editor.putBoolean(PRESSED, pressed)
        editor.commit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if(ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true

        val isDraggable : Boolean = requestMap == SET_LOCATION_MAP
        val title : String = getTitleRoleType(requestRoleType)
        val iconId : Int = getIconIdRoleType(requestRoleType)

        val markerOptions : MarkerOptions = makeMarkerOptions(draggable = isDraggable, position = resourceLocation, title = title, iconId = iconId)

        resourceMarker = mMap.addMarker(markerOptions)
        resourceMarker.setTag(0)
        
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(resourceLocation, DEFAULT_ZOOM_MAP))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.setOnMyLocationChangeListener(onMyLocation)
        mMap.setOnMyLocationButtonClickListener(onMyLocationButton)
        mMap.setOnMyLocationClickListener(onMyLocationClick)
        mMap.setOnMarkerClickListener(onMarkerClick)
        mMap.setOnMarkerDragListener(onMarkerDragListener)
        }

    private fun getIconIdRoleType(roleType: Int) : Int {
        return when(roleType){
            HELPER -> {
                R.drawable.ic_helper_marker
            }
            APPLICANT -> {
                R.drawable.ic_applicant_marker
            }
            else -> {
                R.mipmap.ic_unknown_marker
            }
        }
    }

    private fun getTitleRoleType(roleType : Int) : String {
        return when(roleType){
            HELPER -> {
                getString(R.string.msg_resource_location)
            }
            APPLICANT -> {
                getString(R.string.msg_wish_resource_location)
            }
            else -> {
                getString(R.string.msg_unknown_resource_location)
            }
        }
    }

    private var onMyLocation : GoogleMap.OnMyLocationChangeListener = object : GoogleMap.OnMyLocationChangeListener {
        override fun onMyLocationChange(location: Location?) {
            if(requestMap == SET_LOCATION_MAP) {
                resourceLocation = LatLng(location!!.latitude, location!!.longitude)
            }
        }
    }

    private var onMyLocationButton : GoogleMap.OnMyLocationButtonClickListener = object : GoogleMap.OnMyLocationButtonClickListener {
        override fun onMyLocationButtonClick(): Boolean {
            if(requestMap == SET_LOCATION_MAP){
                resourceMarker.position = resourceLocation
            }
            return false
        }
    }

    private fun makeMarkerOptions(draggable : Boolean, position : LatLng, title : String, iconId : Int) : MarkerOptions {
        val markerOptions : MarkerOptions = MarkerOptions()
        markerOptions.draggable(draggable)
        markerOptions.position(position)
        markerOptions.title(title)
        val resourceIcon : BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, iconId))
        markerOptions.icon(resourceIcon)
        return markerOptions
    }

    private var onMyLocationClick : GoogleMap.OnMyLocationClickListener = object : GoogleMap.OnMyLocationClickListener {
        override fun onMyLocationClick(myLocation: Location) {
            if(requestMap == SET_LOCATION_MAP){
                resourceLocation = LatLng(myLocation.latitude, myLocation.longitude)
                resourceMarker.remove()
                val iconId : Int = getIconIdRoleType(requestRoleType)
                val title : String = getTitleRoleType(requestRoleType)
                val markerOptions : MarkerOptions = makeMarkerOptions(draggable = true, position = resourceLocation, title = title, iconId = iconId)
                resourceMarker = mMap.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(resourceLocation, DEFAULT_ZOOM_MAP))
            }
        }
    }

    private var onMarkerClick : GoogleMap.OnMarkerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker?): Boolean {
            var clickCount : Int = marker?.tag.toString().toInt()
            if (clickCount != null){
                clickCount += 1
                marker!!.tag = clickCount
            }
            return false
        }
    }

    private var onMarkerDragListener: GoogleMap.OnMarkerDragListener = object : GoogleMap.OnMarkerDragListener {
        override fun onMarkerDragEnd(marker: Marker?) {
            resourceLocation = LatLng(marker!!.position.latitude, marker!!.position.longitude)
        }

        override fun onMarkerDragStart(marker: Marker?) {
        }

        override fun onMarkerDrag(marker: Marker?) {
        }
    }
    
    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }


    override fun onPause() {
         writeLocationPreferences(resourceLocation.latitude, resourceLocation.longitude, savePressed)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}

