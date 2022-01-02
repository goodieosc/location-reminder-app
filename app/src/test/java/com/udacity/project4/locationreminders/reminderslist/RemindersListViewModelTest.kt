package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.bouncycastle.asn1.ocsp.ServiceLocator
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    lateinit var fakeDataSource: FakeDataSource
    lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setupViewModel() {
        //Given: the fake data source created in @Before and injected into the view model
        fakeDataSource = FakeDataSource(fakeReminders())
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun fakeReminders(): MutableList<ReminderDTO> {
        val reminder1 = ReminderDTO("test Title 1", "test Description 1", "test Location", 1.1, 1.1, "test Id")
        val reminder2 = ReminderDTO("test Title 2", "test Description 2", "test Location", 1.1, 1.1, "test Id")
        val reminder3 = ReminderDTO("test Title 3", "test Description 3", "test Location", 1.1, 1.1, "test Id")
        val reminder4 = ReminderDTO("test Title 4", "test Description 4", "test Location", 1.1, 1.1, "test Id")
        return mutableListOf(reminder1, reminder2, reminder3, reminder4)
    }

    @Test
    fun loadReminders_corutine_test()= coroutineRule.runBlockingTest {

        // When: The loadReminders function is called
        remindersListViewModel.loadReminders()

        // Then: The remindersList has a size greater than zero
        assert(remindersListViewModel.remindersList.value?.size!! > 0)

    }

    @Test
    fun showLoading_liveData_test() = coroutineRule.runBlockingTest {
        fakeDataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders not found"))
    }

    @Test
    fun loadReminders_error() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

}