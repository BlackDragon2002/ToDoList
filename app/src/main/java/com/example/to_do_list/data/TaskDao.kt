package com.example.to_do_list.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query:String,sortOrder: SortOrder,hideCompleted: Boolean):Flow<List<Task>> =
    when(sortOrder){
        SortOrder.BY_DATE ->getTaskSortedByDate(query,hideCompleted)
        SortOrder.BY_NAME->getTaskSortedByName(query,hideCompleted)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM task_table WHERE completed =1")
    suspend fun deleteCompletedTask()

    @Query("SELECT * FROM task_table WHERE (completed!=:hideCompleted OR completed=0) AND name LIKE '%'|| :searchQuery||'%' ORDER BY important DESC , name")
    fun getTaskSortedByName(searchQuery:String, hideCompleted:Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed!=:hideCompleted OR completed=0) AND name LIKE '%'|| :searchQuery||'%' ORDER BY important DESC , created")
    fun getTaskSortedByDate(searchQuery:String, hideCompleted:Boolean): Flow<List<Task>>
}