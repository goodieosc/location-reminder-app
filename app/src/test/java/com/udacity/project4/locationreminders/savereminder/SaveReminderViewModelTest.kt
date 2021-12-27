package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @Test
    fun whenSavingANewDataItem_InputFakeDataLiem_validateAsTrue() {

        // Given a fresh ViewModel
        val fakeDataSource = FakeDataSource()
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

        // When adding a data item
        val fakeDataItem = ReminderDataItem(
            "test Title",
            "test Description",
            "test Location",
            1.1,
            1.1,
            "test Id")

        saveReminderViewModel.validateAndSaveReminder(fakeDataItem)

        // Then the validateEnteredData is not null, and returns true
        assertThat(saveReminderViewModel.validateEnteredData(fakeDataItem), `is`(true))


        // TODO test LiveData
    }


}