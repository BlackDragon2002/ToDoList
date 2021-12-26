package com.example.to_do_list.di

import android.app.Application
import androidx.room.Room
import com.example.to_do_list.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideDatabase(
        app:Application,
        callBack: TaskDatabase.CallBack
    ):TaskDatabase=Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            "task_database"
        )
            .fallbackToDestructiveMigration()
            .addCallback(callBack)
            .build()

    @Provides
    fun provideTaskDao(
        db:TaskDatabase
    )=db.getDao()


}