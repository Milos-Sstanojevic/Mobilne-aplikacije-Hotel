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
import android.widget.Toast
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.Place


class MapFragment : Fragment() {

    companion object {
        private const val REQUEST_DETAILS = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 50
        private val CAMERA_POSITION_KEY = "CameraPositionKey"
    }

    private var selectedPlace: com.google.android.libraries.places.api.model.Place? = null
    private var place: Place? = null
    private var matchingHotels: Hotel? = null
    private var radiusHotels: List<Hotel>?=null

    private lateinit var locationRequest: LocationRequest
    private lateinit var searchButton: ImageButton
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var addComent: Button
    private lateinit var addHotel: Button
    private lateinit var reserveButton: Button
    
    private lateinit var database: FirebaseDatabase
    private var savedCameraPosition: CameraPosition? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        database = FirebaseDatabase.getInstance()

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map

            if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            }

            googleMap.setOnMarkerClickListener { marker ->
                if (marker != null) {
                    marker.showInfoWindow()
                    true
                } else {
                    false
                }
            }

            googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
        }

        addComent = rootView.findViewById(R.id.add_comment_button)

        addComent.setOnClickListener {
            val addCommentIntent = Intent(requireContext(), CommentPlaceActivity::class.java)
            if (matchingHotels != null) {
                Log.d("MESTOO", "${matchingHotels!!.name}")
                addCommentIntent.putExtra("selectedPlace", matchingHotels!!.name)
                startActivity(addCommentIntent)
            } else if (place != null) {
                Log.d("MESTOO", "${place!!.name}")
                addCommentIntent.putExtra("selectedPlace", place!!.name)
                startActivity(addCommentIntent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "You have to pick a place to comment",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        searchButton = rootView.findViewById(R.id.search_button)

        searchButton.setOnClickListener {
            val detailsIntent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(detailsIntent, REQUEST_DETAILS)
        }

        addHotel = rootView.findViewById(R.id.add_hotel_button)

        addHotel.setOnClickListener {
            val addHotelIntent = Intent(requireContext(), AddHotelActivity::class.java)
            startActivity(addHotelIntent)
        }

        reserveButton = rootView.findViewById(R.id.reserve_button)

        reserveButton.setOnClickListener {
            val reserveIntent = Intent(requireContext(), ReserveActivity::class.java)
            if (matchingHotels != null) {
                reserveIntent.putExtra("selectedPlace", matchingHotels!!.name)
                startActivity(reserveIntent)
            } else if (place != null) {
                Toast.makeText(
                    requireContext(),
                    "It has to be a place that some of the users added",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "You have to pick a place to reserve",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = LatLng(lastLocation!!.latitude, lastLocation.longitude)
                if (savedCameraPosition == null && googleMap.cameraPosition == null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 20f))
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
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DETAILS && resultCode == Activity.RESULT_OK) {

            selectedPlace = data?.getParcelableExtra("selectedPlace")
            matchingHotels = data?.getSerializableExtra("matchingHotels") as? Hotel
            radiusHotels=data?.getSerializableExtra("radiusHotels") as? List<Hotel>


            if (selectedPlace != null) {

                var comments: String? = ""

                val databaseRef = database.reference

                val commentsRef =
                    databaseRef.child("places").child(selectedPlace!!.name).child("comments")

                commentsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments = ""
                        for (commentSnapshot in snapshot.children) {
                            val userName =
                                commentSnapshot.child("userName").getValue(String::class.java)
                            val commentText =
                                commentSnapshot.child("comment").getValue(String::class.java)


                            Log.d("VIEWWW", "$userName : $commentText")

                            if (userName != null && commentText != null) {
                                comments += "$userName:$commentText \n"
                                Log.d("VIEWWW", "$comments")
                            }
                        }


                        val markerOptions = MarkerOptions()
                            .position(selectedPlace!!.latLng!!)
                            .title(selectedPlace!!.name!!)

                        val marker = googleMap.addMarker(markerOptions)

                        var ratingValue = selectedPlace!!.rating
                        if (ratingValue == null) {
                            ratingValue = 0.0
                        }

                        place = Place(
                            name = selectedPlace!!.name,
                            latLng = selectedPlace!!.latLng,
                            address = selectedPlace!!.address,
                            rating = ratingValue,
                            comment = comments!!
                        )

                        marker!!.tag = place
                        marker!!.showInfoWindow()
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(place!!.latLng, 18f)
                        googleMap.animateCamera(cameraUpdate)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        error.toException().printStackTrace()
                    }
                })

            }
            if (matchingHotels != null) {
                var comments: String? = ""

                val databaseRef = database.reference

                val commentsRef =
                    databaseRef.child("places").child(matchingHotels!!.name).child("comments")

                commentsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments = ""
                        for (commentSnapshot in snapshot.children) {
                            val userName =
                                commentSnapshot.child("userName").getValue(String::class.java)
                            val commentText =
                                commentSnapshot.child("comment").getValue(String::class.java)


                            Log.d("VIEWWW", "$userName : $commentText")

                            if (userName != null && commentText != null) {
                                comments += "$userName:$commentText \n"
                                Log.d("VIEWWW", "$comments")
                            }
                        }

                        matchingHotels!!.comments = comments!!

                        Log.d("HOTELLS COMM", matchingHotels!!.comments)

                        val markerOptions = MarkerOptions()
                            .position(LatLng(matchingHotels!!.latitude, matchingHotels!!.longitude))
                            .title(matchingHotels!!.name)

                        val marker = googleMap.addMarker(markerOptions)
                        marker?.tag = matchingHotels
                        marker?.showInfoWindow()

                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(matchingHotels!!.latitude,matchingHotels!!.longitude), 18f)
                        googleMap.animateCamera(cameraUpdate)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        error.toException().printStackTrace()
                    }
                })
            }

            if(radiusHotels!=null){
                var comments: String? = ""

                val databaseRef = database.reference

                Log.d("RADIUSSS","${radiusHotels!!.size}")

                googleMap.clear()

                for(hotel in radiusHotels!!){

                    val commentsRef =
                        databaseRef.child("places").child(hotel.name).child("comments")

                    commentsRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            comments = ""
                            for (commentSnapshot in snapshot.children) {
                                val userName =
                                    commentSnapshot.child("userName").getValue(String::class.java)
                                val commentText =
                                    commentSnapshot.child("comment").getValue(String::class.java)


                                Log.d("VIEWWW", "$userName : $commentText")

                                if (userName != null && commentText != null) {
                                    comments += "$userName:$commentText \n"
                                    Log.d("VIEWWW", "$comments")
                                }
                            }

                            hotel.comments=comments!!

                            val markerOption=MarkerOptions()
                                .position(LatLng(hotel.latitude,hotel.longitude))
                                .title(hotel.name)

                            val marker=googleMap.addMarker(markerOption)
                            marker?.tag=hotel
                            marker?.showInfoWindow()
                        }
                        override fun onCancelled(error: DatabaseError) {
                            error.toException().printStackTrace()
                        }
                    })
                }
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
}