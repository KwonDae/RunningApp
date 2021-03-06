package com.example.runningapp.ui.fragments

import android.Manifest.permission.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningapp.R
import com.example.runningapp.adapters.RunAdapter
import com.example.runningapp.util.Constants.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION
import com.example.runningapp.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningapp.util.Constants.TAG
import com.example.runningapp.ui.viewModels.MainViewModel
import com.example.runningapp.util.SortType
import com.example.runningapp.util.TrackingUtility
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Daewon
 * @package com.example.runningapp.ui.fragments
 * @email green201402317@gmail.com
 * @created 2021/12/13
 */

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        setUpRecyclerView()

        when(viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }


        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        //????????? ????????? ???????????? Tracking ???????????? ??????
        fab.setOnClickListener {
            if (TrackingUtility.hasLocationPermissions(requireContext())) {
                // ?????? ?????? ??? ????????? ?????? ???
                Timber.tag(TAG).d("RunFragment - EasyPermissions called / granted")
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            } else {
                // ?????? ?????? ??? ????????? ?????? ??????
                Timber.tag(TAG).d("RunFragment - EasyPermissions called / deny")
                requestPermissions()
            }
        }

    }

    private fun setUpRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    // ?????? ??????
    private fun requestPermissions() {
        //TODO requireContext()?
        // ?????? ????????? ????????? ??????
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            Timber.tag(TAG).d("RunFragment - requestPermissions called / true")
            return
        } else {
            Timber.tag(TAG).d("RunFragment - requestPermissions called / false")
            // SDK 29 ?????? BACKGROUND_LOCATION ?????? ??????
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

    // ??????????????? API 29 ??????????????? backgroundPermission ??? ?????? ???????????????
    // ???????????????11 ????????? ?????? ????????? ????????? ???????????? ????????????. ????????? ?????? ???????????? ??????????????????.
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun backgroundPermission() {
        EasyPermissions.requestPermissions(
            requireActivity(),
            "??????????????? ?????? ????????? ?????? ?????? ???????????? ??????????????????.",
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

    // ??? ??? ?????? ???????????? ?????? ????????? ????????? ?????? ?????????.
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