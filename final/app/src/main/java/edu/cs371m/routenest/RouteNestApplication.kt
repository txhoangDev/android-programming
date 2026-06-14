package edu.cs371m.routenest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import edu.cs371m.routenest.data.api.PlacesApi

@HiltAndroidApp
class RouteNestApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PlacesApi.initialize(this, getString(R.string.google_maps_key))
    }
}