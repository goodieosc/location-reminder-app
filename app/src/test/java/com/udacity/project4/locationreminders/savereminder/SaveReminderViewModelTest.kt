package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @Test
    fun save_reminder() {

        // Given a fresh ViewModel

        FakeDataSource()

        val SaveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext())

        // When adding a new task
        SaveReminderViewModel.validateAndSaveReminder()

        // Then the new task event is triggered
        // TODO test LiveData
    }


}