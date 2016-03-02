package me.samthompson.bubbleactions_sample;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by sam on 2/9/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class VersionCompatibilityTest {

    @Rule
    public ActivityTestRule<MenuActivity> activityTestRule =
            new ActivityTestRule<>(MenuActivity.class);

    @Test
    public void testPreconditions() {
        assertNotNull(activityTestRule);
        onView(withId(R.id.text_view)).check(matches(isDisplayed()));
    }

    /**
     * If we get a null pointer when showing the BubbleActions, then there is a problem with the
     * reflection
     */
    @Test
    public void testNoNullPointer() {
        try {
            onView(withId(R.id.text_view)).perform(longClick());
        } catch (Exception e) {

            // if we get a null pointer, then there
            if (e.getCause() instanceof NullPointerException) {
                assertFalse(true);
            }
        }
    }

}
