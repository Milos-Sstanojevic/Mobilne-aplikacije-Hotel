package elfak.mosis.hotel


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.Place


class MapFragment : Fragment() {

    companion object {
        private const val REQUEST_DETAILS = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 50
        private val CAMERA_POSITION_KEY = "CameraPositionKey"
    }

    private var selectedPlace: com.google.android.libraries.places.api.model.Place? = null
    private var place:Place? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var showDetailsButton: ImageButton
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var addComent:Button
    private lateinit var addHotel:Button
    private lateinit var reserveButton:Button

    private lateinit var auth:FirebaseAuth

    private var savedCameraPosition: CameraPosition? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView=inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map

            if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            }

            googleMap.setOnMarkerClickListener {marker->
                if(marker!=null){
                marker.showInfoWindow()
                true
                }else{
                    false}
            }

            googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
        }

        addComent=rootView.findViewById(R.id.add_comment_button)

        addComent.setOnClickListener {
            val addCommentIntent = Intent(requireContext(), CommentPlaceActivity::class.java)
            Log.d("MESTOO","${place!!.name}")
            addCommentIntent.putExtra("selectedPlace", place!!.name)
            startActivity(addCommentIntent)
        }

        showDetailsButton = rootView.findViewById(R.id.search_button)

        showDetailsButton.setOnClickListener {
            val detailsIntent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(detailsIntent,REQUEST_DETAILS)
        }

        addHotel=rootView.findViewById(R.id.add_hotel_button)

        addHotel.setOnClickListener {
            val addHotelIntent=Intent(requireContext(),AddHotelActivity::class.java)
            startActivity(addHotelIntent)
        }

        reserveButton=rootView.findViewById(R.id.reserve_button)

        reserveButton.setOnClickListener {

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = LatLng(lastLocation!!.latitude, lastLocation.longitude)
                if (savedCameraPosition == null && googleMap.cameraPosition == null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation,20f))
                }
            }
        }
        requestLocationUpdates()



        return rootView
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1500
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            showLocationSettingsDialog()
        }
    }
    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("Please grant location permission to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode== REQUEST_DETAILS && resultCode== Activity.RESULT_OK){
            selectedPlace=data?.getParcelableExtra("selectedPlace")
            val matchingHotels = data?.getSerializableExtra("matchingHotels") as? Hotel
            Log.d("HOTEEELLL","${matchingHotels!!.name}")
            if (selectedPlace != null) {

                val markerOptions = MarkerOptions()
                    .position(selectedPlace!!.latLng!!)
                    .title(selectedPlace!!.name!!)

                val marker=googleMap.addMarker(markerOptions)

                var ratingValue = selectedPlace!!.rating
                if (ratingValue == null) {
                    ratingValue = 0.0
                }

                place = Place(name= selectedPlace!!.name, latLng = selectedPlace!!.latLng, address = selectedPlace!!.address, rating = ratingValue, comment = emptyList())

                marker!!.tag=place
                marker!!.showInfoWindow()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position,15f))

            }
            if(matchingHotels!=null){

                    val markerOptions = MarkerOptions()
                        .position(LatLng(matchingHotels.latitude, matchingHotels.longitude))
                        .title(matchingHotels.name)
                        .snippet("Address is: "+ matchingHotels.address +"\nNumber of current guests is: "+ matchingHotels.currentGuests +"\nRating of hotel is: "+ matchingHotels.rating +"\n Comments: "+ matchingHotels.comments) // Add additional information if needed
                    val marker = googleMap.addMarker(markerOptions)
                    marker?.tag = matchingHotels
                    marker?.showInfoWindow()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedCameraPosition = savedInstanceState?.getParcelable(CAMERA_POSITION_KEY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition)
    }

    override fun onResume() {
        super.onResume()

        savedCameraPosition?.let { position ->
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
            savedCameraPosition = null
        }
    }

    override fun onPause() {
        super.onPause()

        savedCameraPosition = googleMap.cameraPosition
    }


//    private fun addMarkers(googleMap: GoogleMap){
//        places.forEach{place->
//            val marker= googleMap.addMarker(
//                MarkerOptions().title(place.name).position(place.latLng)
//            )
//            marker!!.tag=place
//        }
//
//        googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
//    }

//    private val places: List<Place> by lazy {
//        PlacesReader(requireContext()).read()
//    }
}