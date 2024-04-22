package com.desarrollodroide.pagekeeper.ui.feed

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.PullRefreshIndicator
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.pullRefresh
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.rememberPullRefreshState
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    actions: FeedActions,
    bookmarks: List<Bookmark>,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    uniqueCategories: MutableState<List<Tag>>,
    isCategoriesVisible: Boolean,
    isSearchBarVisible: MutableState<Boolean>,
    selectedTags: MutableState<List<Tag>>,
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val filteredBookmarks = if (selectedTags.value.isEmpty()) {
        bookmarks
    } else {
        bookmarks.filter { bookmark ->
            bookmark.tags.any { it in selectedTags.value }
        }
    }
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    fun refreshBookmarks() = refreshCoroutineScope.launch {
        actions.onRefreshFeed.invoke()
        isRefreshing = true
        delay(1500)
        isRefreshing = false
    }

    val refreshState = rememberPullRefreshState(isRefreshing, ::refreshBookmarks)

    Box(Modifier.fillMaxHeight()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp)
                .pullRefresh(state = refreshState)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Categories(
                    showCategories = isCategoriesVisible,
                    uniqueCategories = uniqueCategories,
                    selectedTags = selectedTags,
                    onCategoriesSelectedChanged = actions.onCategoriesSelectedChanged
                )
            }
            itemsIndexed(filteredBookmarks) { index, bookmark ->
                Column {
                    BookmarkItem(
                        bookmark = bookmark,
                        serverURL = serverURL,
                        xSessionId = xSessionId,
                        token = token,
                        isLegacyApi = isLegacyApi,
                        viewType = viewType,
                        actions = BookmarkActions(
                            onClickEdit = { actions.onEditBookmark(bookmark) },
                            onClickDelete = { actions.onDeleteBookmark(bookmark) },
                            onClickShare = { actions.onShareBookmark(bookmark) },
                            onClickBookmark = { actions.onBookmarkSelect(bookmark) },
                            onClickEpub = { actions.onBookmarkEpub(bookmark) },
                            onClickSync = { actions.onClickSync(bookmark) },
                            onClickCategory = { category ->
                                bookmark.tags.firstOrNull() { it.name == category.name }?.apply {
                                    if (selectedTags.value.contains(category)) {
                                        selectedTags.value = selectedTags.value - category
                                    } else {
                                        selectedTags.value = selectedTags.value + category
                                    }
                                }
                            }),
                    )
                    if (index < filteredBookmarks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .height(1.dp)
                                .padding(horizontal = 6.dp,),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            refreshing = isRefreshing,
            state = refreshState,
        )
    }

    if (isSearchBarVisible.value) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            shape = BottomSheetDefaults.ExpandedShape,
            onDismissRequest = {
                isSearchBarVisible.value = false
            },
            sheetState = sheetState,
            dragHandle = null
        ) {
            SearchBar(
                bookmarks = bookmarks,
                onBookmarkClick =  actions.onBookmarkSelect,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        isSearchBarVisible.value = false
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchBar(
    onBookmarkClick: (Bookmark) -> Unit,
    onDismiss: () -> Unit,
    bookmarks: List<Bookmark>,
) {
    val searchText = rememberSaveable { mutableStateOf("") }
    val isActive = rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    val filteredBookmarks =
        bookmarks.filter { it.title.contains(searchText.value, ignoreCase = true) }
    Box(Modifier
        .fillMaxSize()) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter),
            query = searchText.value,
            onQueryChange = { searchText.value = it },
            onSearch = {
                Toast.makeText(context, "Select bookmark from list", Toast.LENGTH_SHORT).show()
            },
            active = isActive.value,
            onActiveChange = { isActive.value = it },
            placeholder = { Text("Search...") },
            leadingIcon = {
                IconButton(onClick = {
                    onDismiss()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
                          },
            trailingIcon = {
                Row() {
                    Box(modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            searchText.value = ""
                        }) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                    }
                }
            },
        ) {
            BookmarkSuggestions(
                bookmarks = filteredBookmarks,
                onClickSuggestion = onBookmarkClick
            )
        }
    }
}

@Composable
private fun BookmarkSuggestions(
    bookmarks: List<Bookmark>,
    onClickSuggestion: (Bookmark) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(bookmarks) { bookmark ->
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .clickable {
                        onClickSuggestion(bookmark)
                    }
                    .background(Color.Transparent),
                headlineContent = {
                    Text(
                        text = bookmark.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = {
                    Text(
                        overflow = TextOverflow.Ellipsis,
                        text = bookmark.excerpt,
                        maxLines = 3,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingContent = { Icon(Icons.Rounded.Bookmark, contentDescription = null) },
            )
        }
    }
}
