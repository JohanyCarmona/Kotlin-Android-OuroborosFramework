package com.example.ouroboros.intent

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class LocationSerializable(resourceLocation : LatLng) : Serializable {
    val latitude : Double = resourceLocation.latitude
    val longitude : Double = resourceLocation.longitude
}