package my.noveldokusha.scraper

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.repository.AppFileResolver
import my.noveldokusha.scraper.databases.BakaUpdates
import my.noveldokusha.scraper.sources.NovelUpdates
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scraper @Inject constructor(
    networkClient: NetworkClient,
    @ApplicationContext appContext: Context,
    localSourcesDirectories: LocalSourcesDirectories,
    appFileResolver: AppFileResolver,
) {
    val databasesList = setOf<DatabaseInterface>(
         BakaUpdates(networkClient)
    )

    val sourcesList = setOf<SourceInterface>(
         NovelUpdates(networkClient),
    )

    val sourcesCatalogsList = sourcesList.filterIsInstance<SourceInterface.Catalog>()
    val sourcesCatalogsLanguagesList = sourcesCatalogsList.mapNotNull { it.language }.toSet()

    private fun String.isCompatibleWithBaseUrl(baseUrl: String): Boolean {
        val normalizedUrl = if (this.endsWith("/")) this else "$this/"
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return normalizedUrl.startsWith(normalizedBaseUrl)
    }

    fun getCompatibleSource(url: String): SourceInterface? =
        sourcesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }

    fun getCompatibleSourceCatalog(url: String): SourceInterface.Catalog? =
        sourcesCatalogsList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }

    fun getCompatibleDatabase(url: String): DatabaseInterface? =
        databasesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }
}
