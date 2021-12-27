package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith



@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //lateinit var fakeDataItemList: MutableList<ReminderDTO>
    lateinit var fakeDataSource: FakeDataSource
    lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var coroutineRule = CoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setupViewModel() {
        //Given: the fake data source created in @Before and injected into the view model
        fakeDataSource = FakeDataSource(fakeReminders())
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    private fun fakeReminders(): MutableList<ReminderDTO> {
        val reminder1 = ReminderDTO("test Title 1", "test Description 1", "test Location", 1.1, 1.1, "test Id")
        val reminder2 = ReminderDTO("test Title 2", "test Description 2", "test Location", 1.1, 1.1, "test Id")
        val reminder3 = ReminderDTO("test Title 3", "test Description 3", "test Location", 1.1, 1.1, "test Id")
        val reminder4 = ReminderDTO("test Title 4", "test Description 4", "test Location", 1.1, 1.1, "test Id")
        return mutableListOf(reminder1, reminder2, reminder3, reminder4)
    }

    @Test
    fun loadReminders_corutine_test()= runBlockingTest {

        // When: The loadReminders function is called
        remindersListViewModel.loadReminders()

        // Then: The remindersList has a size greater than zero
        assert(remindersListViewModel.remindersList.value?.size!! > 0)

    }

    @Test
    fun showLoading_liveData_test() {
        //Given
        coroutineRule.pauseDispatcher()

        //When
        remindersListViewModel.loadReminders()

        //Then
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == true)
        coroutineRule.resumeDispatcher()
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == false)
    }

}

@ExperimentalCoroutinesApi
class CoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}

