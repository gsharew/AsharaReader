package my.noveldokusha.ui.screens.sourceCatalog

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import my.noveldokusha.AppPreferences
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.network.PagedListIteratorState
import my.noveldokusha.ui.composeViews.ToolbarMode

data class SourceCatalogScreenState(
    val sourceCatalogNameStrId: State<Int>,
    val searchTextInput: MutableState<String>,
    val fetchIterator: PagedListIteratorState<BookMetadata>,
    val toolbarMode: MutableState<ToolbarMode>,
    val listLayoutMode: MutableState<AppPreferences.LIST_LAYOUT_MODE>,
)