package com.example.to_do_list.ui.addEditTask

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do_list.data.Task
import com.example.to_do_list.data.TaskDao
import com.example.to_do_list.utils.Constants.Companion.ADD_TASK_RESULT_OK
import com.example.to_do_list.utils.Constants.Companion.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel
@Inject
constructor(
    private val taskDao: TaskDao,
    private val state:SavedStateHandle
):ViewModel(),LifecycleObserver{

    val task=state.get<Task>("task")

    var taskName=state.get<String>("taskName")?:task?.name?:""
    set(value) {
        field=value
        state.set("taskName",value)
    }
    var taskImportance=state.get<Boolean>("taskName")?:task?.important?:false
    set(value) {
        field=value
        state.set("taskImportance",value)
    }

    private val addEditTaskEventChannel= Channel<AddEditEvent>()
    val addEditTaskEvent=addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask=Task(name=taskName, important = taskImportance)
            createTask(newTask)
        }

    }

    private fun showInvalidMessage(invalidMessage: String) =viewModelScope.launch{
        addEditTaskEventChannel.send(AddEditEvent.ShowInvalidMessage(invalidMessage))

    }

    private fun createTask(task: Task) =viewModelScope.launch{
        taskDao.insertTask(task)
        addEditTaskEventChannel.send(AddEditEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task)=viewModelScope.launch {
        taskDao.updateTask(task)
        addEditTaskEventChannel.send(AddEditEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    sealed class AddEditEvent {
        data class ShowInvalidMessage(val msg:String): AddEditEvent()
        data class NavigateBackWithResult(val result:Int): AddEditEvent()
    }
}
