# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FirebaseAuth is a formative challenge teaching Firebase Authentication with FirebaseUI, including login, logout, and display name management.

**Package**: `edu.utap.firebaseauth`

**Specification**: `hw/fc/fc50-firebase-auth.tex`

## Build Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew installDebug
```

## Architecture

Single-activity app demonstrating Firebase Auth:

- **MainActivity.kt** - UI for login/logout buttons, display name input, and showing user info
- **AuthUser.kt** - Wrapper class for Firebase Auth operations with LiveData for observing user state

## Key Concepts

- `FirebaseAuth` for authentication
- FirebaseUI `AuthUI` for pre-built sign-in UI
- `activityResultRegistry` for handling auth result
- `LifecycleObserver` pattern for auth state management
- LiveData for observing user state changes
- `updateProfile` for setting display name

## Lifecycle

AuthUser is initialized in `onStart()` (not `onCreate()`) because it needs the activity to be fully created before registering for activity results.

## Solution Code Markers

Code between `//SSS` and `//EEE` comments contains solution code that gets stripped for student distribution.
