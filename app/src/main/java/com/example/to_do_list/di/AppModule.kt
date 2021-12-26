package com.example.to_do_list.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope():CoroutineScope
    = CoroutineScope(SupervisorJob())
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope