package edu.cs371m.reddit.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RedditPostRepositoryTest {
    @Test
    fun getAwwSubredditPosts() = runBlocking {
        // initialization
        val api = RedditApi.create()
        val repository = RedditPostRepository(api)

        // function call
        val posts = repository.getPosts("aww")

        // assertions
        assertEquals(100, posts.size)
    }

    @Test
    fun getSubreddits() = runBlocking {
        // initialization
        val api = RedditApi.create()
        val repository = RedditPostRepository(api)

        val subreddits = repository.getSubreddits()
        print(subreddits)

        assertEquals(100, subreddits.size)
    }

    @Test
    fun mockToEnsureFunctionality() = runBlocking {
        // initialization
        val subreddit = "aww"
        val mockApi = mock(RedditApi::class.java)
        val mockRedditPost = RedditPost(
            key = "testKey",
            title = "Test Title",
            subreddit = subreddit,
            score = 10,
            author = "testUser",
            commentCount = 1,
            thumbnailURL = "https://test",
            imageURL = "https://test",
            selfText = "test",
            isVideo = false,
            displayName = "test",
            iconURL = "https://test",
            publicDescription = "test"
        )
        val mockChildResponse = RedditApi.RedditChildrenResponse(mockRedditPost)
        val mockResponse = RedditApi.ListingResponse(
            RedditApi.ListingData(
                listOf(mockChildResponse),
                null,
                null
            )
        )
        whenever(mockApi.getPosts(subreddit, 100)).thenReturn(mockResponse)

        val repository = RedditPostRepository(mockApi)
        val posts = repository.getPosts(subreddit)

        assertEquals(1, posts.size)
        assertEquals(subreddit, posts[0].subreddit)
        assertEquals("Test Title", posts[0].title)
    }
}

