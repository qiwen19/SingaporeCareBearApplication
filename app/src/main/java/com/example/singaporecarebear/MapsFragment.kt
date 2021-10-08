package com.example.singaporecarebear

import android.Manifest
import android.app.*
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.beust.klaxon.*
import com.example.singaporecarebear.GeoFencing.ReminderRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.RemoteMessage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.net.URL
import java.util.*

class MapsFragment() : Fragment(), OnMapReadyCallback, PermissionListener {

    private val NOTIFICATION_CHANNEL_ID = "help_requesting"
    var notificationManager: NotificationManager? = null

    // Google map component
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //Shared preference component
    private lateinit var sharePreferenceSettings: SharedPreferences

    private var postalAddressEditText: EditText? = null
    private var helpEditText: EditText? = null
    private var searchButton: Button? = null
    private var requestButton: Button? = null
    private var locateBtnPressed = false
    private var location_latLng: LatLng? = null
    private var notiButton: Button? = null
    private lateinit var database: FirebaseFirestore
    private var testLocation: Location? = null
    private var userId: String? = null
    private var cancelBtnContainer: RelativeLayout? = null

    private val mCurrentId = FirebaseAuth.getInstance().uid
//    private lateinit var mCurrentFullName : String

    //set random binded service to mService variable
    private var mService: RequesterObserverService? = null
    //set connection status to mBound (true for success / false for failure)
    private var mBound: Boolean = false

    //set random binded service to mService variable
    private var hService: HelperObserverService? = null
    //set connection status to mBound (true for success / false for failure)
    private var hBound: Boolean = false

    //Progress Bar
    //private lateinit var progressBar: ProgressBar

    //Requester variables
    private var requestedMarker: Marker? = null

    //Polyline variables
    private var polyLine: Polyline? = null
    private var destinationLocationMarker: Marker? = null

    //Geofence variables
    private var destinationCircle: Circle? = null

    //firebase variables
    private lateinit var db: FirebaseFirestore

    //selectedMarkers to redraw route
    private var markerPosition :LatLng? = null
    private var tempMarkerPosition :LatLng? = null

    //request observer
    private lateinit var userRequestObs: ListenerRegistration
    private lateinit var requestLocationObs: ListenerRegistration

    private var imageURL = ArrayList<String>()
    private var userNameStorage = ArrayList<String>()
    private var locationLatsStorage = ArrayList<String>()
    private var locationLngsStorage = ArrayList<String>()



    private var myContext: FragmentActivity? = null

    //test
    private lateinit var cancelBtn :Button

    companion object {
        var mapFragment: SupportMapFragment? = null
        const val REQUEST_CHECK_SETTINGS = 43
    }

    override fun onAttach(activity: Activity) {
        myContext = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.activity_maps, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        fusedLocationProviderClient = FusedLocationProviderClient(this.requireActivity())

        //progressBar = rootView.findViewById((R.id.progressBar))
        cancelBtnContainer = rootView.findViewById(R.id.cancelBtnContainer)

        return rootView
    }

    private fun initialise() {
        postalAddressEditText = addressEditText
        helpEditText = helpRequiredEditText
        searchButton = searchBtn
        requestButton = sendRequestBtn
        database = FirebaseFirestore.getInstance()
        //Initialise firebase variable
        db = FirebaseFirestore.getInstance()


        //Initialise shared preference variables
        sharePreferenceSettings =
            activity!!.applicationContext.getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")
    }

    //    override fun onResume(){
//    super.onResume()
//    if(mMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
//        mMap.clear()
//
//        // add markers from database to the map
//    }
//}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        initialise()

        //Initialise variables
        val sharedPreferences = this.activity!!.getSharedPreferences("savePrefs", 0)
        userId = sharedPreferences.getString("userId", "")

        //Hide the send request button before locate button is press
        sendRequestBtn.visibility = View.VISIBLE

        //retrieveRequestedLocations()

