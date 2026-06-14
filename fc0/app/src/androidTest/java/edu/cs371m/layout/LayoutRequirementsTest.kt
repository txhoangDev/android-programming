package edu.cs371m.layout

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
 * Instrumented tests for the Layout FC assignment.
 * Tests verify requirements specified in fc00-layout.tex.
 */
@RunWith(AndroidJUnit4::class)
class LayoutRequirementsTest {

    // ========================================================================
    // CONSTRAINT LAYOUT TESTS (activity_main.xml)
    // ========================================================================

    /**
     * Doc: "lay out four buttons (named B1, B2, B3, and B4)" (line 28)
     * Verifies all four buttons exist with correct text labels.
     */
    @Test
    fun constraintLayout_allButtonsExistWithCorrectText() {
    }
}
