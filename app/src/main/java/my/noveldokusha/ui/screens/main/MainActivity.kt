package my.noveldokusha.ui.screens.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import my.noveldokusha.R
import my.noveldokusha.ui.BaseActivity
import my.noveldokusha.ui.composeViews.AnimatedTransition
import my.noveldokusha.ui.screens.main.finder.FinderScreen
import my.noveldokusha.ui.screens.main.library.LibraryScreen
import my.noveldokusha.ui.screens.main.settings.SettingsScreen
import my.noveldokusha.ui.screens.sourceCatalog.SourceCatalogViewModel
import my.noveldokusha.ui.theme.Theme

private data class Page(
    @DrawableRes val iconRes: Int,
    @StringRes val stringRes: Int,
)

private val pages = listOf(
    Page(iconRes = R.drawable.ic_baseline_home_24, stringRes = R.string.title_library),
    Page(iconRes = R.drawable.ic_baseline_menu_book_24, stringRes = R.string.title_finder),
    Page(iconRes = R.drawable.ic_twotone_settings_24, stringRes = R.string.title_settings),
)


@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint

open class MainActivity : BaseActivity() {

    private val viewModel by viewModels<SourceCatalogViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var activePageIndex by rememberSaveable { mutableStateOf(0) }

            BackHandler(enabled = activePageIndex != 0) {
                activePageIndex = 0
            }

            Theme(appPreferences = appPreferences) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        AnimatedTransition(targetState = activePageIndex) {
                            when (it) {
                                0 -> LibraryScreen()
                                1 -> FinderScreen(viewModelSourceCatalog=viewModel)
                                2 -> SettingsScreen()
                            }
                        }
                    }
                    NavigationBar {
                        pages.forEachIndexed { pageIndex, page ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(id = page.iconRes),
                                        contentDescription = stringResource(id = page.stringRes)
                                    )
                                },
                                label = { Text(stringResource(id = page.stringRes)) },
                                selected = activePageIndex == pageIndex,
                                onClick = {
                                    activePageIndex = pageIndex
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

