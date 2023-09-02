package my.noveldokusha.ui.screens.globalSourceSearch

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import my.noveldokusha.R
import my.noveldokusha.composableActions.ListLoadWatcher
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.network.IteratorState
import my.noveldokusha.repository.CatalogItem
import my.noveldokusha.repository.rememberResolvedBookImagePath
import my.noveldokusha.ui.bounceOnPressed
import my.noveldokusha.ui.composeViews.BookImageButtonView
import my.noveldokusha.ui.composeViews.BookTitlePosition
import my.noveldokusha.ui.previewFixtures.previewFixturesCatalogList
import my.noveldokusha.ui.theme.ColorAccent
import my.noveldokusha.ui.theme.InternalTheme
import my.noveldokusha.ui.theme.PreviewThemes

@Composable
fun GlobalSourceSearchScreenBody(
    listSources: List<SourceResults>,
    contentPadding: PaddingValues,
    onBookClick: (book: BookMetadata) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(contentPadding),
        contentPadding = PaddingValues(top = 8.dp, bottom = 240.dp)
    ) {
        items(listSources) { entry ->
            Text(
                text = stringResource(id = entry.source.catalog.nameStrId),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .padding(vertical = 2.dp)
            )
            SourceListView(
                list = entry.fetchIterator.list,
                loadState = entry.fetchIterator.state,
                error = entry.fetchIterator.error,
                onBookClick = onBookClick,
                onLoadNext = { entry.fetchIterator.fetchNext() },
            )
        }
    }
}

@Composable
private fun SourceListView(
    list: List<BookMetadata>,
    loadState: IteratorState,
    error: String?,
    onBookClick: (book: BookMetadata) -> Unit,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()

    ListLoadWatcher(listState = state, loadState = loadState, onLoadNext = onLoadNext)

    LazyRow(
        state = state,
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 30.dp,
        ),
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth(),
    ) {
        items(list) {
            val interactionSource = remember { MutableInteractionSource() }
            BookImageButtonView(
                title = it.title,
                coverImageModel = rememberResolvedBookImagePath(
                    bookUrl = it.url,
                    imagePath = it.coverImageUrl
                ),
                onClick = { onBookClick(it) },
                onLongClick = { },
                modifier = Modifier
                    .width(130.dp)
                    .bounceOnPressed(interactionSource),
                bookTitlePosition = BookTitlePosition.Outside,
                interactionSource = interactionSource
            )
        }

        item {
            fun Modifier.topPadding() = padding(top = (130 / 1.45f).dp - 8.dp)

            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                when (loadState) {
                    IteratorState.LOADING -> CircularProgressIndicator(
                        color = ColorAccent,
                        modifier = Modifier.padding(36.dp)
                    )

                    IteratorState.CONSUMED -> when {
                        error != null -> Text(
                            text = stringResource(R.string.error_loading),
                            color = MaterialTheme.colorScheme.error,
                            modifier = if (list.isEmpty()) Modifier else Modifier.topPadding()
                        )

                        list.isEmpty() -> Text(
                            text = stringResource(R.string.no_results_found),
                            color = ColorAccent,
                        )

                        else -> Text(
                            text = stringResource(R.string.no_more_results),
                            color = ColorAccent,
                            modifier = Modifier.topPadding()
                        )
                    }

                    IteratorState.IDLE -> {}
                }
            }
        }
    }
}

@PreviewThemes
@Composable
private fun PreviewView() {


    val list = previewFixturesCatalogList().mapIndexed { index, source ->
        val (catalog, books) = CatalogItem(
            catalog = source,
            pinned = false
        ) to (0..5).map { BookMetadata(title = "Book $it", url = "") }

        val sr = SourceResults(catalog, "", rememberCoroutineScope())
        when (index) {
            0 -> {
                sr.fetchIterator.state = IteratorState.CONSUMED
            }

            1 -> {
                sr.fetchIterator.list.addAll(books.take(2))
                sr.fetchIterator.state = IteratorState.CONSUMED
            }

            2 -> {
                sr.fetchIterator.error = "Error here"
            }

            else -> {
                sr.fetchIterator.list.addAll(books.take(2))
                sr.fetchIterator.state = IteratorState.CONSUMED
                sr.fetchIterator.error = "Error here"
            }
        }
        sr
    }

    InternalTheme {
        GlobalSourceSearchScreenBody(
            listSources = list,
            onBookClick = {},
            contentPadding = PaddingValues(),
        )
    }
}
