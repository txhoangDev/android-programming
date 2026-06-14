# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MusicPlayer is a homework assignment implementing a music player app with playlist management, demonstrating MVVM architecture, Navigation Component, and RecyclerView with DiffUtil.

**Package**: `musicplayer.cs371m.musicplayer`

**Specification**: `hw/hw3-mplayer/hw3-mplayer.tex`

## Build Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew installDebug
```

## Architecture

MVVM with Navigation Component:

- **MainActivity.kt** - Navigation host with options menu for settings
- **PlayerFragment.kt** - Main music player UI with playback controls
- **SettingsFragment.kt** - App preferences/settings
- **MainViewModel.kt** - Shared ViewModel for player state
- **Repository.kt** - Data layer for music/playlist management
- **RVDiffAdapter.kt** - RecyclerView adapter using DiffUtil for efficient updates

## Key Concepts

- Navigation Component with `setupActionBarWithNavController`
- `MenuProvider` for options menu handling
- `navigateUp` for proper back stack management
- DiffUtil for RecyclerView performance optimization

## LiveData Implementation

The ViewModel exposes three LiveData properties:

| Property | Type | Purpose |
|----------|------|---------|
| `songsPlayed` | `LiveData<Int>` | Count of songs played, observed by SettingsFragment |
| `observableCurrentIndex` | `LiveData<Int>` | Current song index, observed by PlayerFragment |
| `observableLoop` | `LiveData<Boolean>` | Loop mode, observed by both fragments |

Convenience properties `currentIndex` and `loop` provide direct read/write access backed by the LiveData.

### Completed LiveData Work
- Added `observableCurrentIndex` and `observableLoop` to MainViewModel
- PlayerFragment observes both to update song display and loop indicator
- SettingsFragment observes `observableLoop` for switch state
- Updated `hw3-mplayer.tex` specification with LiveData section
- LiveData behavior is tested through functional tests in `MusicPlayerRequirementsTest.kt` (e.g., `testLoopIndicatorTogglesOnAndOff`, `testLoopSettingPersistsToSettingsSwitch`, `testSongSelectionSurvivesNavigation`)

### Potential Future LiveData Enhancements
- **`isPlaying`**: Convert to LiveData so play/pause button updates reactively
- **`songList`**: Expose song list as LiveData for reactive RecyclerView updates after shuffle
- **Transformation**: Use `Transformations.map()` to derive `currentSongName` and `nextSongName` from `currentIndex`
- **MediatorLiveData**: Combine multiple LiveData sources for complex UI state

## Solution Code Markers

Code between `//SSS` and `//EEE` comments contains solution code that gets stripped for student distribution.
