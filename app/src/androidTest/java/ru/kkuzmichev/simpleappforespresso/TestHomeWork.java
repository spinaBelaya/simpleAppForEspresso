package ru.kkuzmichev.simpleappforespresso;

import android.Manifest;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static org.hamcrest.Matchers.anyOf;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import org.junit.rules.TestWatcher;
import static androidx.test.internal.platform.util.TestOutputEmitter.takeScreenshot;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ru.kkuzmichev.simpleappforespresso.CustomMatchers.itemVisible;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import androidx.test.espresso.matcher.BoundedMatcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.qameta.allure.android.runners.AllureAndroidJUnit4;
import io.qameta.allure.kotlin.Allure;

@RunWith(AllureAndroidJUnit4.class)
public class TestHomeWork {

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, org.junit.runner.Description description) {
            String className = description.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            String methodName = description.getMethodName();
            takeScreenshot(className + "#" + methodName);
        }
    };

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            );

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    ViewInteraction mainText = onView(withId(R.id.text_home));

    @Test
    public void textCheckPositive() {
        Allure.feature("checkPositive");
        mainText.check(ViewAssertions.matches(withText("This is home fragment")));
    }

    @Test
    public void textCheckNegative() {
        Allure.feature("checkNegative");
        mainText.check(ViewAssertions.matches(withText("This is not a home fragment")));
    }

    @Test
    public void testIntent() {
        Allure.feature("testIntent");
        Intents.init();
        try {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        } catch (Exception e) {
            System.out.println("Error on testIntent: " + e);
        }
        onView(anyOf(withText(R.string.action_settings), withId(R.id.action_settings))).perform(click());
        Intents.intended(hasData("https://google.com"));
    }

    @Before
    public void registerIdlingResources() {
        Allure.feature("registerIdlingResources");
        IdlingRegistry.getInstance().register(EspressoIdlingResources.idlingResource);
    }

    @After
    public void unregisterIdlingResources() {
        Allure.feature("unregisterIdlingResources");
        IdlingRegistry.getInstance().unregister(EspressoIdlingResources.idlingResource);
    }

    @Test
    public void testGalleryList() {
        Allure.feature("testGalleryList");
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_gallery)).perform(click());
        onView(withId(R.id.recycle_view)).check(ViewAssertions.matches (CustomMatchers.withListSize(10)));
    }

    @Test
    public void testItemElementDisplayed() {
        Allure.feature("testItemElementDisplayed");
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_gallery)).perform(click());
        ViewInteraction recyclerView = onView(withId(R.id.recycle_view));
        recyclerView.check(ViewAssertions.matches(itemVisible(0, isDisplayed())));
    }

    private void takeScreenshot(String name) {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/screenshots/");
        if (!path.exists()) {
            path.mkdirs();
        }
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String filename = name + ".png";
        device.takeScreenshot(new File(path, filename));
        try {
            Allure.attachment(filename, new FileInputStream(new File(path, filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class CustomMatchers {

    public static Matcher<View> itemVisible(final int position, @NonNull final Matcher<View> itemMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView has item at " + position + "'th position : ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely (final View view) {
                RecyclerView.Adapter adapter = ((RecyclerView) view).getAdapter();
                return adapter.getItemCount() == size;
            }

            @Override
            public void describeTo (final Description description) {
                description.appendText ("RecyclerView should have " + size + " items");
            }
        };
    }
}