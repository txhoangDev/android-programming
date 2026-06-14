package musicplayer.cs371m.musicplayer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MusicPlayerEspressoTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // -----------------------
    // Navigation helpers
    // -----------------------
    private void openSettings() {
        // Action icon content description is usually the title.
        onView(withContentDescription(R.string.action_settings)).perform(click());
        onView(withId(R.id.loopSwitch)).check(matches(isDisplayed()));
    }

    // -----------------------
    // Basic required UI
    // -----------------------
    @Test
    public void UI() {
        onView(withId(R.id.nowPlayingText)).check(matches(isDisplayed()));
        onView(withId(R.id.playerCurrentSongText)).check(matches(isDisplayed()));
        onView(withId(R.id.nextUpText)).check(matches(isDisplayed()));
        onView(withId(R.id.playerNextSongText)).check(matches(isDisplayed()));

        onView(withId(R.id.playerRV)).check(matches(isDisplayed()));

        onView(withId(R.id.loopIndicator)).check(matches(isDisplayed()));
        onView(withId(R.id.playerSkipBackButton)).check(matches(isDisplayed()));
        onView(withId(R.id.playerPlayPauseButton)).check(matches(isDisplayed()));
        onView(withId(R.id.playerSkipForwardButton)).check(matches(isDisplayed()));
        onView(withId(R.id.playerPermuteButton)).check(matches(isDisplayed()));

        onView(withId(R.id.playerSeekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.playerTimePassedText)).check(matches(isDisplayed()));
        onView(withId(R.id.playerTimeRemainingText)).check(matches(isDisplayed()));

        // Settings icon present on player screen
        onView(withContentDescription(R.string.action_settings)).check(matches(isDisplayed()));
    }

    // -----------------------
    // Initial player display
    // -----------------------
    @Test
    public void firstSongDarkStar() {
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));
    }

    @Test
    public void initialSongInfo() {
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));
        onView(withId(R.id.playerNextSongText)).check(matches(withText("What's Mine")));
        onView(withId(R.id.playerPlayPauseButton))
                .check(matches(withImageDrawable(R.drawable.ic_play_arrow_black_24dp)));
    }

    // -----------------------
    // Clicking song rows updates player display
    // -----------------------
    @Test
    public void clickSong() {
        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(withId(R.id.playerCurrentSongText))
                .check(matches(withText("La Fille Aux Cheveux De Lin")));
    }

    @Test
    public void afterFirstSelect() {
        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(withId(R.id.playerCurrentSongText))
                .check(matches(withText("La Fille Aux Cheveux De Lin")));
        onView(withId(R.id.playerNextSongText))
                .check(matches(withText("Rondo Alla Turca")));
        onView(withId(R.id.playerRV))
                .check(matches(RecyclerViewMatchers.atPositionHasBackgroundColor(
                        2, R.id.songTitle, 0xFFFFFF00 /* Color.YELLOW */)));
    }

    @Test
    public void startPlayClickPause() {
        // Select a different song, then play and pause.
        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, click()));
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Big Digits")));

        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        onView(withId(R.id.playerPlayPauseButton))
                .check(matches(withImageDrawable(R.drawable.ic_pause_black_24dp)));

        // Verify audio state via view model.
        activityRule.getScenario().onActivity(activity -> {
            MainViewModel vm = new ViewModelProvider(activity).get(MainViewModel.class);
            assertNotNull(vm.getPlayer());
            assertTrue(vm.getPlayer().isPlaying());
        });

        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        onView(withId(R.id.playerPlayPauseButton))
                .check(matches(withImageDrawable(R.drawable.ic_play_arrow_black_24dp)));

        activityRule.getScenario().onActivity(activity -> {
            MainViewModel vm = new ViewModelProvider(activity).get(MainViewModel.class);
            assertNotNull(vm.getPlayer());
            assertFalse(vm.getPlayer().isPlaying());
        });
    }

    // -----------------------
    // Songs played counter updates
    // -----------------------
    @Test
    public void clickClickNoPlay() {
        // Clicking rows without playing should not increment the counter.
        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("0")));
        pressBack();

        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));
        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("0")));
    }

    @Test
    public void playOne() {
        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("0")));
        pressBack();

        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        // Give MediaPlayer a beat to start; start() is sync but this reduces flake.
        SystemClock.sleep(150);
        onView(withId(R.id.playerPlayPauseButton))
                .check(matches(withImageDrawable(R.drawable.ic_pause_black_24dp)));

        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("1")));
    }

    @Test
    public void counter() {
        // Counter increments when starting playback and when changing songs while playing.
        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("0")));
        pressBack();

        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);

        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(6, click()));
        SystemClock.sleep(150);

        openSettings();
        onView(withId(R.id.numberOfSongsPlayedValueText)).check(matches(withText("2")));
    }

    // -----------------------
    // Settings navigation
    // -----------------------
    @Test
    public void clickSettings() {
        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(isDisplayed()));
        pressBack();
        onView(withId(R.id.playerRV)).check(matches(isDisplayed()));
    }

    // -----------------------
    // Settings: cancel vs OK behavior for loop
    // -----------------------
    @Test
    public void cancelLoopButton() {
        // Ensure starting state is loop off.
        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(notChecked()));

        // Toggle, then cancel.
        onView(withId(R.id.loopSwitch)).perform(click());
        onView(withId(R.id.cancelBtn)).perform(click());

        // Re-open: change should be canceled.
        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(notChecked()));
    }

    @Test
    public void cancelLoopBack() {
        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(notChecked()));
        onView(withId(R.id.loopSwitch)).perform(click());
        pressBack();

        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(notChecked()));
    }

    @Test
    public void enableLoop() {
        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(notChecked()));
        onView(withId(R.id.loopSwitch)).perform(click());
        onView(withId(R.id.okBtn)).perform(click());

        openSettings();
        onView(withId(R.id.loopSwitch)).check(matches(checked()));
    }

    // -----------------------
    // Play/Pause affects audio
    // -----------------------
    @Test
    public void playPause() {
        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);
        activityRule.getScenario().onActivity(activity -> {
            MainViewModel vm = new ViewModelProvider(activity).get(MainViewModel.class);
            assertTrue(vm.getPlayer().isPlaying());
        });

        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);
        activityRule.getScenario().onActivity(activity -> {
            MainViewModel vm = new ViewModelProvider(activity).get(MainViewModel.class);
            assertFalse(vm.getPlayer().isPlaying());
        });
    }

    // -----------------------
    // Loop behavior (player screen)
    // -----------------------
    @Test
    public void loop() {
        // Enable loop on player screen.
        onView(withId(R.id.loopIndicator)).perform(click());
        onView(withId(R.id.loopIndicator))
                .check(matches(withBackgroundColor(0xFFFF0000 /* Color.RED */)));

        // Start playing and seek near the end; with loop on, we should still be on the same song.
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));
        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);

        // Drag seek bar close to the end to trigger completion quickly.
        SystemClock.sleep(200);
        AtomicInteger max = new AtomicInteger(0);
        onView(withId(R.id.playerSeekBar)).perform(readSeekBarMax(max));
        int endish = Math.max(0, max.get() - 1200);
        onView(withId(R.id.playerSeekBar)).perform(dragSeekBarTo(endish));
        SystemClock.sleep(3500);

        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));

        // Disable loop, seek near end again, expect advancement.
        onView(withId(R.id.loopIndicator)).perform(click());
        onView(withId(R.id.loopIndicator))
                .check(matches(withBackgroundColor(0xFFFFFFFF /* Color.WHITE */)));

        onView(withId(R.id.playerSeekBar)).perform(readSeekBarMax(max));
        endish = Math.max(0, max.get() - 1200);
        onView(withId(R.id.playerSeekBar)).perform(dragSeekBarTo(endish));
        SystemClock.sleep(3500);

        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("What's Mine")));
    }

    // -----------------------
    // List behaviors (highlighting, transitions)
    // -----------------------
    @Test
    public void list() {
        // Initial highlight on first row.
        onView(withId(R.id.playerRV))
                .check(matches(RecyclerViewMatchers.atPositionHasBackgroundColor(
                        0, R.id.songTitle, 0xFFFFFF00 /* Color.YELLOW */)));

        // Click row 1; highlight should move.
        onView(withId(R.id.playerRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("What's Mine")));
        onView(withId(R.id.playerRV))
                .check(matches(RecyclerViewMatchers.atPositionHasBackgroundColor(
                        1, R.id.songTitle, 0xFFFFFF00 /* Color.YELLOW */)));
        onView(withId(R.id.playerRV))
                .check(matches(RecyclerViewMatchers.atPositionHasBackgroundColor(
                        0, R.id.songTitle, 0xFFFFFFFF /* Color.WHITE */)));
    }

    // -----------------------
    // SeekBar behavior
    // -----------------------
    @Test
    public void slider() {
        // Start then pause so currentPosition stays stable after seeking.
        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);
        onView(withId(R.id.playerPlayPauseButton)).perform(click());
        SystemClock.sleep(150);

        AtomicInteger max = new AtomicInteger(0);
        onView(withId(R.id.playerSeekBar)).perform(readSeekBarMax(max));
        int target = Math.min(1000, Math.max(1, max.get() / 3));

        onView(withId(R.id.playerSeekBar)).perform(dragSeekBarTo(target));
        SystemClock.sleep(300);

        activityRule.getScenario().onActivity(activity -> {
            MainViewModel vm = new ViewModelProvider(activity).get(MainViewModel.class);
            int pos = vm.getPlayer().getCurrentPosition();
            // Tolerance accounts for device timing and framework rounding.
            assertTrue("Expected seek near " + target + "ms, got " + pos + "ms",
                    Math.abs(pos - target) < 900);
        });
    }

    // -----------------------
    // Shuffle behavior
    // -----------------------
    @Test
    public void shuffle() {
        // Snapshot current list ordering from adapter.
        List<Integer> before = new ArrayList<>();
        activityRule.getScenario().onActivity(activity -> {
            RecyclerView rv = activity.findViewById(R.id.playerRV);
            assertNotNull(rv);
            RecyclerView.Adapter<?> a = rv.getAdapter();
            assertNotNull(a);
            // ListAdapter (Kotlin) exposes getCurrentList()
            @SuppressWarnings("unchecked")
            List<SongInfo> cur = (List<SongInfo>) ((androidx.recyclerview.widget.ListAdapter<?, ?>) a).getCurrentList();
            for (SongInfo s : cur) before.add(s.getUniqueId());
        });

        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));

        // Click shuffle until the order changes (extremely likely on first click, but avoid flake).
        List<Integer> after = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.playerPermuteButton)).perform(click());
            SystemClock.sleep(300);
            after.clear();
            activityRule.getScenario().onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.playerRV);
                RecyclerView.Adapter<?> a = rv.getAdapter();
                @SuppressWarnings("unchecked")
                List<SongInfo> cur = (List<SongInfo>) ((androidx.recyclerview.widget.ListAdapter<?, ?>) a).getCurrentList();
                for (SongInfo s : cur) after.add(s.getUniqueId());
            });
            if (!after.equals(before)) break;
        }

        // Current song should remain the same after shuffle.
        onView(withId(R.id.playerCurrentSongText)).check(matches(withText("Dark Star")));
        assertTrue("Shuffle should change ordering (tried a few times)", !after.equals(before));
    }

    // -----------------------
    // Matchers / actions
    // -----------------------
    private static Matcher<View> checked() {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description description) {
                description.appendText("is checked");
            }
            @Override protected boolean matchesSafely(View view) {
                if (!(view instanceof android.widget.CompoundButton)) return false;
                return ((android.widget.CompoundButton) view).isChecked();
            }
        };
    }

    private static Matcher<View> notChecked() {
        return not(checked());
    }

    private static Matcher<View> withImageDrawable(@DrawableRes int resId) {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description description) {
                description.appendText("has image drawable id=").appendValue(resId);
            }
            @Override protected boolean matchesSafely(View view) {
                if (!(view instanceof ImageView)) return false;
                ImageView iv = (ImageView) view;
                Drawable actual = iv.getDrawable();
                if (actual == null) return false;

                Drawable expected = AppCompatResources.getDrawable(view.getContext(), resId);
                if (expected == null) return false;

                // Normalize both drawables by applying the ImageView's tint to the expected drawable.
                expected = expected.mutate();
                actual = actual.mutate();
                if (ImageViewCompat.getImageTintList(iv) != null) {
                    DrawableCompat.setTintList(expected, ImageViewCompat.getImageTintList(iv));
                    if (ImageViewCompat.getImageTintMode(iv) != null) {
                        DrawableCompat.setTintMode(expected, ImageViewCompat.getImageTintMode(iv));
                    }
                }

                int w = view.getWidth();
                int h = view.getHeight();
                if (w <= 0 || h <= 0) return false;

                Bitmap a = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas ca = new Canvas(a);
                actual.setBounds(0, 0, w, h);
                actual.draw(ca);

                Bitmap e = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas ce = new Canvas(e);
                expected.setBounds(0, 0, w, h);
                expected.draw(ce);

                return a.sameAs(e);
            }
        };
    }

    private static Matcher<View> withBackgroundColor(@ColorInt int expectedColor) {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description description) {
                description.appendText("has background color=").appendValue(expectedColor);
            }
            @Override protected boolean matchesSafely(View view) {
                if (!(view.getBackground() instanceof ColorDrawable)) return false;
                int actual = ((ColorDrawable) view.getBackground()).getColor();
                return actual == expectedColor;
            }
        };
    }

    private static ViewAction readSeekBarMax(AtomicInteger outMax) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return allOf(isDisplayed());
            }
            @Override public String getDescription() {
                return "read SeekBar max";
            }
            @Override public void perform(UiController uiController, View view) {
                if (!(view instanceof SeekBar)) throw new AssertionError("Not a SeekBar");
                outMax.set(((SeekBar) view).getMax());
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private static ViewAction dragSeekBarTo(int targetProgress) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return allOf(isDisplayed());
            }
            @Override public String getDescription() {
                return "drag SeekBar to progress " + targetProgress;
            }
            @Override public void perform(UiController uiController, View view) {
                if (!(view instanceof SeekBar)) throw new AssertionError("Not a SeekBar");
                SeekBar sb = (SeekBar) view;
                int max = Math.max(1, sb.getMax());
                int clamped = Math.min(Math.max(0, targetProgress), max);

                CoordinatesProvider start = v -> {
                    int[] pos = new int[2];
                    v.getLocationOnScreen(pos);
                    float x = pos[0] + sb.getPaddingLeft() + 1f;
                    float y = pos[1] + v.getHeight() / 2f;
                    return new float[]{x, y};
                };
                CoordinatesProvider end = v -> {
                    int[] pos = new int[2];
                    v.getLocationOnScreen(pos);
                    float usable = v.getWidth() - sb.getPaddingLeft() - sb.getPaddingRight();
                    float frac = clamped / (float) max;
                    float x = pos[0] + sb.getPaddingLeft() + (usable * frac);
                    float y = pos[1] + v.getHeight() / 2f;
                    return new float[]{x, y};
                };

                new GeneralSwipeAction(Swipe.SLOW, start, end, Press.FINGER)
                        .perform(uiController, view);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    static class RecyclerViewMatchers {
        static Matcher<View> atPositionHasBackgroundColor(
                int position, int childViewId, @ColorInt int expectedColor) {
            return new TypeSafeMatcher<View>() {
                @Override public void describeTo(Description description) {
                    description.appendText("RecyclerView item at position ")
                            .appendValue(position)
                            .appendText(" has background color ")
                            .appendValue(expectedColor);
                }

                @Override protected boolean matchesSafely(View view) {
                    if (!(view instanceof RecyclerView)) return false;
                    RecyclerView rv = (RecyclerView) view;
                    RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
                    if (vh == null) return false; // Not bound/visible; ensure it is by scrolling if needed.
                    View child = (childViewId == View.NO_ID) ? vh.itemView : vh.itemView.findViewById(childViewId);
                    if (child == null) return false;
                    if (!(vh.itemView.getBackground() instanceof ColorDrawable)
                            && !(child.getBackground() instanceof ColorDrawable)) {
                        // Background is set on the row root in onBind; fall back to checking itemView.
                        return false;
                    }
                    Drawable bg = vh.itemView.getBackground();
                    if (bg instanceof ColorDrawable) {
                        return ((ColorDrawable) bg).getColor() == expectedColor;
                    }
                    Drawable bg2 = child.getBackground();
                    return (bg2 instanceof ColorDrawable) && ((ColorDrawable) bg2).getColor() == expectedColor;
                }
            };
        }
    }
}
