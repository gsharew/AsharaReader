package my.noveldokusha.ui.screens.main.finder

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldokusha.ui.goToBookChapters
import my.noveldokusha.ui.screens.sourceCatalog.SourceCatalogScreen
import my.noveldokusha.ui.screens.sourceCatalog.SourceCatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinderScreen(
    viewModel: SourceCatalogViewModel     = viewModel(),
    viewModelSourceCatalog:SourceCatalogViewModel
) {


    val context by rememberUpdatedState(newValue = LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )
    val scrollState = rememberScrollState()


    Scaffold(
        content = { innerPadding ->
            SourceCatalogScreen(
                modifier = Modifier.verticalScroll(scrollState),
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
    )
}
