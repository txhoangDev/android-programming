package musicplayer.cs371m.musicplayer

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    // Here is a test to get you started
    @Test
    fun firstSongDarkStar() {
        onView(withId(R.id.playerCurrentSongText))
            .check(matches(withText("Dark Star")))
    }

    @Test
    fun UI() {
        onView(withId(R.id.playerCurrentSongText)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.nextUpText)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerNextSongText)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.playerRV)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.loopIndicator)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerSkipBackButton)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerPlayPauseButton)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerSkipForwardButton)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerPermuteButton)).check(matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.playerSeekBar)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerTimePassedText)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.playerTimeRemainingText)).check(matches(ViewMatchers.isDisplayed()))


        // Settings icon present on player screen
        onView(withContentDescription(R.string.action_settings)).check(matches(ViewMatchers.isDisplayed()))
    }
}


