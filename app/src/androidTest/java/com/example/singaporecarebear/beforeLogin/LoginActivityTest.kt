package com.example.singaporecarebear.beforeLogin
//
//import android.app.Instrumentation.ActivityMonitor
//import android.util.Log
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.*
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.rules.activityScenarioRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
//import androidx.test.rule.ActivityTestRule
//import com.example.singaporecarebear.ForgetPasswordActivity
//import com.example.singaporecarebear.MainActivity
//import com.example.singaporecarebear.R
//import com.example.singaporecarebear.RegisterActivity
//import com.example.singaporecarebear.ui.login.LoginActivity
//import org.hamcrest.CoreMatchers.allOf
//import org.hamcrest.Matchers.endsWith
//import org.junit.Assert.assertNotNull
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//
//@RunWith(AndroidJUnit4::class)
//class LoginActivityTestUI{
//    companion object {
//        private const val TAG = "LoginActivityTestUI"
//
//        // test data
//        private const val TIMEOUT = 1500L
//        private const val SIGN_UP_BUTTON_TEXT = "SIGN UP"
//        private const val SIGN_IN_BUTTON_TEXT = "SIGN IN"
//        private const val FORGET_PWD_TEXT_VIEW = "Forget Your Password?"
//    }
//
//    @get:Rule
//    var activityRule = activityScenarioRule<LoginActivity>()
//
//    @Test
//    fun onLaunch_containsRequiredLoginUI() {
//        Log.i(TAG, """
//            ### 5. UI elements all exist
//            - wait a teeny bit
//            - check text element textViewResult exist
//            - check buttons "$SIGN_UP_BUTTON_TEXT" and "$SIGN_IN_BUTTON_TEXT" exist
//            - check text views "$FORGET_PWD_TEXT_VIEW" exist
//            """.trimIndent())
//
//        Thread.sleep(TIMEOUT)
//
//        onView(withId(R.id.textView2))
//            .check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(endsWith("TextView")), withText(FORGET_PWD_TEXT_VIEW)))
//            .check(matches(isDisplayed()))
//
//        // Check if buttons exist
//        onView(withId(R.id.login)).check(matches(isDisplayed()))
//        onView(withId(R.id.register)).check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(endsWith("Button")), withText(SIGN_UP_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(endsWith("Button")), withText(SIGN_IN_BUTTON_TEXT)))
//            .check(matches(isDisplayed()))
//    }
//}
//
//@RunWith(AndroidJUnit4::class)
//class LoginActivityTestNavigation{
//    companion object {
//        private const val TAG = "LoginActivityTestNavigation"
//
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
//    @get:Rule
//    var intentsRule = ActivityTestRule(LoginActivity::class.java)
//
//    @Test
//    fun launchMainActivityTest() {
//        val activityMonitor: ActivityMonitor = getInstrumentation()
//            .addMonitor(MainActivity::class.java.name, null, false)
//
//        // fill up email and password editText and click login btn
//        onView(withId(R.id.email)).perform(replaceText(email)).perform(closeSoftKeyboard());
//        onView(withId(R.id.password)).perform(replaceText(password)).perform(closeSoftKeyboard());
//        onView(withId(R.id.login)).perform(click())
//        val targetActivity: MainActivity =
//            (activityMonitor.waitForActivityWithTimeout(50000) as? MainActivity)!! // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//
//        //-------------------------------------
////        Intents.init()
////        onView(withId(R.id.login)).perform(click())
////        intentsRule.launchActivity(Intent())
////        intended(hasComponent(MainActivity::class.java.name))
////        Intents.release()
////        intending(toPackage("com.example.singaporecarebear.MainActivityyy"))
//
//    }
//
//    @Test
//    fun launchSignUpTest() {
//        val activityMonitor: ActivityMonitor = getInstrumentation()
//            .addMonitor(RegisterActivity::class.java.name, null, false)
//
//        // click on sign up btn
//        onView(withId(R.id.register)).perform(click())
//        val targetActivity: RegisterActivity =
//            (activityMonitor.waitForActivityWithTimeout(50000) as? RegisterActivity)!! // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//
//    @Test
//    fun launchForgetPasswordTest() {
//        val activityMonitor: ActivityMonitor = getInstrumentation()
//            .addMonitor(ForgetPasswordActivity::class.java.name, null, false)
//
//        // click on forget password TextView
//        onView(withId(R.id.forgetPasswordTextView)).perform(click())
//        val targetActivity: ForgetPasswordActivity =
//            (activityMonitor.waitForActivityWithTimeout(50000) as? ForgetPasswordActivity)!! // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//}
