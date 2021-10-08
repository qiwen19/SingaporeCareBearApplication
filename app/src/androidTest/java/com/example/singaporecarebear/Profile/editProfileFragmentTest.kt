package com.example.singaporecarebear.Profile

//import android.app.Instrumentation
//import android.util.Log
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.action.ViewActions.replaceText
//import androidx.test.espresso.assertion.ViewAssertions
//import androidx.test.espresso.matcher.ViewMatchers
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
//import androidx.test.rule.ActivityTestRule
//import com.example.singaporecarebear.MainActivity
//import com.example.singaporecarebear.R
//import com.example.singaporecarebear.ui.login.LoginActivity
//import junit.framework.Assert.assertNotNull
//import org.hamcrest.CoreMatchers
//import org.hamcrest.CoreMatchers.allOf
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//// login first
//@RunWith(AndroidJUnit4::class)
//class EditProfileFragmentTestUIAfterLogin {
//    companion object {
//        private const val TAG = "editProfileFragmentTestUIAfterLogin"
//
//        private lateinit var email: String
//        private lateinit var password: String
//
//        // test data
//        private const val TIMEOUT = 1500L
//        private const val SUBMIT_CHANGES_BUTTON_TEXT = "submit changes"
//        private const val BACK_BUTTON_TEXT = "BACK"
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
//    fun launchMainActivityTest_EditProfile() {
//        val activityMonitor: Instrumentation.ActivityMonitor = getInstrumentation()
//            .addMonitor(MainActivity::class.java.name, null, false)
//
//        // fill up email and password editText and click login btn
//        onView(withId(R.id.email)).perform(replaceText(email))
//        onView(withId(R.id.password)).perform(replaceText(password))
//        onView(withId(R.id.login)).perform(click())
//        val targetActivity: MainActivity =
//            (activityMonitor.waitForActivityWithTimeout(50000) as? MainActivity)!! // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//
//    @Test
//    fun onLaunch_containsRequiredEditProfileUI() {
//
//        launchFragmentInContainer<editProfileFragment>()
//
//        Log.i(TAG, """
//            ### 5. UI elements all exist
//            - wait a teeny bit
//            - check editText exist
//            - check buttons "$SUBMIT_CHANGES_BUTTON_TEXT" and "$BACK_BUTTON_TEXT" exist
//            """.trimIndent())
//
//        Thread.sleep(TIMEOUT)
//
//        // Check if buttons exist
//        onView(withId(R.id.submitBtn))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        onView(withId(R.id.backBtn))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//
//        // Check if EditText exist
//        onView(withId(R.id.editTextUsername))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        onView(withId(R.id.editTextEmail))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        onView(withId(R.id.editCurrentPassword))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        onView(withId(R.id.editTextNewPassword))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        onView(withId(R.id.editTextConfirmPassword))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//
//        // Check if Button Text matches
//        onView(allOf(withClassName(CoreMatchers.endsWith("Button")),
//                withText(SUBMIT_CHANGES_BUTTON_TEXT)
//            )
//        )
//            .check(ViewAssertions.matches(isDisplayed()))
//
//        onView(allOf(withClassName(CoreMatchers.endsWith("Button")),
//                withText(BACK_BUTTON_TEXT)
//            )
//        )
//            .check(ViewAssertions.matches(isDisplayed()))
//    }
//}
//
////// need to login before running the testcase
////@RunWith(AndroidJUnit4::class)
////class editProfileFragmentTestUI{
////    companion object {
////        private const val TAG = "editProfileFragmentTestUI"
////
////        // test data
////        private const val TIMEOUT = 1500L
////        private const val SUBMIT_CHANGES_BUTTON_TEXT = "submit changes"
////        private const val BACK_BUTTON_TEXT = "BACK"
////    }
////
////    @Test
////    fun onLaunch_containsRequiredViewProfileUI() {
////
////        launchFragmentInContainer<editProfileFragment>()
////
////        Log.i(TAG, """
////            ### 5. UI elements all exist
////            - wait a teeny bit
////            - check editText exist
////            - check buttons "$SUBMIT_CHANGES_BUTTON_TEXT" and "$BACK_BUTTON_TEXT" exist
////            """.trimIndent())
////
////        Thread.sleep(TIMEOUT)
////
////        // Check if buttons exist
////        onView(withId(R.id.submitBtn))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////        onView(withId(R.id.backBtn))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////
////        // Check if EditText exist
////        onView(withId(R.id.editTextUsername))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////        onView(withId(R.id.editTextEmail))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////        onView(withId(R.id.editCurrentPassword))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////        onView(withId(R.id.editTextNewPassword))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////        onView(withId(R.id.editTextConfirmPassword))
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////
////        // Check if Button Text matches
////        Espresso.onView(
////            CoreMatchers.allOf(
////                ViewMatchers.withClassName(CoreMatchers.endsWith("Button")),
////                ViewMatchers.withText(SUBMIT_CHANGES_BUTTON_TEXT)
////            )
////        )
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////
////        Espresso.onView(
////            CoreMatchers.allOf(
////                ViewMatchers.withClassName(CoreMatchers.endsWith("Button")),
////                ViewMatchers.withText(BACK_BUTTON_TEXT)
////            )
////        )
////            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
////    }
//}