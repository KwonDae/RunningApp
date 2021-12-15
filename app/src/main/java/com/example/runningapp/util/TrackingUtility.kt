package com.example.runningapp.util

import android.Manifest
import android.content.Context
import android.os.Build
import com.vmadalin.easypermissions.EasyPermissions

/**
 * @author Daewon
 * @package com.example.runningapp.other
 * @email green201402317@gmail.com
 * @created 2021/12/15
 */

object TrackingUtility {

    fun hasLocationPermissions(context: Context) : Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    }

}