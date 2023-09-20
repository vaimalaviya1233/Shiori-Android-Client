package com.shiori.androidclient.ui.bookmarkeditor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.components.error
import com.shiori.androidclient.ui.components.isLoading
import com.shiori.androidclient.ui.components.success
import com.shiori.common.result.Result
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.data.local.room.dao.BookmarksDao
import com.shiori.domain.usecase.AddBookmarkUseCase
import com.shiori.domain.usecase.EditBookmarkUseCase
import com.shiori.model.Bookmark
import com.shiori.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(
    bookmarkDatabase: BookmarksDao,
    private val bookmarkAdditionUseCase: AddBookmarkUseCase,
    private val editBookmarkUseCase: EditBookmarkUseCase,
    private val userPreferences: SettingsPreferenceDataSource,

    ) : ViewModel() {

    var backendUrl = ""

    private val _bookmarkUiState = MutableStateFlow(UiState<Bookmark>(idle = true))
    val bookmarkUiState = _bookmarkUiState.asStateFlow()

    init {
        viewModelScope.launch {
            backendUrl = userPreferences.getUrl()
        }
    }

    val availableTags: StateFlow<List<Tag>> = bookmarkDatabase.getAll()
        .map { bookmarks ->
            bookmarks.flatMap { it.tags }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )


    fun saveBookmark(url: String, tags: List<Tag>) = viewModelScope.launch {
        viewModelScope.launch {
            bookmarkAdditionUseCase.invoke(
                serverUrl = backendUrl,
                xSession = userPreferences.getSession(),
                bookmark = Bookmark(url, tags)
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("Add BookmarkViewModel", "Error")
                            _bookmarkUiState.error(errorMessage = result.error?.message ?: "" )
                        }

                        is Result.Loading -> {
                            Log.v("Add BookmarkViewModel", "Loading")
                            _bookmarkUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("Add BookmarkViewModel", "Success")
                            _bookmarkUiState.success(result.data)
                        }
                    }
                }
        }
    }

    fun editBookmark(bookmark: Bookmark) = viewModelScope.launch {
        viewModelScope.launch {
            editBookmarkUseCase.invoke(
                serverUrl = backendUrl,
                xSession = userPreferences.getSession(),
                bookmark = bookmark
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            val errorMessage = result.error?.message ?: result.error?.throwable?.message?: "Unknown error"
                            Log.v("Edit BookmarkViewModel", errorMessage)
                            _bookmarkUiState.error(errorMessage = errorMessage)
                        }

                        is Result.Loading -> {
                            Log.v("Edit BookmarkViewModel", "Loading")
                            _bookmarkUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("Edit BookmarkViewModel", "Success")
                            _bookmarkUiState.success(result.data)
                        }
                    }
                }
        }
    }
}