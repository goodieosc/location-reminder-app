package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    lateinit var navController: NavController
    lateinit var fragmentScenario: FragmentScenario<ReminderListFragment>

    @Before
    fun init(){
        //Given
        fragmentScenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        navController = mock(NavController::class.java)
        fragmentScenario.onFragment {Navigation.setViewNavController(it.view!!, navController)}
    }

    @Test
    fun fabButtonTestNavigation() {

        //When the FAB is clicked
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Navigate to the next fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}