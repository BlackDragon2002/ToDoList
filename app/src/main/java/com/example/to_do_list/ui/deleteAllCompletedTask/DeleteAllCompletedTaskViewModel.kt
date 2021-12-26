package com.example.to_do_list.ui.deleteAllCompletedTask

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.example.to_do_list.data.TaskDao
import com.example.to_do_list.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedTaskViewModel
@Inject
constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
):ViewModel(),LifecycleObserver{
    fun onConfirmClick()=applicationScope.launch {
        taskDao.deleteCompletedTask()
    }
}