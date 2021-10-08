package com.example.singaporecarebear.Profile
//
//import android.app.Instrumentation
//import android.util.Log
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
//import androidx.test.rule.ActivityTestRule
//import com.example.singaporecarebear.MainActivity
//import com.example.singaporecarebear.R
//import com.example.singaporecarebear.ui.login.LoginActivity
//import junit.framework.Assert
//import org.hamcrest.CoreMatchers.allOf
//import org.hamcrest.CoreMatchers.endsWith
//import org.junit.Assert.assertNotNull
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//
//@RunWith(AndroidJUnit4::class)
//class ViewProfileFragmentTestUIAfterLogin{
//    companion object {
//        private const val TAG = "viewProfileFragmentTestUIAfterLogin"
//
//        private lateinit var email: String
//        private lateinit var password: String
//
//        // test data
//        private const val TIMEOUT = 1500L
//        private const val EDIT_PROFILE_BUTTON_TEXT = "Edit Profile"
//        private const val LOGOUT_BUTTON_TEXT = "Logout"
//    }
//
//    @Before
//    fun initValidString() {
//        // Specify a valid string.
//        email = "ong.qi.wen@hotmail.com"
//        password = "P@ssw0rd"
//    }
//
//    // login to the application first
//    @get:Rule
//    var intentsRule = ActivityTestRule(LoginActivity::class.java)
//
//    @Before
//    fun launchMainActivityTest_ViewProfile() {
//        val activityMonitor: Instrumentation.ActivityMonitor = getInstrumentation()
//            .addMonitor(MainActivity::class.java.name, null, false)
//
//        // fill up email and password editText and click login btn
//        onView(withId(R.id.email)).perform(
//            ViewActions.replaceText(email)
//        )
//        onView(withId(R.id.password)).perform(
//            ViewActions.replaceText(password)
//        )
//        onView(withId(R.id.login)).perform(click())
//        val targetActivity: MainActivity =
//            (activityMonitor.waitForActivityWithTimeout(50000) as? MainActivity)!! // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//
//    @Test
//    fun onLaunch_containsRequiredViewProfileUI() {
//
//        launchFragmentInContainer<viewProfileFragment>()
//
//        Log.i(TAG, """
//            ### 5. UI elements all exist
//            - wait a teeny bit
//            - check text element textViewResult exist
//            - check buttons "$EDIT_PROFILE_BUTTON_TEXT" and "$LOGOUT_BUTTON_TEXT" exist
//            """.trimIndent())
//
//        Thread.sleep(TIMEOUT)
//
//        // Check if buttons exist
//        onView(withId(R.id.editBtn)).check(matches(isDisplayed()))
//        onView(withId(R.id.logoutBtn)).check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(endsWith("Button")), withText(EDIT_PROFILE_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(endsWith("Button")), withText(LOGOUT_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//    }
//
//}
//
//@RunWith(AndroidJUnit4::class)
//class ViewProfileFragmentTestNavigationAfterLogin{
//    companion object {
//        private const val TAG = "viewProfileFragmentTestNavigationAfterLogin"
//        private lateinit var email: String
//        private lateinit var password: String
//    }
//
//    @Before
//    fun initValidString() {
//        // Specify a valid string.
//        email = "ong.qi.wen@hotmail.com"
//        password = "P@ssw0rd"
//    }
//
//    // login to the application first
//    @get:Rule
//    var intentsRule = ActivityTestRule(LoginActivity::class.java)
//
//    @Before
//    fun launchMainActivityTest() {
//        val activityMonitor: Instrumentation.ActivityMonitor = getInstrumentation()
//            .addMonitor(MainActivity::class.java.name, null, false)
//
//        // fill up email and password editText and click login btn
//        onView(withId(R.id.email)).perform(
//            ViewActions.replaceText(email)
//        )
//        onView(withId(R.id.password)).perform(
//            ViewActions.replaceText(password)
//        )
//        onView(withId(R.id.login)).perform(click())
//        val targetActivity: MainActivity? =
//            activityMonitor.waitForActivityWithTimeout(50000) as? MainActivity // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//
//    @Test
//    fun launchLogoutTest() {
//        // can only be run once after user login
//        launchFragmentInContainer<viewProfileFragment>()
//        val activityMonitor: Instrumentation.ActivityMonitor = getInstrumentation()
//            .addMonitor(LoginActivity::class.java.name, null, false)
//
//        // click logout btn
//        onView(withId(R.id.logoutBtn)).perform(click())
//        val targetActivity: LoginActivity =
//            activityMonitor.waitForActivityWithTimeout(50000) as LoginActivity // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//}
