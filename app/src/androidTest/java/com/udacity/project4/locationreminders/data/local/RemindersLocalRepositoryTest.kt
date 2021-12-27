package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule() // Synchronously run tasks

    private lateinit var db: RemindersDatabase

    @Before
    fun init() {
        // instantiate an IN MEMORY database
        db = Room.inMemoryDatabaseBuilder(AuthUI.getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun retrieveInvalidId() = runBlocking {

        // When: A call is made for an invalid ID
        val gottenReminder = RemindersLocalRepository(db.reminderDao(),Dispatchers.Main).getReminder("invalid ID")

        // Then:
        assertThat(gottenReminder, instanceOf(Result.Error::class.java))
    }
}