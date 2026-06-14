package edu.cs371m.reddit.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RedditPost (
    @SerializedName("name")
    val key: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("subreddit")
    val subreddit: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("author")
    val author: String,
    @SerializedName("num_comments")
    val commentCount: Int,
    @SerializedName("thumbnail")
    val thumbnailURL: String,
    @SerializedName("url")
    val imageURL: String,
    @SerializedName("selftext")
    val selfText : String?,
    @SerializedName("is_video")
    val isVideo : Boolean,
    // Useful for subreddits
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("icon_img")
    val iconURL: String?,
    @SerializedName("public_description")
    val publicDescription: String?
): Serializable {
    // NB: This changes the behavior of lists of RedditPosts.  I want posts fetched
    // at two different times to compare as equal.  By default, they will be different
    // objects with different hash codes.
    override fun equals(other: Any?) : Boolean =
        if (other is RedditPost) {
            key == other.key
        } else {
            false
        }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
