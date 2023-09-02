package my.noveldokusha.ui.screens.sourceCatalog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import my.noveldokusha.ui.BaseActivity
import my.noveldokusha.ui.goToBookChapters
import my.noveldokusha.ui.goToWebViewWithUrl
import my.noveldokusha.ui.theme.Theme
import my.noveldokusha.utils.Extra_String


@AndroidEntryPoint
class SourceCatalogActivity : BaseActivity() {
    class IntentData : Intent, SourceCatalogStateBundle {
        override var sourceBaseUrl by Extra_String()

        constructor(intent: Intent) : super(intent)
        constructor(ctx: Context, sourceBaseUrl: String) : super(
            ctx,
            SourceCatalogActivity::class.java
        ) {
            this.sourceBaseUrl = sourceBaseUrl
        }
    }

    private val viewModel by viewModels<SourceCatalogViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context by rememberUpdatedState(LocalContext.current)

            Theme(appPreferences = appPreferences) {
                SourceCatalogScreen(
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
        }
    }

    private fun openSourceWebPage() = goToWebViewWithUrl(viewModel.sourceBaseUrl)
}
