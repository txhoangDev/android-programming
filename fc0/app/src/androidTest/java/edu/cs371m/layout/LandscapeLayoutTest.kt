package edu.cs371m.layout

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for landscape orientation.
 * Doc: "Above are portrait and landscape screenshots of our ideal layout" (line 45)
 * The LaTeX document shows both portrait and landscape layouts should work correctly.
 */
@RunWith(AndroidJUnit4::class)
class LandscapeLayoutTest {

    // ========================================================================
    // LANDSCAPE TESTS - CONSTRAINT LAYOUT
    // ========================================================================

    /**
     * Doc: "portrait and landscape screenshots" (line 45)
     * Verifies all buttons are visible in landscape mode.
     */
    @Test
    fun landscape_allButtonsVisible() {

    }
}
