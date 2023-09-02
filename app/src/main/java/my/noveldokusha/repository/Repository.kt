package my.noveldokusha.repository

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import my.noveldokusha.data.Response
import my.noveldokusha.data.database.AppDatabase
import my.noveldokusha.data.database.tables.Book
import my.noveldokusha.data.database.tables.Chapter
import my.noveldokusha.isContentUri
import my.noveldokusha.tools.epub.epubImporter
import my.noveldokusha.tools.epub.epubParser
import my.noveldokusha.utils.tryAsResponse
import javax.inject.Inject

class Repository @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context,
    val name: String,
    val libraryBooks: LibraryBooksRepository,
    val bookChapters: BookChaptersRepository,
    val chapterBody: ChapterBodyRepository,
    private val appFileResolver: AppFileResolver,
) {
    val settings = Settings()
    val eventDataRestored = MutableSharedFlow<Unit>()

    suspend fun toggleBookmark(bookUrl: String, bookTitle: String): Boolean {
        val realUrl = appFileResolver.getLocalIfContentType(bookUrl, bookFolderName = bookTitle)
        return if (bookUrl.isContentUri && libraryBooks.get(realUrl) == null) {
            importEpubFromContentUri(
                contentUri = bookUrl,
                bookTitle = bookTitle,
                addToLibrary = true
            ) is Response.Success
        } else {
            libraryBooks.toggleBookmark(bookUrl = realUrl, bookTitle = bookTitle)
        }
    }

    suspend fun importEpubFromContentUri(
        contentUri: String,
        bookTitle: String,
        addToLibrary: Boolean = false
    ) = tryAsResponse {
        val inputStream = context.contentResolver.openInputStream(contentUri.toUri())
            ?: return@tryAsResponse
        val epub = inputStream.use { epubParser(inputStream = inputStream) }
        epubImporter(
            storageFolderName = bookTitle,
            appFileResolver = appFileResolver,
            repository = this,
            epub = epub,
            addToLibrary = addToLibrary
        )
    }

    suspend fun getDatabaseSizeBytes() = withContext(Dispatchers.IO) {
        context.getDatabasePath(name).length()
    }

    fun close() = db.close()
    fun delete() = context.deleteDatabase(name)
    fun clearAllTables() = db.clearAllTables()
    suspend fun vacuum() =
        withContext(Dispatchers.IO) { db.openHelper.writableDatabase.execSQL("VACUUM") }

    suspend fun <T> withTransaction(fn: suspend () -> T) = db.transaction(fn)

    inner class Settings {
        suspend fun clearNonLibraryData() = withContext(Dispatchers.IO)
        {
            db.libraryDao().removeAllNonLibraryRows()
            db.chapterDao().removeAllNonLibraryRows()
            db.chapterBodyDao().removeAllNonChapterRows()
        }

        /**
         * Folder where additional book data like images is stored.
         * Each subfolder must be an unique folder for each book.
         * Each book folder can have an arbitrary structure internally.
         */
        val folderBooks = appFileResolver.folderBooks
    }

}

fun isValid(book: Book): Boolean = book.url.matches("""^(https?|local)://.*""".toRegex())
fun isValid(chapter: Chapter): Boolean =
    chapter.url.matches("""^(https?|local)://.*""".toRegex())