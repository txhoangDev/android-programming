# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android educational project demonstrating various layout patterns (ConstraintLayout, LinearLayout, RelativeLayout, GridLayout). Package: `edu.cs371m.layout`

## Build Commands

Set JAVA_HOME before running Gradle commands:

```bash
export JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr"
```

Common commands:
```bash
# Build debug APK
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew assembleDebug

# Install on connected device/emulator
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew installDebug

# Run unit tests
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew test

# Run instrumented tests (requires device/emulator)
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew connectedAndroidTest

# Clean build
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew clean
```

## Build Configuration

- **Gradle:** 8.13
- **Android Gradle Plugin:** 8.13.2
- **Kotlin:** 2.2.0
- **Compile/Target SDK:** 36
- **Min SDK:** 33
- **Java:** 21
- **View Binding:** Enabled

## Architecture

Single-activity app focused on demonstrating Android layout techniques:

- **MainActivity.kt** - Uses View Binding (`ActivityMainBinding`) to inflate layouts
- **Layout files** demonstrate different approaches:
  - `activity_main.xml` - ConstraintLayout with chains and percentage constraints
  - `activity_main_linear*.xml` - LinearLayout variants with weights and nesting
  - `ai_challenge.xml` - LinearLayout with 3 GridLayout number pads (1 top, 2 bottom side-by-side)
  - `number_pad.xml` - Reusable GridLayout component (included via `<include>` tag)

## Layout File Markers

Some layout files contain `SSS`/`EEE` and `DDD` comment markers indicating exercise boundaries for students. Files `ai_challenge.xml` and `number_pad.xml` contain "Delete this entire file" comments suggesting optional exercise materials.

## Testing

Instrumented tests verify layout requirements from the assignment specification (`../../../hw/fc/fc00-layout.tex`):

- **LayoutRequirementsTest.kt** - Tests for `activity_main.xml` (ConstraintLayout): button existence, 16dp margins, B1:B2 width ratio (2:1), TextView centering, B3/B4 equidistant spacing
- **LinearLayoutRequirementsTest.kt** - Tests for `activity_main_linear.xml`: LinearLayout-only structure, layout_weight usage, 0dp width for weight-based sizing
- **LandscapeLayoutTest.kt** - Verifies layouts work correctly in landscape orientation: button visibility, width ratios, alignment, centering
- **AiChallengeTest.kt** - Tests for `ai_challenge.xml`: numbers 0-9 present, no * or # buttons, three number pads, GridLayout structure, zero in center column only (not spanning)

Run a specific test class:
```bash
JAVA_HOME="C:/Program Files/Android/Android Studio1/jbr" ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.cs371m.layout.LayoutRequirementsTest
```

Each test includes a comment citing the documentation line that motivated it.
