package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule() // Synchronously run tasks

    lateinit var db: RemindersDatabase
    lateinit var fakeDataItem: ReminderDTO

    @Before
    fun init() {
        // instantiate an IN MEMORY database
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Given: A new entry is saved
        fakeDataItem = ReminderDTO(
            "test Title",
            "test Description",
            "test Location",
            1.1,
            1.1,
            "test Id")
    }

    @Test
    fun addDataAndRetrieveAgain() = runBlockingTest {

        db.reminderDao().saveReminder(fakeDataItem)

        // When: getReminderById is then called
        val gottenReminder = db.reminderDao().getReminderById("test Id")

        // Then: Check the values matched the fakeDataItem
        assertThat(gottenReminder?.id, `is`(fakeDataItem.id))
        assertThat(gottenReminder?.title, `is`(fakeDataItem.title))
        assertThat(gottenReminder?.description, `is`(fakeDataItem.description))
    }
}