package edu.utap.intent

import android.content.ComponentName
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class MainActivityTest {

    // Here is a simple test to check if a Snackbar is displayed
    @Test
    fun testSnackbarIsDisplayed() {
        // Launch the MainActivity
        ActivityScenario.launch(MainActivity::class.java)

        // Simulate a condition where the Snackbar should appear (e.g., clicking play with empty name)
        onView(withId(R.id.playButton)).perform(click())

        // Verify that any Snackbar is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }
}
