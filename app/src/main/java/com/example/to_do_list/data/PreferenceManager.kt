package com.example.to_do_list.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val Context.createDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preference")
    private val dataStore = context.createDataStore

    val preferences=dataStore.data
        .catch {exception ->
            if(exception is IOException){
                Log.e("PreferenceManager","Error exception" ,exception)
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map {preferences->
            val sortedOrder=SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER]?:SortOrder.BY_DATE.name
            )
            val hideCompleted=preferences[PreferencesKeys.HIDE_COMPLETED] ?:false
            FilterPreferences(sortedOrder, hideCompleted)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.SORT_ORDER]=sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.HIDE_COMPLETED]=hideCompleted
        }
    }

    private object PreferencesKeys{
        val SORT_ORDER= stringPreferencesKey("sort-order")
        val HIDE_COMPLETED=booleanPreferencesKey("hide-completed")
    }
    data class FilterPreferences(
        val sortOrder: SortOrder,
        val hideCompleted:Boolean
    )

}
enum class SortOrder {
    BY_NAME,
    BY_DATE
}
