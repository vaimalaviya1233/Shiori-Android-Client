package com.shiori.androidclient.ui.bookmarkeditor

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.shiori.androidclient.ui.components.ConfirmDialog
import com.shiori.androidclient.ui.components.InfiniteProgressDialog
import com.shiori.androidclient.ui.components.SimpleDialog
import com.shiori.model.Bookmark
import com.shiori.model.Tag
import org.koin.androidx.compose.get

@Composable
fun BookmarkEditorScreen(
    title: String,
    bookmarkEditorType: BookmarkEditorType,
    bookmark: Bookmark,
    onBackClick: () -> Unit,
    updateBookmark: (Bookmark) -> Unit
) {
    val viewModel = get<BookmarkViewModel>()
    val newTag = remember { mutableStateOf("") }
    val availableTags = viewModel.availableTags.collectAsState()
    val bookmarkUiState = viewModel.bookmarkUiState.collectAsState().value
    BackHandler {
        onBackClick()
    }
    if (bookmarkUiState.isLoading) {
        Log.v("BookmarkEditorScreen", "isLoading")
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarkUiState.error.isNullOrEmpty()) {
        Log.v("BookmarkEditorScreen", "Error")
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarkUiState.error?:"",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                //viewModel.clearError()
            }
        )
    } else {
        Log.v("BookmarkEditorScreen", "Success")
    }


    val assignedTags: MutableState<List<Tag>> = remember { mutableStateOf(bookmark.tags ?: emptyList()) }

    BookmarkEditorView(
            title = title,
            bookmarkEditorType = bookmarkEditorType,
            newTag = newTag,
            assignedTags = assignedTags,
            availableTags = availableTags,
            saveBookmark = {
                when (bookmarkEditorType) {
                    BookmarkEditorType.ADD -> {
                        viewModel.saveBookmark(bookmark.url, assignedTags.value)
                    }
                    BookmarkEditorType.EDIT -> {
                        viewModel.editBookmark(bookmark.copy (tags = assignedTags.value))
                    }
                }
            },
            onBackClick = onBackClick,
        )
    //}

    if (bookmarkUiState.data != null) {
        Log.v("BookmarkEditorScreen", "Success")
        SimpleDialog(
            title = "Success",
            content = "Bookmark successfully saved!",
            confirmButtonText = "Ok",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = { updateBookmark(bookmarkUiState.data) }
        )
    }
}

