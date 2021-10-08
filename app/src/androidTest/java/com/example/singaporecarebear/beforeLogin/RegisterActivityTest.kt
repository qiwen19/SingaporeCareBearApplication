package com.example.singaporecarebear.beforeLogin

//import android.app.Instrumentation
//import android.util.Log
//import androidx.test.espresso.Espresso
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions
//import androidx.test.espresso.action.ViewActions.*
//import androidx.test.espresso.assertion.ViewAssertions
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.rules.activityScenarioRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.rule.ActivityTestRule
//import com.example.singaporecarebear.MainActivity
//import com.example.singaporecarebear.R
//import com.example.singaporecarebear.RegisterActivity
//import com.example.singaporecarebear.ui.login.LoginActivity
//import org.hamcrest.CoreMatchers
//import org.hamcrest.CoreMatchers.allOf
//import org.hamcrest.CoreMatchers.endsWith
//import org.hamcrest.Matchers
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class RegisterActivityTestUI{
//    companion object {
//        private const val TAG = "RegisterActivityTestUI"
//
//        // test data
//        private const val TIMEOUT = 1500L
//        private const val SIGN_UP_BUTTON_TEXT = "Register"
//
//        private lateinit var validName: String
//        private lateinit var validPassword: String
//        private lateinit var validConfirmpassword: String
//        private lateinit var validEmail: String
//        private lateinit var dob: String
//
//        private lateinit var invalidConfirmpassword: String
//        private lateinit var existingEmail: String
//        private lateinit var wrongFormattedEmail: String
//    }
//
//    @Before
//    fun initValidString() {
//        // Specify a valid string.
//
//        validName = "TestCase" // get a random generator
//        validPassword = "P@ssw0rd"
//        validConfirmpassword = "P@ssw0rd"
//        validEmail = "testcaseEmail@hotmail.com"
//        dob = "11/11/1990"
//
//        invalidConfirmpassword = "P@ssw0rddd"
//        existingEmail = "ong.qi.wen@hotmail.com"
//        wrongFormattedEmail = "ong."
//    }
//    @get:Rule
//    var activityRule = activityScenarioRule<RegisterActivity>()
//
//    @Test
//    fun onLaunch_containsRequiredLoginUI() {
//        Log.i(TAG, """
//            ### 5. UI elements all exist
//            - wait a teeny bit
//            - check buttons "$SIGN_UP_BUTTON_TEXT" exist
//            """.trimIndent())
//
//        Thread.sleep(TIMEOUT)
//
//        // Check if buttons exist
//        onView(withId(R.id.registerButton))
//            .check(matches(isDisplayed()))
//
//        // Check if EditText Exist
//        onView(withId(R.id.nameEditText))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.passwordEditText))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.confirmPasswordEditText))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.emailEditText))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.DOBEditText))
//            .check(matches(isDisplayed()))
//
//        onView(allOf(withClassName(Matchers.endsWith("Button")),
//                withText(SIGN_UP_BUTTON_TEXT)
//            )
//        ).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun fullName_empty_error() {
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in your name")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun password_empty_error() { //
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in your password!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun not_secure_password_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText("P@ss")).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Password has to contain at least one special character, one digit, one alphabet, one capital letter and at least 8 characters long!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun confirmPassword_empty_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in your confirm password!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun password_not_match_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText(invalidConfirmpassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Confirm Password does not match!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun email_empty_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in your email!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun email_wrong_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.emailEditText)).perform(replaceText(wrongFormattedEmail)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in a valid email!")).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun dob_empty_error() {
//        onView(withId(R.id.nameEditText)).perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText)).perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.emailEditText)).perform(replaceText(validEmail)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        onView(withText("Please fill in your dob!")).check(matches(isDisplayed()))
//    }
//}
//
//@RunWith(AndroidJUnit4::class)
//class RegisterActivityTestNavigation{
//    companion object {
//        private const val TAG = "RegisterActivityTestNavigation"
//        private lateinit var validName: String
//        private lateinit var validPassword: String
//        private lateinit var validConfirmpassword: String
//        private lateinit var validEmail: String
//        private lateinit var dob: String
//    }
//
//    @Before
//    fun initValidString() {
//        // Specify a valid string.
//
//        validName = "TestCase" // get a random generator
//        validPassword = "P@ssw0rd"
//        validConfirmpassword = "P@ssw0rd"
//        validEmail = "testcaseEmail@hotmail.com"
//        dob = "11/11/1990"
//    }
//
//    @get:Rule
//    var intentsRule = ActivityTestRule(RegisterActivity::class.java)
//
//    @Test
//    fun launchLoginActivityTest() {
//        val activityMonitor: Instrumentation.ActivityMonitor = InstrumentationRegistry.getInstrumentation()
//            .addMonitor(LoginActivity::class.java.name, null, false)
//
//        // fill up all valid details in editText and click register btn
//        onView(withId(R.id.nameEditText))
//            .perform(replaceText(validName)).perform(closeSoftKeyboard());
//        onView(withId(R.id.passwordEditText))
//            .perform(replaceText(validPassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.confirmPasswordEditText))
//            .perform(replaceText(validConfirmpassword)).perform(closeSoftKeyboard());
//        onView(withId(R.id.emailEditText))
//            .perform(replaceText(validEmail)).perform(closeSoftKeyboard());
//        onView(withId(R.id.DOBEditText))
//            .perform(replaceText(dob)).perform(closeSoftKeyboard());
//        onView(withId(R.id.registerButton)).perform(click())
//        val targetActivity: LoginActivity =
//            activityMonitor.waitForActivityWithTimeout(50000) as LoginActivity // By using ActivityMonitor
//
//        assertNotNull("Target Activity is not launched", targetActivity)
//    }
//
//}