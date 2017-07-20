package io.github.fmilitao.shopper;


import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.fmilitao.shopper.shops.ShopsActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

// TODO see https://stackoverflow.com/questions/29908110/how-to-disable-animations-in-code-when-running-espresso-tests
// to disable animations before testing or tests may fail

/*

The following commands to not work!? I ended up having to go to the menu manually.

am force-stop -n "io.github.fmilitao.shopper"

 ./adb shell
settings put global window_animation_scale 0.0 
settings put global transition_animation_scale 0.0
settings put global animator_duration_scale 0.0

 afterwards
 ./adb shell
settings put global window_animation_scale 1.0 
settings put global transition_animation_scale 1.0
settings put global animator_duration_scale 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScreenTest {

    @Rule
    public ActivityTestRule<ShopsActivity> mShopsActivityTestRule =
            new ActivityTestRule<>(ShopsActivity.class);

    /**
     * Tests adding and then deleting a list/shop.
     * The test should leave the app in a state that is equivalent to the initial one.
     */
    @Test
    public void testAddDeleteList() {
        final String newShopName = "Shop1";

        // check that the shop name does not originally exist
        onView(withText(newShopName)).check(doesNotExist());

        addShop(newShopName);

        // check if the new shop is now present
        onView(withText(newShopName)).check(matches(isDisplayed()));

        // swipe left to delete
        onView(withText(newShopName)).perform(withCustomConstraints(swipeLeft(), isDisplayingAtLeast(50)));

        onView(withText(newShopName)).check(doesNotExist());
    }


    /**
     * Adds a new shop.
     *
     * @param newShopName the name of the new shop.
     */
    private void addShop(String newShopName) {
        // click the add list button
        onView(withId(R.id.add_list)).perform(click());

        // check that the correct dialog is displayed
        onView(withText(R.string.NEW_LIST))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // check if the add list elements are displayed
        onView(withId(R.id.dialog_shop_name)).check(matches(isDisplayed()));
        onView(withId(R.id.dialog_shop_clipboard)).check(matches(isDisplayed()));

        // add the new shop name
        onView(withId(R.id.dialog_shop_name))
                .perform(typeText(newShopName), closeSoftKeyboard());

        // press OK button (button1 is OK, button2 is Cancel)
        onView(withId(android.R.id.button1)).perform(click());
    }


    /**
     * Custom matcher needed due to the swipe animation.
     * from: https://stackoverflow.com/questions/33505953/espresso-how-to-test-swiperefreshlayout
     */
    public static ViewAction withCustomConstraints(
            final ViewAction action,
            final Matcher<View> constraints
    ) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return constraints;
            }

            @Override
            public String getDescription() {
                return action.getDescription();
            }

            @Override
            public void perform(UiController uiController, View view) {
                action.perform(uiController, view);
            }
        };
    }
}
