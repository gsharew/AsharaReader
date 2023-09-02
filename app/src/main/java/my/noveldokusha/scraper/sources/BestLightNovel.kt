package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.R
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.data.ChapterMetadata
import my.noveldokusha.data.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.PagedList
import my.noveldokusha.network.tryConnect
import my.noveldokusha.network.tryFlatConnect
import my.noveldokusha.scraper.LanguageCode
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.utils.add
import my.noveldokusha.utils.addPath
import my.noveldokusha.utils.ifCase
import my.noveldokusha.utils.toDocument
import my.noveldokusha.utils.toUrlBuilderSafe
import org.jsoup.nodes.Document

class BestLightNovel(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "best_light_novel"
    override val nameStrId = R.string.source_name_bestlightnovel
    override val baseUrl = "https://bestlightnovel.com/"
    override val catalogUrl = "https://bestlightnovel.com/novel_list"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterTitle(doc: Document): String? = null

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#vung_doc")!!.let(TextExtractor::get)
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".info_image > img[src]")
                ?.attr("src")
        }
    }


    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("#noidungm")
                ?.let {
                    it.select("h2").remove()
                    TextExtractor.get(it)
                }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterMetadata>> =
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select("div.chapter-list a[href]")
                .map { ChapterMetadata(title = it.text(), url = it.attr("href")) }
                .reversed()
        }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookMetadata>> {
        val page = index + 1
        return tryFlatConnect {

            val url = catalogUrl
                .toUrlBuilderSafe()
                .ifCase(page != 1) {
                    add("type", "newest")
                    add("category", "all")
                    add("state", "all")
                    add("page", page)
                }
            parseToBooks(networkClient.get(url).toDocument(), index)
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookMetadata>> {
        if (input.isBlank()) return Response.Success(PagedList.createEmpty(index = index))

        val page = index + 1
        return tryFlatConnect {
            val url = baseUrl
                .toUrlBuilderSafe()
                .addPath("search_novels", input.replace(" ", "_"))
                .ifCase(page != 1) { add("page", page) }
            parseToBooks(networkClient.get(url).toDocument(), index)
        }
    }

    private fun parseToBooks(doc: Document, index: Int): Response<PagedList<BookMetadata>> {

        return doc.select(".update_item.list_category")
            .mapNotNull {
                val link = it.selectFirst("a[href]") ?: return@mapNotNull null
                val bookCover = it.selectFirst("img[src]")?.attr("src") ?: ""
                BookMetadata(
                    title = link.attr("title"),
                    url = link.attr("href"),
                    coverImageUrl = bookCover
                )
            }
            .let {
                Response.Success(
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = when (val nav = doc.selectFirst("div.phan-trang")) {
                            null -> true
                            else -> nav.children().takeLast(2).first()?.`is`(".pageselect")
                                ?: true
                        }
                    )
                )
            }
    }
}
