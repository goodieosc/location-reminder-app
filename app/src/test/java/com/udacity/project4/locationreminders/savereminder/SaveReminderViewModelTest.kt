package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    lateinit var saveReminderViewModel: SaveReminderViewModel
    lateinit var fakeDataItem: ReminderDataItem
    lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init(){
        // Given a fresh ViewModel
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

        fakeDataItem = ReminderDataItem(
            "test Title",
            "test Description",
            "test Location",
            1.1,
            1.1,
            "test Id")
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun whenSavingANewDataItem_InputFakeDataLiem_validateAsTrue() {

        // When adding a data item
        saveReminderViewModel.validateAndSaveReminder(fakeDataItem)

        // Then the validateEnteredData is not null, and returns true
        assertThat(saveReminderViewModel.validateEnteredData(fakeDataItem), `is`(true))
    }

    @Test
    fun onClear_is_null() {

        saveReminderViewModel.apply {
                fakeDataItem
        }

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value, isEmptyOrNullString())
        assertThat(saveReminderViewModel.reminderDescription.value, isEmptyOrNullString())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, isEmptyOrNullString())
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value, `is`(nullValue()))
    }


}