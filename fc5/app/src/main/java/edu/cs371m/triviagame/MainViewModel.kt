package edu.cs371m.triviagame

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.triviagame.api.Repository
import edu.cs371m.triviagame.api.TriviaApi
import edu.cs371m.triviagame.api.TriviaQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

class MainViewModel : ViewModel() {
    private var difficulty = "medium"
    // XXX You need some important member variables
    private val repository = Repository(TriviaApi.create())
    private val _questions = MutableLiveData<List<TriviaQuestion>>()
    val questions: LiveData<List<TriviaQuestion>> = _questions
    private val _fetchDone = MutableLiveData(false)
    val fetchDone: LiveData<Boolean> = _fetchDone
    init {
        // XXX one-liner to kick off the app
        netRefresh()
    }

    fun setDifficulty(level: String) {
        difficulty = when(level.lowercase(Locale.getDefault())) {
            // Sanitize input
            "easy" -> "easy"
            "medium" -> "medium"
            "hard" -> "hard"
            else -> "medium"
        }
        Log.d(javaClass.simpleName, "level $level END difficulty $difficulty")
    }

    fun netRefresh() {
        // XXX Write me.  This is where the network request is initiated.
        // When it returns, update all of your live data
        viewModelScope.launch(Dispatchers.IO) {
            var attempts = 0

            while (attempts < 3) {
                try {
                    val result = repository.retrieveThree(difficulty)
                    Log.d(javaClass.simpleName, "result $result")
                    _questions.postValue(result)
                    _fetchDone.postValue(true)
                    break
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        Log.d(javaClass.simpleName, "Too many attempts, retrying...")
                        attempts++
                        delay(1000L)
                    } else {
                        Log.d(javaClass.simpleName, "Exception $e")
                        break
                    }
                }
            }
            return@launch
        }
    }
}
