package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.*
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
import com.example.runningapp.util.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Daewon
 * @package com.example.runningapp.services
 * @email green201402317@gmail.com
 * @created 2021/12/15
 *
 */

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

// Noti ????????? ?????????
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()

        // ?????? ?????? ?????? ??????
        val isTracking = MutableLiveData<Boolean>()

        // LatLng = ??????,??????
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        // Empty List
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // ???????????? on?????? Observing
        isTracking.observe(this, Observer {
            Timber.d("TrackingService - onCreate called / ")
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService() {
        // don't do anything service
        serviceKilled = true;
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // ???????????? ???????????????
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TrackingFragment?????? sendCommandToService??? ????????? action??? intent??? ????????????
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false

    // ??????????????? ????????? ????????? ???????????? ?????? ??????
    private var lapTime = 0L

    // ??????????????? ????????? ??? ?????? ??????
    private var timeRun = 0L

    // ?????? ????????? ????????? ??? ??????
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        /*
        ?????? startForegroundService??? ????????????
        only called first start this foregroundService but not resume
        that reason addEmptyPolyline move to startTimer()
        that means either foregroundService before or just first of service
         */
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        // coroutine
        // we don't want call of service all the time
        // ?????? ??????????????? ????????? ?????? ???????????? ????????? ????????? ?????? ??????.
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                Timber.tag("isTracking").d("${isTracking.value}")
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                // post the new laptime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent: PendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getService(this, 1, pauseIntent, FLAG_MUTABLE)
            } else {
                PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
            }
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getService(this, 2, resumeIntent, FLAG_MUTABLE)
            } else {
                PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
            }
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        //TODO ???
        // getDeclaredField
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL // ?????? ???????????? ??????
                    fastestInterval = FASTEST_LOCATION_INTERVAL // ?????? ?????? ?????? ???????????? ??????
                    priority = PRIORITY_HIGH_ACCURACY // ?????????????????? ???????????? ????????? ???????????? ??????????????? ??????
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

    // location(????????????)???????????? addPathPoint??? ??????
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    // ????????????(lat,lon) ??????
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

    // ??? polyline ??????
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    // Notification ??????, ????????? ??????
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager = notificationManager)
        }

        /**
         * ????????????????????? ???????
         */
//        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
//            .setContentTitle("Running App")
//            .setContentText("00:00:00")
//            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            // could happened that service killed notification
            // observer still get call one more time
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L, false))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    // ????????? ?????? ??????, ?????? ??????
    /**
     * ????????????????????? ??????
     */
//    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this, MainActivity::class.java).also {
//            it.action = ACTION_SHOW_TRACKING_FRAGMENT
//        },
//        FLAG_UPDATE_CURRENT
//    )

    // ?????? ?????????
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // ????????? ??????
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}