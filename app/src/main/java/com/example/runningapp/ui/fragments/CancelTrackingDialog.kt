package com.example.runningapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @author Daewon
 * @package com.example.runningapp.ui.fragments
 * @email green201402317@gmail.com
 * @created 2021/12/23
 */

class CancelTrackingDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("예") { _, _ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton("아니오") { dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .create()

    }
}