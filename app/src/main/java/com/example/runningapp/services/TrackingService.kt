package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.util.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningapp.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.util.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.util.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp.util.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.util.Constants.NOTIFICATION_ID
import com.example.runningapp.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

/**
 * @author Daewon
 * @package com.example.runningapp.services
 * @email green201402317@gmail.com
 * @created 2021/12/15
 */

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>
// Noti 제공할 서비스
class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        // Empty List
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    // 서비스가 호출되었을
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TrackingFragment에서 sendCommandToService에 넣어준 action을 intent로 받아오기
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                // pathPoints = Polylines
                // Polylines = MutableList<PolyLine>
                // Polylines.las() = lastIndex polyline(=MutableList<LatLng>)
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    // Notification 등록, 서비스 시작
    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager = notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    // 알림창 버튼 생성, 액션 추가
    //TODO PendingIntent?
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    // 채널 만들기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // 알림음 없음
        )
        notificationManager.createNotificationChannel(channel)
    }
}