        //Get shared preference
//        val sharePreferenceSettings = this.getActivity()!!.getSharedPreferences("savePrefs", 0)
//        val userId = sharePreferenceSettings.getString("userId", "")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager =
                myContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SG-Care Bear Application",
                "Help me help you"
            )
            Log.d("mnb","notification channel created")
        }

        postalAddressEditText!!.setText("My Location")

        val broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                Log.d("rrr","received intent from broadcast --> " +intent!!.action)
                if(intent.action =="requesterResetMap"){
                    if(intent.getBooleanExtra("requesterResetMap",false)){
                        Log.d("rrr","inside requester intent broadcast")
                        mMap.clear()
                        repopulateTheMarkers()
                        if(mBound) {
                            myContext!!.unbindService(connection)
                            mBound = false
                            Log.d("rrr","successfully unbinded")
                        }
                    }
                }
                else if(intent.action =="resetMapHelper"){
                    if(intent.getBooleanExtra("helperResetMap",false)){
                        Log.d("zzz","helper reset map")
                        mMap.clear()
                        repopulateTheMarkers()
                        if(hBound){
                            myContext!!.unbindService(helperConnection)
                            hBound=false
                            Log.d("hhh","successfully unbinded")
                        }
                    }
                }
                else if(intent.action =="hideCancelButton"){
                    if(intent.getBooleanExtra("hideCancelButton",false)){
                        Log.d("zzz","hide cancel button")
                        cancelBtnContainer!!.visibility= View.GONE
                    }
                }
                else if(intent.action =="deleteRoute"){
                    if(intent.getBooleanExtra("deleteRoute",false)){
                        markerPosition = null
                        Log.d("zzz","markerPosition 2 --> $markerPosition")
                        cancelBtnContainer!!.visibility= View.GONE
                        val toast = Toast.makeText(myContext, "You have been awarded 99 points", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
                else if(intent.action =="removePolyRouteAndCancelBtn"){
                    if(intent.getBooleanExtra("removePolyRouteAndCancelBtn",false)){
                        Log.d("www","hide my marker & cancel button")
                        //do not draw route
                        markerPosition = null
                        //hide cancel button
                        cancelBtnContainer!!.visibility= View.GONE
                    }
                }
            }
        }


        val filter = IntentFilter()
        filter.addAction("requesterResetMap")
        filter.addAction("resetMapHelper")
        filter.addAction("hideCancelButton")
        filter.addAction("deleteRoute")
        filter.addAction("removePolyRouteAndCancelBtn")
        LocalBroadcastManager.getInstance(activity!!.applicationContext)
            .registerReceiver(broadCastReceiver, filter)

        //populate maps with marker and set on clickable
        observeChangesInRequestLocationCollection()

        //observe if user owns a request, if yes display cancel button
        ifRequestExistsObserver()

        requestButton!!.setOnClickListener {
            if (locateBtnPressed) {
                val userRefDoc = db.collection("users").document(userId!!)
                userRefDoc.get()
                    .addOnSuccessListener { document ->
                        if (document.contains("imageUrl")) {
                            //User current Location
                            val from = location_latLng
                            //Store the request location after requested for help
                            storeRequestLocation(from!!, userId!!)
                            requestLayout.visibility = View.INVISIBLE
                            generateCancelBtn("requester")
                            //initialize observer for requester ( will reset map after being helped)
                            //observeDocumentForRequesterToResetMap()

//                        val intent = Intent(this.requireActivity(),RequesterObserverService()::class.java)
//                        myContext!!.bindService(intent,connection, Context.BIND_AUTO_CREATE)
//                        Log.d("hhh","connection should be set")
                            //notification()

                            //initialize observer to track user location and notify requester when helper is closeby
                            //observeHelperLocationStatus()
                        }
                        else {
                            Toast.makeText(this.requireActivity(), "Kindly set up a profile picture first so it will be easier for others to help you", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this.requireActivity(), "Kindly locate your location first", Toast.LENGTH_LONG).show()
            }
        }

        //Request help btn action
        helpFeatures.setOnClickListener {
            val requestLayout = getView()!!.findViewById<RelativeLayout>(R.id.requestLayout)
            if (requestLayout.visibility == View.VISIBLE) {
                requestLayout.visibility = View.INVISIBLE
            } else {
                requestLayout.visibility = View.VISIBLE
            }
        }

    }

    //TODO implement the menu items
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu to use in the action bar
        if(isAdded && activity != null) {
            inflater!!.inflate(R.menu.menu_main, menu)
            super.onCreateOptionsMenu(menu, inflater)
        }
    }

    // onclick notification icon
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(isAdded && activity != null) {
            // Handle action bar item clicks here.
            val id = item.itemId

            if (id == R.id.notification) {
                val intent = Intent(this.requireActivity(), NotificationActivity::class.java)
                startActivity(intent)
                activity!!.finish()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun validateEditText(postalAddressEditText: String, helpEditText: String): Boolean {
        return if (postalAddressEditText.isEmpty() && helpEditText.isEmpty()) {
            Toast.makeText(this.requireActivity(), "Please enter a Valid Value", Toast.LENGTH_SHORT)
                .show()
            false
        } else if (postalAddressEditText.isEmpty()) {
            Toast.makeText(
                this.requireActivity(),
                "Please enter a Postal Address",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else if (helpEditText.isEmpty()) {
//            Toast.makeText(this.myContext, mCurrentFullName, Toast.LENGTH_LONG).show()
            Toast.makeText(this.requireActivity(), "Please enter a Help Request", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.context,
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

    override fun onMapReady(googleMap: GoogleMap) {
        if(isAdded() && activity != null) {
            //progressBar.visibility = View.GONE
            mMap = googleMap
            setMapStyle(mMap)
            if (isPermissionGiven()) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.uiSettings.isZoomControlsEnabled = true

                //This is needed for locate button to even be clickable
                getCurrentLocation()
                //move camera to correct position
                getImmediateLocation()
            } else {
                givePermission()
            }
        }
    }

    private fun isPermissionGiven(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun givePermission() {
        Dexter.withActivity(this.requireActivity())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        //This is needed for locate button to even be clickable
        getCurrentLocation()
        //move camera to correct position
        getImmediateLocation()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        token!!.continuePermissionRequest()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(
            this.requireContext(),
            "Permission required for showing location",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getCurrentLocation() {

        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = 1000

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val result = LocationServices.getSettingsClient(this.requireActivity())
            .checkLocationSettings(locationSettingsRequest)
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)

                // initialise edit text fields
                initialise()
                if(postalAddressEditText != null){
                    postalAddressEditText!!.setText("My Location")
                }

                if(searchButton != null){
                    searchButton!!.setOnClickListener {

                        sendRequestBtn.visibility = View.VISIBLE

                        mMap.clear()

                        if (validateEditText(
                                postalAddressEditText!!.text.toString(),
                                helpEditText!!.text.toString()
                            )
                        ) {
                            if (postalAddressEditText!!.text.toString() == "My Location") {
                                if (response!!.locationSettingsStates.isLocationPresent) {
                                    getLastLocation("My Location")
                                }
                            } else {
                                getLastLocation(postalAddressEditText!!.text.toString())
                            }
                        }
                    }
                }

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(
                            this.requireActivity(),
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                    } catch (e: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }



    private fun getImmediateLocation(){
        var address = "No known address"
        var addresses: List<Address>? = null
        val gcd = Geocoder(this.requireContext(), Locale.getDefault())
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val mLastLocation = task.result
                    try {
                        addresses = gcd.getFromLocation(
                            mLastLocation!!.latitude,
                            mLastLocation.longitude,
                            1
                        )
                        if (addresses!!.isNotEmpty()) {
                            address = addresses!![0].getAddressLine(0)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()

                        // state that user location is not found
                        locateBtnPressed = false
                    }

                    // state that user location is found
                    locateBtnPressed = true

                    // store lat long in global
                    location_latLng =
                        (LatLng(mLastLocation!!.latitude, mLastLocation.longitude))

                    //move camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location_latLng, 14.0f))
                }
            }

    }
    private fun getLastLocation(location: String) {

        var address = "No known address"
        var addresses: List<Address>? = null
        val gcd = Geocoder(this.requireContext(), Locale.getDefault())

        // If location get from GPS
        if (location == "My Location") {
            fusedLocationProviderClient.lastLocation
                .addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        val mLastLocation = task.result
                        try {

                            addresses = gcd.getFromLocation(
                                mLastLocation!!.latitude,
                                mLastLocation.longitude,
                                1
                            )
                            if (addresses!!.isNotEmpty()) {
                                address = addresses!![0].getAddressLine(0)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()

                            // state that user location is not found
                            locateBtnPressed = false
                        }

                        // state that user location is found
                        locateBtnPressed = true

                        // store lat long in global
                        location_latLng =
                            (LatLng(mLastLocation!!.latitude, mLastLocation.longitude))
                        Log.d("checkMarker", "hello i am here too")
                        requestedMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                                .title(userId)
                                .snippet(address)
                                .icon(
                                    bitmapDescriptorFromVector(
                                        this.requireContext(),
                                        R.drawable.ic_pin
                                    )
                                )
                        )

                        val cameraPosition = CameraPosition.Builder()
                            .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                            .zoom(17f)
                            .build()
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    } else {
                        // clear global lat long if location not found
                        location_latLng = null

                        // state that user location is not found
                        locateBtnPressed = false

                        Toast.makeText(
                            this.requireContext(),
                            "No current location found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            // If location get from TextEdit
            try {
                addresses = gcd.getFromLocationName(location, 1)
            } catch (e: IOException) {
                // clear global lat long if location not found
                location_latLng = null

                // state that user location is not found
                locateBtnPressed = false

                e.printStackTrace()
            }

            if (addresses!!.isEmpty()) {
                // clear global lat long if location not found
                location_latLng = null

                // state that user location is not found
                locateBtnPressed = false

                Toast.makeText(
                    this.requireContext(),
                    "Sorry, invalid address. Please enter a valid address",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // state that user location is found
                locateBtnPressed = true

                val addressList = addresses!![0]

                // store lat long in global
                location_latLng = LatLng(addressList.latitude, addressList.longitude)
                val latLng = LatLng(addressList.latitude, addressList.longitude)
                Log.d("checkMarker", "hello i am here")
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(userId)
                        .snippet(addressList.getAddressLine(0))
                        .icon(bitmapDescriptorFromVector(this.requireContext(), R.drawable.ic_pin))
                )
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(17f)
                    .build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

        }

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getCurrentLocation()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateCancelBtn(roleType: String) {
        if(isAdded && activity != null) {
            if (cancelBtnContainer!!.visibility == View.GONE && (::cancelBtn.isInitialized)) {
                //Log.d("rrr","display cancel button")
                cancelBtnContainer!!.visibility = View.VISIBLE
            } else {
                //Log.d("rrr","create a cancel button")
                cancelBtn = Button(this.context)
                cancelBtn.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                cancelBtn.setTag("cancelBtn")
                cancelBtn.text = "Cancel"
                cancelBtn.setBackgroundColor(Color.rgb(160, 44, 29))
                cancelBtn.setTextColor(Color.WHITE)
                cancelBtnContainer!!.addView(cancelBtn)
                cancelBtnContainer!!.visibility= View.VISIBLE
                Log.d("zzz", "cancel button displayed here")
                if (roleType == "requester") {
                    cancelBtn.setOnClickListener {
                        cancelRequest(userId!!)
                        cancelBtnContainer!!.removeView(cancelBtn)
                        requestLayout.visibility = View.VISIBLE
                    }
                } else if (roleType == "helper") {
                    cancelBtn.setOnClickListener {
                        cancelHelpingOther()
                        cancelBtnContainer!!.removeView(cancelBtn)
                        requestLayout.visibility = View.VISIBLE
                        markerPosition = null
                    }
                }
            }
        }
    }

    // get Route

    private fun getRoutePath(destination: LatLng, currentPosition: LatLng) {
        //Set destination marker
        //destinationLocationMarker =
        //mMap!!.addMarker(MarkerOptions().position(destination).title("Destination position"))

        //Set up the latlngbounds builder
        val LatLongB = LatLngBounds.Builder()

        //Set up the polyline color etc
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)

        //Get the path url
        val url = getURL(currentPosition, destination)
        async {
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                //val routes = json.array<JsonObject>("routes")
                val routes = json.array<JsonObject>("routes")!!
                val legs = routes[0]["legs"] as JsonArray<JsonObject>
                val points = legs[0]["steps"] as JsonArray<JsonObject>
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!) }
                // Add  points to polyline and bounds
                options.add(currentPosition)
                LatLongB.include(currentPosition)
                for (point in polypts) {
                    options.add(point)
                    LatLongB.include(point)
                }
                options.add(destination)
                LatLongB.include(destination)
                // build bounds
                val bounds = LatLongB.build()
                //Store polyline
                polyLine = mMap.addPolyline(options)
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                // add polyline to the map
                //mMap!!.addPolyline(options)
            }
        }
    }

    private fun getURL(from: LatLng, to: LatLng): String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val key = "key=AIzaSyCh2kveXO8DzDcV8HhEr6wb9DS7mjSmFOw"
        val params = "$origin&$dest&$key"
        Log.d("URL", "https://maps.googleapis.com/maps/api/directions/json?$params")
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

    private fun storeRequestLocation(location: LatLng, userId: String) {
        val locationLat = location.latitude.toString()
        val locationLng = location.longitude.toString()
        val requestHelpInfo = HashMap<String, Any>()
        requestHelpInfo["Lat"] = locationLat
        requestHelpInfo["Lng"] = locationLng
        requestHelpInfo["taskDescription"] = helpEditText!!.text.toString()

        //Get user name and profile image
        val userDocRef = database.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    requestHelpInfo.put("fullname", document.data?.getValue("fullname").toString())
                    if (document.contains("imageUrl")) {
                        requestHelpInfo.put(
                            "imageUrl",
                            document.data!!.getValue("imageUrl").toString()
                        )
                    }
                } else {
                    Log.d("No such document Error", "No such document")
                }
                database.collection("requestLocation").document(userId).set(requestHelpInfo)
                    .addOnSuccessListener {
                        if(isAdded() && activity != null){
                            Toast.makeText(
                                this.requireActivity(),
                                "Request made successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this.requireActivity(),RequesterObserverService()::class.java)
                            myContext!!.bindService(intent,connection, Context.BIND_AUTO_CREATE)
                            Log.d("hhh","connection should be set")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this.requireActivity(),
                            "Request failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                if (isAdded() && activity != null) {
                    Toast.makeText(
                        this.requireActivity(),
                        "Request failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun cancelRequest(userId: String) {
        database.collection("requestLocation").document(userId).delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this.requireActivity(),
                    "Cancel request successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                if (requestedMarker != null) {
                    requestedMarker!!.remove()
                }
                mMap.addMarker(
                    MarkerOptions()
                        .position(location_latLng!!)
                        .title(userId)
                        //.snippet(address)
                        .icon(
                            bitmapDescriptorFromVector(
                                this.requireContext(),
                                R.drawable.ic_pin
                            )
                        )
                ).remove()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this.requireActivity(),
                    "Cancel request unsuccessful! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun acceptedToHelp(requesterId: String, helperId: String) {
        val db = FirebaseFirestore.getInstance()
        val helperInfo = HashMap<String, Any>()
        helperInfo["helperId"] = helperId
        helperInfo["arrivalStatus"] = false
        val requestedLocationDocRef = db.collection("requestLocation").document(requesterId)
        requestedLocationDocRef.update(helperInfo)
        generateCancelBtn("helper")
    }

    private fun showHelpDialogBox(markerLatLng: LatLng, requesterId: String) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.confirmation_dialog, null)
        //initalise xml elements
        val requesterNameField = mDialogView.findViewById<TextView>(R.id.requesterNameField)
        val taskField = mDialogView.findViewById<TextView>(R.id.taskField)
        val helpBtn = mDialogView.findViewById<Button>(R.id.helpBtn)
        val rejectBtn = mDialogView.findViewById<Button>(R.id.rejectBtn)
        val requesterProfile =  mDialogView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.requesterIcon)
        val title =  mDialogView.findViewById<TextView>(R.id.title)

        helpBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))
        rejectBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))

        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)
            .setCancelable(false)
        //get user details
        val docRef = db.collection("requestLocation").document(requesterId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val requesterName = document.data?.getValue("fullname").toString()
                    val requesterProfileImage = document.data?.getValue("imageUrl").toString()
                    val taskDescription = document.data?.getValue("taskDescription").toString()
                    title.text = "Assistance Requested"
                    requesterNameField.text = requesterName
                    taskField.text = "Issues faced: $taskDescription"
                    //Picasso.get().load(requesterProfileImage).fit().centerCrop()
                    //    .into(requesterProfile)
                    Picasso.get().load(requesterProfileImage).fit().centerCrop().into(requesterProfile)

                    //show dialog after all information populated
                    val  mAlertDialog = mBuilder.show()

                    helpBtn.setOnClickListener() {
                        //check here if user cancelled his request
                        db.collection("requestLocation").document(requesterId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    markerPosition = tempMarkerPosition
                                    acceptedToHelp(requesterId, userId!!)
                                    val editor = sharePreferenceSettings.edit()
                                    Log.d("requesterIdMap", requesterId)
                                    editor.putString("requesterId", requesterId)
                                    editor.commit()
                                    //testing
                                    //observeDocumentForUserHelpingOthers(requesterId)
                                    val intent =
                                        Intent(myContext, HelperObserverService()::class.java)
                                    myContext!!.bindService(
                                        intent,
                                        helperConnection,
                                        Context.BIND_AUTO_CREATE
                                    )
                                    Log.d("fff", "connection should be set")

                                    //Add geofence
                                    val repository = ReminderRepository(context!!)
                                    repository.remove("Destination")
                                    repository.add(markerLatLng, failure = {
                                        //                            Snackbar.make(login, it, Snackbar.LENGTH_LONG).show()
                                    }
                                    )
                                    //Remove previous geofence circle
                                    if (destinationCircle != null) {
                                        destinationCircle!!.remove()
                                    }

                                    Log.d("qqq", "circle has been drawn")
                                    destinationCircle = mMap.addCircle(
                                        CircleOptions()
                                            .center(markerLatLng)
                                            .radius(500.0)
                                            .strokeColor(
                                                ContextCompat.getColor(
                                                    context!!,
                                                    R.color.colorAccent
                                                )
                                            )
                                            .fillColor(
                                                ContextCompat.getColor(
                                                    context!!,
                                                    R.color.colorPrimary
                                                )
                                            )
                                    )

                                    //Remove the previous polyline
                                    if (polyLine != null) {
                                        polyLine!!.remove()

                                    }

                                    if (destinationLocationMarker != null) {
                                        destinationLocationMarker!!.remove()
                                    }
                                    fusedLocationProviderClient.lastLocation
                                        .addOnCompleteListener(this.requireActivity())
                                        { task ->
                                            if (task.isSuccessful && task.result != null) {
                                                val currentLocation = task.result!!
                                                val userCurrentLocationLatLng = LatLng(
                                                    currentLocation.latitude,
                                                    currentLocation.longitude
                                                )
                                                Log.d("99ii9"," if run first");
                                                getRoutePath(
                                                    markerPosition!!,
                                                    userCurrentLocationLatLng

                                                )
                                                mAlertDialog.dismiss()
                                            }
                                        }

                                } else {
                                    Log.d("99ii9"," else run first");
                                    markerPosition = null;
                                    Toast.makeText(
                                        this.requireActivity(),
                                        "request doesnt exist",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }

                    rejectBtn.setOnClickListener() {
                        markerPosition = null
                        mAlertDialog.dismiss()
                    }

                }else{
                    Toast.makeText(this.requireActivity(), "request doesnt exist", Toast.LENGTH_LONG).show()
                }
            }
    }

    //if request exist display cancel button
    //TAKE NOTE, observers can be set on non existent documents,
    private fun ifRequestExistsObserver() {
        val docRef = db.collection("requestLocation").document(userId!!)
        userRequestObs = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) { //snapshot will NOT BE NULL even if document doesnt exist
                if (snapshot.exists()) { //if this document exists
                    //Log.d("rrr", "userId ==> $userId, snapshot exists")
                    generateCancelBtn("requester")
                } else {//document that doesnt exist will enter here{
                    //Log.d("rrr", "this document doesnt exist")
                    cancelBtnContainer!!.visibility = View.GONE
                }
            }
        }
    }


    private fun cancelHelpingOther(){
        //Get requester ID from shared preferences
        val requesterId = sharePreferenceSettings.getString("requesterId", "")!!

        //Remove the requester Id after helping
        sharePreferenceSettings.edit().remove("requesterId").apply()

        //Search the document and remove the relavent fields
        val deleteInfo = HashMap<String, Any>()
        deleteInfo["helperId"] = FieldValue.delete()
        deleteInfo["arrivalStatus"] = FieldValue.delete()
        FirebaseFirestore.getInstance()
            .collection("requestLocation")
            .document(requesterId)
            .update(deleteInfo)

        //Delete Line
        if (polyLine != null) {
            polyLine!!.remove()
        }

        //Delete Geofence
        if (destinationCircle != null) {
            destinationCircle!!.remove()
        }
    }

    //observes request location collection
    //repopulate markers & routes
    private fun observeChangesInRequestLocationCollection() {
        //var documentWasAdded: Int = 0 cannot put here because this value wont be called again after first initalise
        userNameStorage = ArrayList<String>()
        imageURL = ArrayList<String>()
        locationLngsStorage = ArrayList<String>()
        locationLatsStorage = ArrayList<String>()
        var firsttime = true
        //whenever there is a new document, add its respective marker
        val docRef = db.collection("requestLocation")
        requestLocationObs = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }
            //as long as something was added
            if (snapshot != null) {
                //first time running the obs just dont do anything, i dnt want to receive notification
                if (firsttime == true) {
                    firsttime = false
                } else if (firsttime == false) {
                    var requesterName = ""
                    var requesterTaskDescription = ""
                    var documentWasAdded: Int =
                        0 //must put here so that value will be reset everytime observer observes a change
                    var docId = ""
                    for (dc in snapshot!!.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            documentWasAdded = 1
                            requesterName = dc.document.data.getValue("fullname").toString()
                            requesterTaskDescription =
                                dc.document.data.getValue("taskDescription").toString()
                            docId = dc.document.id
                        }
                    }
                    if (documentWasAdded == 1 && docId != userId) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendNotification(requesterName, requesterTaskDescription)
                        } else {
                            showNotification(requesterName, requesterTaskDescription)
                        }
                    }
                }
                //send notification
                //test
                mMap.clear()
                Log.d("www", "map erased here")
                //redraw route path & geofence that may have been deleted due to map clear when a user cancels a request
                if (markerPosition != null) {
                    Log.d("www", "markerPosition 1 --> $markerPosition")
                    fusedLocationProviderClient.lastLocation
                        .addOnCompleteListener(this.requireActivity())
                        { task ->
                            if (task.isSuccessful && task.result != null) {
                                var currentLocation = task.result!!
                                var userCurrentLocationLatLng = LatLng(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                )
                                //cheat way of solving for now ( observer code running before broadcast so even though
                                // marker position has been set to null in broadcast, observer check first and see that marker
                                // position is not null, causing the system to crash when it try to delete a non existent marker
                                Log.d("99ii9","i should be last");
                                if (markerPosition != null) {
                                    getRoutePath(markerPosition!!, userCurrentLocationLatLng)

                                    //draw geofence circle
                                    destinationCircle = mMap.addCircle(
                                        CircleOptions()
                                            .center(markerPosition)
                                            .radius(500.0)
                                            .strokeColor(
                                                ContextCompat.getColor(
                                                    context!!,
                                                    R.color.colorAccent
                                                )
                                            )
                                            .fillColor(
                                                ContextCompat.getColor(
                                                    context!!,
                                                    R.color.colorPrimary
                                                )
                                            )
                                    )
                                }
                            } else {
                                Toast.makeText(
                                    this.requireActivity(),
                                    "Failed to get current location",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
                for (document in snapshot) {
                    if (document != null) {
                        if (isAdded() && activity != null) {
                            Log.d("hhh", "there is a change in request location firebase")
                            //generate a marker
                            //Get user id for storing on marker
                            val requesterId = document.id
//                            locationLatsStorage.add(document.data.getValue("Lat").toString())
//                            locationLngsStorage.add(document.data.getValue("Lng").toString())
//                            userNameStorage.add(document.data.getValue("fullname").toString())
//                            if (document.contains("imageUrl")) {
//                                imageURL.add(document.data.getValue("imageUrl").toString())
//                            } else {
//                                imageURL.add("null")
//                            }
                            val latitude = document.data.getValue("Lat").toString().toDouble()
                            val longitute = document.data.getValue("Lng").toString().toDouble()
                            if (requesterId == userId) {
                                requestedMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(latitude, longitute))
                                        .title(requesterId)
                                        //.snippet(address)
                                        .icon(
                                            bitmapDescriptorFromVector(
                                                this.requireContext(),
                                                R.drawable.ic_pin
                                            )
                                        )
                                )
                            } else {
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(latitude, longitute))
                                        .title(requesterId)
                                        //.snippet(address)
                                        .icon(
                                            bitmapDescriptorFromVector(
                                                this.requireContext(),
                                                R.drawable.ic_pin
                                            )
                                        )
                                )
                            }
                        }


                        mMap.setOnMarkerClickListener { marker ->
                            if (marker.isInfoWindowShown) {
                                marker.hideInfoWindow()
                            } else {
                                //marker.title contains the requester id
                                showHelpDialogBox(marker!!.position, marker.title)

                                //Remove the previous polyline
                                if (polyLine != null) {
                                    polyLine!!.remove()
                                    markerPosition = null
                                }

                                if (destinationLocationMarker != null) {
                                    destinationLocationMarker!!.remove()
                                }
                                fusedLocationProviderClient.lastLocation
                                    .addOnCompleteListener(this.requireActivity())
                                    { task ->
                                        if (task.isSuccessful && task.result != null) {
                                            val currentLocation = task.result!!
                                            val userCurrentLocationLatLng = LatLng(
                                                currentLocation.latitude,
                                                currentLocation.longitude
                                            )
                                            tempMarkerPosition = marker.position;
                                            //markerPosition = marker.position
//
                                            Log.d("zzz", "drawing route here")
                                        }
                                    }
                            }
                            true
                        }

                    }
                }
            }
        }
    }

    //repopulate markers without setting observer
    private fun repopulateTheMarkers() {
        userNameStorage = ArrayList()
        imageURL = ArrayList()
        locationLngsStorage = ArrayList()
        locationLatsStorage = ArrayList()
        //whenever there is a new document, add its respective marker
        val docRef = db.collection("requestLocation")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    //test
                    //mMap.clear()
                    for (document in snapshot) {
                        if (document != null) {
                            if (isAdded && activity != null) {

                                Log.d("hhh", "there is a change in request location firebase")
                                //generate a marker
                                //Get user id for storing on marker
                                val requesterId = document.id
                                locationLatsStorage.add(document.data.getValue("Lat").toString())
                                locationLngsStorage.add(document.data.getValue("Lng").toString())
                                userNameStorage.add(document.data.getValue("fullname").toString())
                                if (document.contains("imageUrl")) {
                                    imageURL.add(document.data.getValue("imageUrl").toString())
                                } else {
                                    imageURL.add("null")
                                }
                                val latitude = document.data.getValue("Lat").toString().toDouble()
                                val longitute = document.data.getValue("Lng").toString().toDouble()
                                if (requesterId == userId) {
                                    requestedMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(latitude, longitute))
                                            .title(requesterId)
                                            //.snippet(address)
                                            .icon(
                                                bitmapDescriptorFromVector(
                                                    this.requireContext(),
                                                    R.drawable.ic_pin
                                                )
                                            )
                                    )
                                } else {
                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(latitude, longitute))
                                            .title(requesterId)
                                            //.snippet(address)
                                            .icon(
                                                bitmapDescriptorFromVector(
                                                    this.requireContext(),
                                                    R.drawable.ic_pin
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

    }

    //connection for setting up bind service
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false //end connection between service
            Log.d("hhh","fail to connect")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //called when client binds successfully to the service
            //when bind to BindService class cast the IBinder and get BindService instance
            val binder = service as RequesterObserverService.LocalBinder //pass the argument service to BindService class as a variable called binder
            mService = binder.getService() //call the getService on the binder object
            mBound = true //indicate connection established
            Log.d("hhh","Service is connected")
        }
    }

    private val helperConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            hBound = false //end connection between service
            Log.d("hhh","fail to connect")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //called when client binds successfully to the service
            //when bind to BindService class cast the IBinder and get BindService instance
            val binder = service as HelperObserverService.LocalBinder //pass the argument service to BindService class as a variable called binder
            hService = binder.getService() //call the getService on the binder object
            hBound = true //indicate connection established
            Log.d("fff","Service is connected")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userRequestObs.remove()
        requestLocationObs.remove()
        if(mBound) {
            myContext!!.unbindService(connection)
            mBound = false
            Log.d("rrr","successfully unbinded")
        }
        if(hBound) {
            myContext!!.unbindService(helperConnection)
            hBound = false
            Log.d("rrr","successfully unbinded")
        }
    }

    private fun showNotification(fullname:String, taskDesc:String) {
        // go to notification fragment onclick notification
        val getToActivity = Intent(myContext, NotificationActivity::class.java)
        // Get the PendingIntent containing the entire back stack
        val getToPendingIntent = PendingIntent.getActivity(myContext,
            0, getToActivity, PendingIntent.FLAG_UPDATE_CURRENT)

        // val requestingUsers = remoteMessage.data["requestingUsers"]
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(myContext!!.applicationContext,
            NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(true)
            //.addAction(R.drawable.ic_accept, "ACCEPT", getToPendingIntent)
            //.addAction(R.drawable.ic_accept, "DECLINE", getToPendingIntent)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("$fullname needs help!")
            .setContentText("Please help him/her with $taskDesc")
            .setContentIntent(getToPendingIntent)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)    // important for heads-up notification
            .setAutoCancel(true)
            .setSound(soundUri)

        val notification = notificationBuilder.build()
        val context: Context = myContext!!.applicationContext
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(0, notification)

    }

    //notification code
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(id: String, name: String,
                                  description: String) {
        //makes notification pop up
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

    //for higher api
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(fullname:String, taskDesc:String) {
        Log.d("mnb","notification sent")
        val notificationID = 100
        //send to main activity via the notification
        val resultIntent = Intent(myContext, NotificationActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val getToPendingIntent = PendingIntent.getActivity(myContext,
            notificationID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val notification = Notification.Builder(myContext,
            NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("$fullname needs help!")
            .setContentText("Please help him/her with $taskDesc")
            .setSmallIcon(android.R.drawable.ic_dialog_info) //required
            .setChannelId(NOTIFICATION_CHANNEL_ID)//required
            .setContentIntent(getToPendingIntent)
            .setAutoCancel(true)
            .setTicker("Notification")
            .build()

        notificationManager?.notify(notificationID, notification)
    }
}