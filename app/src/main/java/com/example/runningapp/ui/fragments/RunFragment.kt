package com.example.runningapp.ui.fragments

import android.Manifest
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.other.Constants.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION
import com.example.runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningapp.other.Constants.TAG
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewModels.MainViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*

/**
 * @author Daewon
 * @package com.example.runningapp.ui.fragments
 * @email green201402317@gmail.com
 * @created 2021/12/13
 */

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

    }

    private fun requestPermissions() {
        //TODO requireContext()?
        if (EasyPermissions.hasPermissions(
                requireContext(),
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION,
                ACCESS_BACKGROUND_LOCATION
            )
        ) {
            return
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    requireActivity(),
                    "You need to accept location permissions to use this app.",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
                )
            } else {
                EasyPermissions.requestPermissions(
                    requireActivity(),
                    "You need to accept location permissions to use this app.",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
                )
                backgroundPermission()
            }
        }

    }

    // 안드로이드 API 30 버전부터는 backgroundPermission 을 직접 설정해야함
    private fun backgroundPermission() {
        EasyPermissions.requestPermissions(
            requireActivity(),
            "백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.",
            REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION,
            ACCESS_BACKGROUND_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted: $requestCode :${perms.size}")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied: $requestCode :${perms.size}")

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissionList ->
//        permissionList.forEach { permission ->
//            if (permission.value) {
//                //isGranted
//            } else {
//                Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
//                    .show();
//            }
//        }
//    }


}