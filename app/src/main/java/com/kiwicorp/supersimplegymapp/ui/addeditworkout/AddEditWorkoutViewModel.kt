package com.kiwicorp.supersimplegymapp.ui.addeditworkout

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwicorp.supersimplegymapp.Event
import com.kiwicorp.supersimplegymapp.data.*
import com.kiwicorp.supersimplegymapp.data.source.ActivityRepository
import com.kiwicorp.supersimplegymapp.data.source.RoutineRepository
import com.kiwicorp.supersimplegymapp.data.source.WorkoutRepository
import com.kiwicorp.supersimplegymapp.ui.chooseactivitycommon.ChooseActivityListAdapter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class AddEditWorkoutViewModel @ViewModelInject constructor(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    activityRepository: ActivityRepository
): ViewModel(), ChooseActivityListAdapter.ChooseActivityActions {
    //initialize workout id here it can be used for entries
    private var workoutId = UUID.randomUUID().toString()

    private val _date = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> = _date

    private val _entries = MutableLiveData(listOf<WorkoutEntryWithActivity>())
    val entries: LiveData<List<WorkoutEntryWithActivity>> = _entries

    val activities = activityRepository.activities

    private val _navigateToChooseActivityFragment = MutableLiveData<Event<Unit>>()
    val navigateToChooseActivityFragment: LiveData<Event<Unit>> = _navigateToChooseActivityFragment

    private val _close = MutableLiveData<Event<Unit>>()
    val close: LiveData<Event<Unit>> = _close

    fun loadWorkout(workoutId: String) {
        // stop if workout already loaded (needed because all changes will be undone when navigating
        // from ChooseActivityFragment to AddEditWorkoutFragment)
        if (workoutId == this.workoutId) {
            return
        }
        viewModelScope.launch {
            val workoutWithEntries = workoutRepository.getWorkout(workoutId)
            this@AddEditWorkoutViewModel.workoutId = workoutId
            _date.value = workoutWithEntries.workout.date
            _entries.value = workoutWithEntries.entries
        }
    }

    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineWithEntriesById(routineId)
            for (entryWithActivity in routine.entries) {
               val activity = entryWithActivity.activity
               val entry = entryWithActivity.routineEntry

               val workoutEntry = WorkoutEntry(entry.activityId,workoutId,entry.description,date.value!!)
               _entries.value = entries.value!!.plus(WorkoutEntryWithActivity(workoutEntry,activity))
           }
        }
    }

    fun insertWorkoutAndClose() {
        viewModelScope.launch {
            val workout = Workout(date.value!!,workoutId)
            workoutRepository.insertWorkout(workout)

            for (entryWithActivity in entries.value!!) {
                workoutRepository.insertEntry(entryWithActivity.workoutEntry)
            }

            close()
        }
    }

    fun deleteWorkoutAndClose() {
        viewModelScope.launch {
            val workout = Workout(date.value!!, workoutId)
            workoutRepository.deleteWorkout(workout)
            // entries don't need to be deleted here because workout is a foreign key in WorkoutEntry
            close()
        }
    }

    fun updateWorkoutAndClose() {
        viewModelScope.launch {
            val workout = Workout(date.value!!, workoutId)
            workoutRepository.updateWorkout(workout)

            val prevEntries = workoutRepository.getEntriesByWorkoutId(workoutId)

            val entriesToDelete = prevEntries.dropWhile {
                // drop if this entry is in entries
                for (entryWithActivity in entries.value!!) {
                    if (entryWithActivity.workoutEntry.id == it.id) {
                        return@dropWhile true
                    }
                }
                return@dropWhile false
            }

            for (entry in entriesToDelete) {
                workoutRepository.deleteEntry(entry)
            }

            for (entryWithActivity in entries.value!!) {
                // don't use update, use insert because onConflict = Replace. So if already exists
                // will be updated; if doesn't exist will be inserted.
                workoutRepository.insertEntry(entryWithActivity.workoutEntry)
            }

            close()
        }
    }

    override fun chooseActivity(activity: Activity) {
        val entry = WorkoutEntry(activity.id,workoutId,"", date.value!!)
        val entryWithActivity = WorkoutEntryWithActivity(entry, activity)
        _entries.value = _entries.value!!.plusElement(entryWithActivity)
    }

    override fun unchooseActivity(activity: Activity) {
        // drop entries with the same activity (for some reason dropWhile() will not work)
        _entries.value = _entries.value!!.filter {
            it.activity.id != activity.id
        }
    }

    override fun activityIsInEntries(activity: Activity): Boolean {
        for (entryWithActivity in entries.value!!) {
            if (entryWithActivity.activity == activity) {
                return true
            }
        }
        return false
    }

    fun navigateToChooseActivityFragment() {
        _navigateToChooseActivityFragment.value = Event(Unit)
    }

    fun close() {
        _close.value = Event(Unit)
    }

}