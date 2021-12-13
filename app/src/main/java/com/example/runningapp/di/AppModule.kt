package com.example.runningapp.di

import android.content.Context
import androidx.room.Room
import com.example.runningapp.db.RunningDatabase
import com.example.runningapp.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * SingletonComponent =
 * Repository Pattern에서 Repository는 뷰, 뷰 모델과 별개로 존재해야 하며,
 * 어디서든 접근이 가능해야 하므로 Singleton 컴포넌트로 작성을 합니다.
 * 그리하여 Repository 모듈은 InstallIn(SingletonComponent::class)를 통해 싱글턴 모듈임을 나타내도록 합니다.
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()
}