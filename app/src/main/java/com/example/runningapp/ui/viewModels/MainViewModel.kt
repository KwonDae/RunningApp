package com.example.runningapp.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.repositories.MainRepository
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

    val runSortedByDate = mainRepository.getAllRunSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}