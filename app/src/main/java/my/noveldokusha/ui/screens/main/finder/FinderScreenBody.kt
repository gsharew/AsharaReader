package my.noveldokusha.ui.screens.main.finder

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import my.noveldokusha.repository.CatalogItem
import my.noveldokusha.scraper.DatabaseInterface
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.ui.goToBookChapters
import my.noveldokusha.ui.screens.sourceCatalog.SourceCatalogScreen
import my.noveldokusha.ui.screens.sourceCatalog.SourceCatalogViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun FinderScreenBody(
    innerPadding: PaddingValues,
    databasesList: List<DatabaseInterface>,
    sourcesList: List<CatalogItem>,
    onDatabaseClick: (DatabaseInterface) -> Unit,
    onSourceClick: (SourceInterface.Catalog) -> Unit,
    onSourceSetPinned: (id: String, pinned: Boolean) -> Unit,
    viewModel: SourceCatalogViewModel

) {
    val context by rememberUpdatedState(LocalContext.current)


    SourceCatalogScreen(
        innerPadding=innerPadding,
        state = viewModel.state,
        onSearchTextInputChange = viewModel.state.searchTextInput::value::set,
        onSearchTextInputSubmit = viewModel::onSearchText,
        onSearchCatalogSubmit = viewModel::onSearchCatalog,
        onListLayoutModeChange = viewModel.state.listLayoutMode::value::set,
        onToolbarModeChange = viewModel.state.toolbarMode::value::set,
        onOpenSourceWebPage = ::openSourceWebPage,
        onBookClicked = context::goToBookChapters,
        onBookLongClicked = viewModel::addToLibraryToggle,
        onPressBack = ::onBackPressed
    )

}

fun onBackPressed() {
    Log.e("TAG", "openSourceWebPage: >>>>>", )
}

fun openSourceWebPage() {
    Log.e("TAG", "openSourceWebPage: >>>>>", )
}


//fun  goToWebViewWithUrl(url: String="https://www.novelupdates.com/novelslisting/?sort=7&order=1&status=1") {
//    WebViewActivity.IntentData(this, url = url).let(::startActivity)
//}
//@PreviewThemes
//@Composable
//private fun PreviewView() {
//    val catalogItemsList = previewFixturesCatalogList().mapIndexed { index, it ->
//        CatalogItem(
//            catalog = it,
//            pinned = index % 2 == 0,
//        )
//    }
//
//    InternalTheme {
//        FinderScreenBody(
//            innerPadding = PaddingValues(),
//            databasesList = previewFixturesDatabaseList(),
//            sourcesList = catalogItemsList,
//            onDatabaseClick = {},
//            onSourceClick = {},
//            onSourceSetPinned = { _, _ -> },
//        )
//    }
//}