package com.example.runningapp.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.example.runningapp.util.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.util.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

/**
 * @author Daewon
 * @package com.example.runningapp.services
 * @email green201402317@gmail.com
 * @created 2021/12/15
 */

class TrackingService : LifecycleService() {

    // 서비스가 호출되었을
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
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
}