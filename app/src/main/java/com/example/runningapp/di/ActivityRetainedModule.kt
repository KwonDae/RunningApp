package com.example.runningapp.di

import android.content.Context
import android.content.SharedPreferences
import com.example.runningapp.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityRetainedModule {

//    @FragmentScoped
//    @Provides
//    fun provideSharedPreferences(@ApplicationContext app: Context): SharedPreferences =
//        app.getSharedPreferen`ces(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @ActivityRetainedScoped
    @Provides
    fun provideName(sharedPref: SharedPreferences): String = sharedPref.getString(Constants.KEY_NAME, "")?: ""

    @ActivityRetainedScoped
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(Constants.KEY_WEIGHT, 80f)

    @ActivityRetainedScoped
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences): Boolean = sharedPref.getBoolean(
        Constants.KEY_FIRST_TIME_TOGGLE, true)
}