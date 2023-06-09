package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.CallLog.Locations
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentNotificationsBinding
import com.example.gps.interfaces.MapInterface
import com.example.gps.utils.FontUtils
import com.example.gps.utils.MapUtils
import com.example.gps.utils.StringUtils
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class NotificationsFragment : Fragment(R.layout.fragment_notifications), MapInterface {
    private var cameraPosition: CameraPosition? = null
    private lateinit var sharedPreferencesSetting: SharedPreferences
    private lateinit var sharedPreferencesState: SharedPreferences
    private var mapFragment: SupportMapFragment? = null
    private var checkMoveCameraMap: Boolean = false

    @SuppressLint("SuspiciousIndentation", "MissingPermission")
    private val callback = OnMapReadyCallback { p0 ->
        map = p0
        map!!.apply {
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(18.683500, 105.485750)))
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isRotateGesturesEnabled = true
            try {
                map!!.isMyLocationEnabled = true
            } catch (e: Exception) {
            }
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            setOnCameraMoveStartedListener {
                resetMinMaxZoomPreference()
            }
        }
    }
    private var binding: FragmentNotificationsBinding? = null
    private var map: GoogleMap? = null
    private var polylineOption = PolylineOptions()
    private var check = false
    private var i = 0

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentNotificationsBinding.bind(view)

        sharedPreferencesSetting =
            requireContext().getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
        sharedPreferencesState =
            requireActivity().getSharedPreferences("state", Context.MODE_PRIVATE)
        check = sharedPreferencesSetting.getBoolean(SettingConstants.TRACK_ON_MAP, true)
        val positionsColor = sharedPreferencesSetting.getInt(SettingConstants.COLOR_DISPLAY, 2)
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable("cameraPosition")
            if (sharedPreferencesState.getString(
                    MyLocationConstants.STATE,
                    null
                ) == MyLocationConstants.STOP
            ) cameraPosition == null
        }
        onColorChange(positionsColor)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapAsync()
        checkPermission()

        with(binding) {
            this?.imgChange?.setOnClickListener {
                map?.let { it1 -> MapUtils.setStStyleMap(it1) }
            }
            this!!.txtSpeed.text = "0" + SharedData.toUnit
            FontUtils.setFont(requireContext(), this.txtSpeed)
            addPolylineIfValidState()
            SharedData.locationLiveData.observe(viewLifecycleOwner) { location ->
                updateLocationUI(location)
            }
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) { Speed ->
                this.txtSpeed.text = StringUtils.convert(Speed.keys.first())
                FontUtils.setFont(requireContext(), this.txtSpeed)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun addPolylineIfValidState() {
        val state = sharedPreferencesState.getString(MyLocationConstants.STATE, null)
        if ((state == MyLocationConstants.START || state == MyLocationConstants.PAUSE || state == MyLocationConstants.RESUME) && check) {
            val polylineOptions =
                PolylineOptions().addAll(convertToListLatLng()).color(Color.GREEN).width(15f)
            map?.addPolyline(polylineOptions)
            polylineOption = polylineOptions
        }
    }

    private fun updateLocationUI(location: Location?) {
        with(binding) {
            if (location == null) {
                this!!.latitude.text = "0"
                longitude.text = "0"
            } else {
                if (i == 0 && map != null && cameraPosition == null) {
                    val sydney = LatLng(location.latitude, location.longitude)
                    map?.addMarker(MarkerOptions().position(sydney).title("User"))
                    map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                    i = 1
                }
                polylineOption.add(LatLng(location.latitude, location.longitude)).color(Color.GREEN)
                    .width(15f)
                if (check) map?.addPolyline(polylineOption);
                this!!.latitude.text = location.latitude.toString()
                longitude.text = location.longitude.toString()
            }
        }
    }

    private fun updateMoveCamera(location: Location): CameraPosition {
        return CameraPosition.Builder().target(LatLng(location.latitude, location.longitude))
            .zoom(map?.cameraPosition?.zoom!!).bearing(location.bearing).build()
    }

    private fun checkPermission() {
        if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || requireActivity().checkSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (this@NotificationsFragment.cameraPosition != null) {
                this@NotificationsFragment.cameraPosition?.let {
                    CameraUpdateFactory.newCameraPosition(
                        it
                    )
                }
            } else {
                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            // Tạo LatLng object từ vị trí hiện tại
                            val latLng = LatLng(location.latitude, location.longitude)

                            // Di chuyển camera tới vị trí hiện tại
                            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        }
                    }

            }
        }

    }

    fun mapAsync() {
        mapFragment?.getMapAsync(callback)

    }

    private fun convertToListLatLng(): List<LatLng> {
        val listMovement = MyDataBase.getInstance(requireContext()).locationDao().getLocationData(
            MyDataBase.getInstance(requireContext()).movementDao().getLastMovementDataId()
        )

        return listMovement.map { LatLng(it.latitude, it.longitude) }.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("cameraPosition", map?.cameraPosition)
    }


    override fun onVisibilityPolyLine(boolean: Boolean) {
        check = boolean
        if (boolean) map?.addPolyline(polylineOption) else map?.clear()
    }

    fun clear() {
        map?.clear()
    }

    override fun onColorChange(i: Int) {
        with(binding) {
            FontUtils.setTextColor(
                i,
                this!!.longitude,
                latitude,
                txtSpeed,
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onUnitChange() {
        binding?.txtSpeed?.text = "${
            SharedData.convertSpeed(
                SharedData.currentSpeedLiveData.value!!.keys.first()
            )
        }+${SharedData.toUnit}"
        FontUtils.setFont(requireContext(), binding?.txtSpeed!!)
    }
}