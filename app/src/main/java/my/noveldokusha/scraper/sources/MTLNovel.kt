package my.noveldokusha.scraper.sources

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.R
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.data.ChapterMetadata
import my.noveldokusha.data.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.PagedList
import my.noveldokusha.network.getRequest
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.LanguageCode
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.utils.add
import my.noveldokusha.utils.addPath
import my.noveldokusha.utils.toDocument
import my.noveldokusha.utils.toUrlBuilderSafe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MTLNovel(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "mtlnovel"
    override val nameStrId = R.string.source_name_mtlnovel
    override val baseUrl = "https://www.mtlnovel.com/"
    override val catalogUrl = "https://www.mtlnovel.com/alltime-rank/"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterTitle(doc: Document): String? = null

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".par.fontsize-16")!!.let { TextExtractor.get(it) }
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("amp-img.main-tmb[src]")
                ?.attr("src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val text = networkClient.get(bookUrl).toDocument()
                .selectFirst(".desc") ?: return@tryConnect null
            val node = text.apply {
                select("h2").remove()
                select("p.descr").remove()
            }
            TextExtractor.get(node).trim()
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            // Needs to add "/" at the end
            val url = bookUrl
                .toUrlBuilderSafe()
                .addPath("chapter-list")
                .toString() + "/"

            networkClient.get(url)
                .toDocument()
                .select("a.ch-link[href]")
                .map {
                    ChapterMetadata(
                        title = it.text(),
                        url = it.attr("href"),
                    )
                }
                .reversed()
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = catalogUrl.toUrlBuilderSafe().apply {
                if (page != 1) addPath("page", "$page")
            }
            val doc = networkClient.get(url).toDocument()
            doc.select(".box.wide")
                .mapNotNull {
                    val link = it.selectFirst("a.list-title[href]") ?: return@mapNotNull null
                    BookMetadata(
                        title = link.attr("aria-label"),
                        url = link.attr("href"),
                        coverImageUrl = it.selectFirst("amp-img[src]")?.attr("src") ?: ""
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = when (val nav = doc.selectFirst("div#pagination")) {
                            null -> true
                            else -> nav.children().last()?.`is`("span") ?: true
                        }
                    )
                }
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookMetadata>> = withContext(Dispatchers.Default) {
        tryConnect {
            if (input.isBlank() || index > 0)
                return@tryConnect PagedList.createEmpty(index = index)

            val url = """https://www.mtlnovel.com/wp-admin/admin-ajax.php"""
                .toUrlBuilderSafe()
                .add("action", "autosuggest")
                .add("q", input)
                .add("__amp_source_origin", "https://www.mtlnovel.com")
                .toString()

            val request = getRequest(url)
            val json = networkClient.call(request)
                .body
                .string()

            JsonParser
                .parseString(json)
                .asJsonObject["items"]
                .asJsonArray[0]
                .asJsonObject["results"]
                .asJsonArray
                .map { it.asJsonObject }
                .map {
                    BookMetadata(
                        title = Jsoup.parse(it["title"].asString).text(),
                        url = it["permalink"].asString,
                        coverImageUrl = it["thumbnail"].asString
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
