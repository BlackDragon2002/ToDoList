package com.example.to_do_list.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.to_do_list.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Task::class],
    version = 1
)
abstract class TaskDatabase:RoomDatabase() {

    abstract fun getDao(): TaskDao

    class CallBack
    @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope
        private val applicationScope: CoroutineScope
    ):RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao=database.get().getDao()

            applicationScope.launch {
                dao.insertTask(Task("Wash the dishes"))
                dao.insertTask(Task("Do the laundry"))
                dao.insertTask(Task("Buy groceries", important = true))
                dao.insertTask(Task("Prepare food", completed = true))
                dao.insertTask(Task("Call mom"))
                dao.insertTask(Task("Visit grandma", completed = true))
                dao.insertTask(Task("Repair my bike"))
                dao.insertTask(Task("Call Elon Musk"))
            }
        }
    }

}