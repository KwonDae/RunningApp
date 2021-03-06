package com.example.runningapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Daewon
 * @package com.example.runningapp.ui.viewModels
 * @email green201402317@gmail.com
 * @created 2021/12/13
 */

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    mainRepository: MainRepository
): ViewModel() {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunSortedByDate()

}