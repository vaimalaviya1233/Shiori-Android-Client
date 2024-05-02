package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
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
    isCategoriesVisible: MutableState<Boolean>,
    isSearchBarVisible: MutableState<Boolean>,
    selectedTags: MutableState<List<Tag>>,
    bookmarksPagingItems: LazyPagingItems<Bookmark>,
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
//            item {
//                Categories(
//                    showCategories = isCategoriesVisible,
//                    uniqueCategories = uniqueCategories,
//                    selectedTags = selectedTags,
//                    onCategoriesSelectedChanged = actions.onCategoriesSelectedChanged
//                )
//            }

            item { Spacer(modifier = Modifier.padding(4.dp)) }
            items(bookmarksPagingItems.itemCount) { index ->
                val bookmark = bookmarksPagingItems[index]
                if (bookmark != null) {
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
                    if (index < bookmarksPagingItems.itemCount) {
                        HorizontalDivider(
                            modifier = Modifier
                                .height(1.dp)
                                .padding(horizontal = 6.dp,),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
            bookmarksPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { PageLoader(modifier = Modifier.fillParentMaxSize()) }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val error = loadState.refresh as LoadState.Error
                        if (error.error.localizedMessage == SESSION_HAS_BEEN_EXPIRED) {
                            actions.goToLogin()
                        } else {
                            item {
                                ErrorMessage(
                                    modifier = Modifier.fillParentMaxSize(),
                                    message = error.error.localizedMessage ?: "Unknown error",
                                    onClickRetry = { retry() })
                            }
                        }
                    }
                    loadState.append is LoadState.Loading -> {
                        item { LoadingNextPageItem(modifier = Modifier) }
                    }
                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item {
                            ErrorMessage(
                                modifier = Modifier,
                                message = error.error.localizedMessage ?: "Unknown error",
                                onClickRetry = { retry() })
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(4.dp)) }
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

    if (isCategoriesVisible.value) {
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
fun BookmarkSuggestions(
    bookmarks: LazyPagingItems<Bookmark>,
    onClickSuggestion: (Bookmark) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(bookmarks.itemCount) { index ->
            val bookmark = bookmarks[index]
            if (bookmark != null) {
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
}
