package com.example.noteexample


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class InsertAndEditNote {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE"
        )

    @Test
    fun insertAndEditNote() {
        val floatingActionButton = onView(
            allOf(
                withId(R.id.fab_to_insert), withContentDescription("Insert note"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_host_fragment),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.title_edit),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.edit_recycler),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("1"), closeSoftKeyboard())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.save_note), withContentDescription("Сохранить"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar_note_edit),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val recyclerView = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(
                    withId(R.id.main_fragment),
                    1
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, longClick()))

        val appCompatImageView = onView(
            allOf(
                withId(R.id.action_mode_close_button), withContentDescription("Done"),
                childAtPosition(
                    allOf(
                        withId(R.id.action_mode_bar),
                        childAtPosition(
                            withId(R.id.action_bar_root),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageView.perform(click())

        val recyclerView2 = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(
                    withId(R.id.main_fragment),
                    1
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val actionMenuItemView2 = onView(
            allOf(
                withId(R.id.edit_item), withContentDescription("Edit"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar_one_note),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView2.perform(click())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.first_note_edit),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.edit_recycler),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("2"), closeSoftKeyboard())

        val appCompatImageButton = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar_note_edit),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())

        val materialButton = onView(
            allOf(
                withId(android.R.id.button1), withText("Да"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        materialButton.perform(scrollTo(), click())

        val recyclerView3 = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(
                    withId(R.id.main_fragment),
                    1
                )
            )
        )
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val actionMenuItemView3 = onView(
            allOf(
                withId(R.id.edit_item), withContentDescription("Edit"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar_one_note),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView3.perform(click())

        val actionMenuItemView4 = onView(
            allOf(
                withId(R.id.insert_photo), withContentDescription("photo"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar_note_edit),
                        1
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        actionMenuItemView4.perform(click())

        val materialTextView = onData(anything())
            .inAdapterView(
                allOf(
                    withId(R.id.select_dialog_listview),
                    childAtPosition(
                        withId(R.id.contentPanel),
                        0
                    )
                )
            )
            .atPosition(1)
        materialTextView.perform(click())

        val recyclerView4 = onView(
            allOf(
                withId(R.id.gallery_recycler_view),
                childAtPosition(
                    withId(R.id.gallery_motion),
                    4
                )
            )
        )
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val appCompatImageView2 = onView(
            allOf(
                withId(R.id.accept_selected_photos),
                withContentDescription("Accept selected photos"),
                childAtPosition(
                    allOf(
                        withId(R.id.gallery_motion),
                        childAtPosition(
                            withId(R.id.design_bottom_sheet),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageView2.perform(click())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.note_editText_first),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.edit_card),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(replaceText("3"), closeSoftKeyboard())

        val appCompatImageButton2 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar_note_edit),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageButton2.perform(click())

        val materialButton2 = onView(
            allOf(
                withId(android.R.id.button1), withText("Да"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        materialButton2.perform(scrollTo(), click())

        val recyclerView5 = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(
                    withId(R.id.main_fragment),
                    1
                )
            )
        )
        recyclerView5.perform(actionOnItemAtPosition<ViewHolder>(0, longClick()))

        val actionMenuItemView5 = onView(
            allOf(
                withId(R.id.delete_action), withContentDescription("Delete"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_mode_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView5.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(android.R.id.button1), withText("Да"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        materialButton3.perform(scrollTo(), click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
