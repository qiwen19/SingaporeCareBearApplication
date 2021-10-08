package com.example.singaporecarebear

//import android.util.Log
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import org.hamcrest.CoreMatchers
//import org.hamcrest.CoreMatchers.allOf
//import org.junit.Test
//import org.junit.runner.RunWith
//
//// need to login before running the testcase
//@RunWith(AndroidJUnit4::class)
//class MapsFragmentTestUI{}
//    companion object {
//        private const val TAG = "MapsFragmentTestUI"
//
//        // test data
//        private const val TIMEOUT = 1500L
//        private const val LOCATE_BUTTON_TEXT = "LOCATE"
//        private const val SEND_REQUEST_BUTTON_TEXT = "SEND REQUEST"
//    }
//
//    @Test
//    fun onLaunch_containsRequiredViewMapUI() {
//
//        launchFragmentInContainer<MapsFragment>()
//
//        Log.i(
//            TAG, """
//            ### 5. UI elements all exist
//            - wait a teeny bit
//            - check editText exist
//            - check buttons "$LOCATE_BUTTON_TEXT" and "$SEND_REQUEST_BUTTON_TEXT" exist
//            """.trimIndent())
//
//        Thread.sleep(TIMEOUT)
//
//        // Check if buttons exist
//        onView(withId(R.id.searchBtn))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.sendRequestBtn))
//            .check(matches(isDisplayed()))
//
//        // Check if EditText exist
//        onView(withId(R.id.addressEditText))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.helpRequiredEditText))
//            .check(matches(isDisplayed()))
//
//        // Check if Button Text matches
//        onView(allOf(withClassName(CoreMatchers.endsWith("Button")),
//            withText(LOCATE_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(CoreMatchers.endsWith("Button")),
//            withText(SEND_REQUEST_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//    }
//}