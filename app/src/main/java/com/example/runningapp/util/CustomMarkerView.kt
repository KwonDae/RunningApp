package com.example.runningapp.util

import android.content.Context
import com.example.runningapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Daewon
 * @package com.example.runningapp.util
 * @email green201402317@gmail.com
 * @created 2021/12/22
 */

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if( e != null) {
            val curRunId = e.x.toInt()
            val run = runs[curRunId]

            // setTvDate
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.KOREA)
            tvDate.text= dateFormat.format(calendar.time)

            // setAvgSpeed
            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            tvDistance.text = distanceInKm

            tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            tvCaloriesBurned.text = caloriesBurned
        }
    }
}