package edu.utap.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
    fun updateData(string: String) {
        // Please add one line to this function (where indicated), leaving the rest
        // of the code unchanged.
        // All work done in viewModelScope will be
        // canceled if viewModel is cleared.
        // Dispatcher.Main - main thread
        // Dispatcher.Default - background thread possibly on other processor
        // Dispatcher.IO - for network or file system
        // NB: We are working on a background thread.  That means we can't
        // assign a livedata's value property directly.
        // Read the LiveData documentation about what to do on a background thread.
        // https://developer.android.com/reference/androidx/lifecycle/LiveData
        viewModelScope.launch(Dispatchers.Default) {
            // Please leave this delay
            delay(2000)
            // XXX Write me (one liner)
            _data.postValue(string)
        }
    }
}
