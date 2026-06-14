package edu.cs371m.reddit.ui


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.reddit.api.RedditApi
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.api.RedditPostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// XXX Much to write
class MainViewModel : ViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title
    var titleValue: String
        get() = _title.value ?: ""
        set(value) { _title.value = value }
    private val _searchTerm = MutableLiveData<String>()
    val searchTerm: LiveData<String> = _searchTerm
    var searchTermValue: String
        get() = _searchTerm.value ?: ""
        set(value) { _searchTerm.value = value }
    private val _subreddit = MutableLiveData<String>().apply {
        value = "aww"
    }
    val subreddit: LiveData<String> = _subreddit
    var subredditValue: String
        get() = _subreddit.value ?: "aww"
        set(value) {
            Log.d(javaClass.simpleName, "set subreddit $value")
            _subreddit.value = value
        }
    private val _favoritesVisible = MutableLiveData<Boolean>()
    val favoritesVisible: LiveData<Boolean> = _favoritesVisible
    // XXX Write me, api, repository, favorites
    val api = RedditApi.create()
    val repository = RedditPostRepository(api)

    private val _favorites = MutableLiveData<List<RedditPost>>()
    private val favorites: LiveData<List<RedditPost>> = _favorites
    var favoritesValue: List<RedditPost>
        get() = _favorites.value ?: emptyList()
        set(value) { _favorites.value = value }

    /////////////////////////

    // netSubreddits fetches the list of subreddits
    // We only do this once, so technically it does not need to be
    // MutableLiveData, or even really LiveData.  But maybe in the future
    // we will refetch it.
    private val netSubreddits = MutableLiveData<List<RedditPost>>().apply{
        // XXX Write me, viewModelScope.launch getSubreddits()
        viewModelScope.launch(Dispatchers.IO) {
            postValue(repository.getSubreddits())
        }
    }

    // netPosts fetches the posts for the current subreddit, when that
    // changes
    private val netPosts = MediatorLiveData<List<RedditPost>>().apply {
        addSource(_subreddit) { subreddit: String ->
            Log.d("repoPosts", subreddit)
            // XXX Write me, viewModelScope.launch getPosts
            viewModelScope.launch(Dispatchers.IO) {
                postValue(repository.getPosts(subredditValue))
            }
        }
    }
    // XXX Write me MediatorLiveData searchSubreddit, searchFavorites
    // searchPosts
    private fun searchPost(post: RedditPost, term: String): Boolean {
        return post.title.contains(term, ignoreCase = true) || post.selfText?.contains(term, ignoreCase = true) == true
    }

    private fun searchSubreddit(post: RedditPost, term: String): Boolean {
        return post.displayName?.contains(term, ignoreCase = true) == true || post.publicDescription?.contains(term, ignoreCase = true) == true
    }

    private val searchPosts = MediatorLiveData<List<RedditPost>>().apply {
        addSource(netPosts) {
            value = netPosts.value ?: emptyList()
        }
        addSource(searchTerm) {
            if (searchTermValue.isEmpty()) {
                value = netPosts.value ?: emptyList()
            } else {
                value =
                    netPosts.value?.filter { searchPost(it, searchTermValue) } ?:
                    emptyList()
            }
        }
    }

    private val searchFavorites = MediatorLiveData<List<RedditPost>>().apply {
        addSource(favorites) {
            value = favoritesValue
        }
        addSource(searchTerm) {
            value = favoritesValue.filter { searchPost(it, searchTermValue) }
        }
    }

    private val searchSubreddit = MediatorLiveData<List<RedditPost>>().apply {
        addSource(netSubreddits) {
            value = netSubreddits.value ?: emptyList()
        }
        addSource(searchTerm) {
            if (searchTermValue.isEmpty())
                value = netSubreddits.value ?: emptyList()
            else
                value =
                    netSubreddits.value?.filter { searchSubreddit(it, searchTermValue) } ?:
                    emptyList()
        }
    }

    // Looks pointless, but if LiveData is set up properly, it will fetch posts
    // from the network
    fun repoFetch() {
        subredditValue = subredditValue
    }

    /////////////////////////
    // LiveData to control Action bar
    // The view model should not hold a reference to a view, like
    // the action bar, but it can hold a reference to data that controls
    // the action bar.
    fun hideActionBarFavorites() {
        _favoritesVisible.value = false
    }
    fun showActionBarFavorites() {
        _favoritesVisible.value = true
    }

    // XXX Write me, set, observe, deal with favorites
    fun observePosts(): LiveData<List<RedditPost>> = searchPosts
    var postsValue: List<RedditPost>
        get() = searchPosts.value ?: emptyList()
        set(value) {}
    fun observeFavorites(): LiveData<List<RedditPost>> = searchFavorites
    fun observeSubreddits(): LiveData<List<RedditPost>> = searchSubreddit

    fun addFavorites(post: RedditPost) {
        if (favoritesValue.contains(post)) {
            favoritesValue = favoritesValue.filter { it != post }
        } else {
            favoritesValue = favoritesValue.plus(post)
        }
    }
}