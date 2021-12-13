package com.example.runningapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * @HiltAndroidApp은 애플리케이션 수준 종속 항목 컨테이너 역할을 하는 애플리케이션의 기본 클래스를 비롯하여 Hilt의 코드 생성을 트리거합니다.
 * 생성된 이 Hilt 구성요소는 Application 객체의 수명 주기에 연결되며 이와 관련한 종속 항목을 제공합니다. 또한 이는 앱의 상위 구성요소이므로
 * 다른 구성요소는 이 상위 구성요소에서 제공하는 종속 항목에 액세스할 수 있습니다.
 */
@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}