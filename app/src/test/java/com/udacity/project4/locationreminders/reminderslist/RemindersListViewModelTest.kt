package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @Test
    fun load_reminders_and_list() {

        // Given a fresh ViewModel
        val RemindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext())

        // When adding a new task
        RemindersListViewModel.loadReminders()

        // Then the new task event is triggered
        // TODO test LiveData
    }

}