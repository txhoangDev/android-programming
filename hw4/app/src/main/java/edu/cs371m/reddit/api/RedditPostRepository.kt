package edu.cs371m.reddit.api

import com.google.gson.Gson
import edu.cs371m.reddit.MainActivity

class RedditPostRepository(private val redditApi: RedditApi) {
    // NB: This is for our testing.
    private val gson = Gson()

    private fun extractPosts(response: RedditApi.ListingResponse): List<RedditPost> {
        // XXX Write me.
        return response.data.children.map { it.data }
    }

    suspend fun getPosts(subreddit: String): List<RedditPost> {
        val response : RedditApi.ListingResponse?
        if (MainActivity.globalDebug) {
            response = gson.fromJson(
                MainActivity.jsonAww100,
                RedditApi.ListingResponse::class.java)
        } else {
            // XXX Write me.
            response = redditApi.getPosts(subreddit, 100)
        }
        return extractPosts(response!!)
    }

    suspend fun getSubreddits(): List<RedditPost> {
        val response : RedditApi.ListingResponse?
        if (MainActivity.globalDebug) {
            response = gson.fromJson(
                MainActivity.subreddit1,
                RedditApi.ListingResponse::class.java)
        } else {
            // XXX Write me.
            response = redditApi.getSubreddits(100)
        }
        return extractPosts(response!!)
    }
}
