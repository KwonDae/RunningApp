package com.example.runningapp.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.repositories.MainRepository
import com.example.runningapp.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Daewon
 * @package com.example.runningapp.ui.viewModels
 * @email green201402317@gmail.com
 * @created 2021/12/13
 *
 * @HilteViewModel에서 @Inject 어노테이션이 붙은 생성자는 생성자 파라미터가
 * Hilt에 의해 주입받을 거라고 정의내리는 종속성을 갖게 해준다.
 */

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunSortedByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        // https://ddangeun.tistory.com/126

        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}