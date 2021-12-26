package com.example.to_do_list.ui.task

import androidx.lifecycle.*
import com.example.to_do_list.data.PreferencesManager
import com.example.to_do_list.data.SortOrder
import com.example.to_do_list.data.Task
import com.example.to_do_list.data.TaskDao
import com.example.to_do_list.utils.Constants.Companion.ADD_TASK_RESULT_OK
import com.example.to_do_list.utils.Constants.Companion.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel
@Inject
constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val state:SavedStateHandle
):ViewModel(),LifecycleObserver {

    val searchQuery= state.getLiveData("Search Query","")

    var sortOrder= SortOrder.BY_DATE

    val preferencesFlow = preferencesManager.preferences

    private val taskEventChannel= Channel<TaskEvent>()

    val taskEvent=taskEventChannel.receiveAsFlow()

    @ExperimentalCoroutinesApi
    private val tasksFlow= combine(
        searchQuery.asFlow(),
        preferencesFlow
    ){ query,filterPreferences->
        Pair(query,filterPreferences)
    }.flatMapLatest {( query,filterPreferences)->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    @ExperimentalCoroutinesApi
    val tasks=tasksFlow.asLiveData()
    
    fun onSortOrderSelected(sortOrder: SortOrder)=viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompleted(hideCompleted:Boolean)=viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task)=viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) =viewModelScope.launch {
        taskDao.updateTask(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) =viewModelScope.launch {
        taskDao.deleteTask(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoClicked(task: Task) =viewModelScope.launch{
        taskDao.insertTask(task)
    }

    fun onAddNewTaskClick()=viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when(result){
            ADD_TASK_RESULT_OK->showTaskSavedConfirmationMessage("Task Added")
            EDIT_TASK_RESULT_OK->showTaskSavedConfirmationMessage("Task Updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String)=viewModelScope.launch {

    }

    fun onDeleteAllCompletedTask() =viewModelScope.launch{
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TaskEvent{
        object NavigateToAddTaskScreen: TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val message:String): TaskEvent()
        object NavigateToDeleteAllCompletedScreen:TaskEvent()
    }
}