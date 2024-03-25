package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag

data class BookmarkActions(
    val onClickEdit: (Bookmark) -> Unit,
    val onClickDelete: (Bookmark) -> Unit,
    val onClickShare: (Bookmark) -> Unit,
    val onClickCategory: (Tag) -> Unit,
    val onClickBookmark: (Bookmark) -> Unit,
    val onClickEpub: (Bookmark) -> Unit,
    val onClickSync: (Bookmark) -> Unit
)

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions,
    viewType: BookmarkViewType
) {
    Box(modifier = Modifier
        .padding(horizontal = 6.dp,)
        .padding(bottom = if (viewType == BookmarkViewType.FULL) 16.dp else 6.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { actions.onClickBookmark(bookmark) },
            elevation = if (viewType == BookmarkViewType.FULL) {
                CardDefaults.cardElevation(defaultElevation = 4.dp)
            } else {
                CardDefaults.cardElevation(defaultElevation = 0.dp)
            },            shape = RoundedCornerShape(8.dp)
        ) {
            val imageUrl =
                "${serverURL.removeTrailingSlash()}${bookmark.imageURL}?lastUpdated=${bookmark.modified}"
            when (viewType) {
                BookmarkViewType.FULL -> FullBookmarkView(
                    bookmark,
                    imageUrl,
                    xSessionId,
                    isLegacyApi,
                    token,
                    actions
                )

                BookmarkViewType.SMALL -> SmallBookmarkView(
                    bookmark,
                    imageUrl,
                    xSessionId,
                    isLegacyApi,
                    token,
                    actions
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewPost() {
    MaterialTheme() {
        val actions = BookmarkActions(
            onClickEdit = {},
            onClickDelete = {},
            onClickShare = {},
            onClickCategory = {},
            onClickBookmark = {},
            onClickEpub = {},
            onClickSync = {}
        )
        val bookmark = Bookmark(
            id = -1,
            url = "url",
            title = "Bookmark title",
            excerpt = "Bookmark content",
            author = "",
            public = 1,
            modified = "",
            imageURL = "",
            hasContent = true,
            hasArchive = true,
            hasEbook = false,
            createArchive = true,
            createEbook = true,
            tags = listOf(Tag("tag1"), Tag("tag2")),
        )
        Column {
            BookmarkItem(
                bookmark = bookmark,
                serverURL = "",
                xSessionId = "",
                isLegacyApi = false,
                token = "",
                actions = actions,
                viewType = BookmarkViewType.FULL
            )
            BookmarkItem(
                bookmark = bookmark,
                serverURL = "",
                xSessionId = "",
                isLegacyApi = false,
                token = "",
                actions = actions,
                viewType = BookmarkViewType.SMALL
            )
        }

    }
}