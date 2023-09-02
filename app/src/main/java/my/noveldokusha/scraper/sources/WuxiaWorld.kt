package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.R
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.data.ChapterMetadata
import my.noveldokusha.data.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.PagedList
import my.noveldokusha.network.postRequest
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.LanguageCode
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.utils.add
import my.noveldokusha.utils.addPath
import my.noveldokusha.utils.ifCase
import my.noveldokusha.utils.toDocument
import my.noveldokusha.utils.toUrlBuilder
import my.noveldokusha.utils.toUrlBuilderSafe

class WuxiaWorld(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "wuxia_world"
    override val nameStrId = R.string.source_name_wuxia_world
    override val baseUrl = "https://wuxiaworld.site/"
    override val catalogUrl = "https://wuxiaworld.site/novel/?m_orderby=trending"
    override val iconUrl = "https://wuxiaworld.site/wp-content/uploads/2019/04/favicon-1.ico"
    override val language = LanguageCode.ENGLISH

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("div.summary_image")
                ?.selectFirst("img[src]")
                ?.attr("src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".summary__content.show-more")
                ?.let { TextExtractor.get(it).trim() }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            val url = bookUrl
                .toUrlBuilder()
                ?.addPath("ajax")
                ?.addPath("chapters")

            val request = postRequest(url.toString())
            networkClient.call(request)
                .toDocument()
                .select(".wp-manga-chapter > a[href]")
                .map { ChapterMetadata(title = it.text(), url = it.attr("href")) }
                .reversed()
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = baseUrl
                .toUrlBuilderSafe()
                .addPath("novel")
                .ifCase(page != 1) { addPath("page", page.toString()) }
                .add("m_orderby", "alphabet")

            val doc = networkClient.get(url).toDocument()
            doc.select(".page-item-detail")
                .mapNotNull {
                    val link = it.selectFirst("a[href]") ?: return@mapNotNull null
                    val bookCover = it
                        .selectFirst("img[src]")
                        ?.attr("src") ?: ""

                    BookMetadata(
                        title = link.attr("title"),
                        url = link.attr("href"),
                        coverImageUrl = bookCover
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = doc.selectFirst("div.nav-previous.float-left") == null
                    )
                }
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            if (page != 1)
                return@tryConnect PagedList.createEmpty(index = index)
            val url = baseUrl
                .toUrlBuilderSafe()
                .ifCase(page > 1) { addPath("page", page.toString()) }
                .add(
                    "s" to input,
                    "post_type" to "wp-manga",
                    "op" to "",
                    "author" to "",
                    "artist" to "",
                    "release" to "",
                    "adult" to ""
                )

            val doc = networkClient.get(url).toDocument()
            doc.select(".c-tabs-item__content")
                .mapNotNull {
                    val link = it.selectFirst("a[href]") ?: return@mapNotNull null
                    val bookCover = it.selectFirst("img[src]")
                        ?.attr("src") ?: ""

                    BookMetadata(
                        title = link.attr("title"),
                        url = link.attr("href"),
                        coverImageUrl = bookCover
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = true
                    )
                }
        }
    }
}
