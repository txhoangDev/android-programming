package musicplayer.cs371m.musicplayer

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // A repository can be a local database or the network
    //  or a combination of both
    private val repository = Repository()
    private var songResources = repository.fetchData()

    // Public properties, mostly accessed by PlayerFragment, but useful elsewhere

    // This variable controls what song is playing (exposed as LiveData)
    private val _currentIndex = MutableLiveData(0)
    val observableCurrentIndex: LiveData<Int> = _currentIndex
    // Convenience property for internal access
    var currentIndex: Int
        get() = _currentIndex.value ?: 0
        set(value) { _currentIndex.value = value }

    // It is convenient to have the player never be null, so proactively
    // create it, but you should not create MediaPlayer instances
    // in the view model
    var player: MediaPlayer = MediaPlayer.create(
        application.applicationContext,
        getCurrentSongResourceId()
    )
    // Should I loop the current song? (exposed as LiveData)
    private val _loop = MutableLiveData(false)
    val observableLoop: LiveData<Boolean> = _loop
    // Convenience property for internal access
    var loop: Boolean
        get() = _loop.value ?: false
        set(value) { _loop.value = value }

    // How many songs have played?
    private val _songsPlayed = MutableLiveData(0)
    val songsPlayed: LiveData<Int> = _songsPlayed

    fun incrementSongsPlayed() {
        // Its a bit funny to call postValue after calling .value, but
        // it is always safe to call postValue from the main or background threads.
        _songsPlayed.postValue((_songsPlayed.value ?: 0) + 1)
    }
    // Is the player playing?
    var isPlaying = false

    // Creating a mutable list also creates a copy
    fun getCopyOfSongInfo(): MutableList<SongInfo> {
        return songResources.toMutableList()
    }

    fun shuffleAndReturnCopyOfSongInfo(): MutableList<SongInfo> {
        // XXX Write me
        val currentUniqueId = songResources[currentIndex].uniqueId
        val copyOfSongResources = getCopyOfSongInfo()
        copyOfSongResources.shuffle()
        songResources = copyOfSongResources
        currentIndex = songResources.indexOfFirst { it.uniqueId == currentUniqueId }
        return copyOfSongResources
    }

    fun getCurrentSongName() : String {
        // XXX Write me
        return songResources[currentIndex].name
    }
    // Private function
    private fun nextIndex() : Int {
        // XXX Write me
        if (currentIndex == songResources.size - 1) {
            return 0
        }
        return currentIndex + 1
    }
    fun nextSong() {
        // XXX Write me
        currentIndex = nextIndex()
    }
    fun getNextSongName() : String {
        // XXX Write me
        return songResources[nextIndex()].name
    }

    fun prevSong() {
        // XXX Write me
        if (currentIndex == 0) {
            currentIndex = songResources.size - 1
        } else {
            currentIndex--
        }
    }

    fun getCurrentSongResourceId(): Int {
        // XXX Write me
        return songResources[currentIndex].rawId
    }
}