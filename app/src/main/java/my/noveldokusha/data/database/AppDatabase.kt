package my.noveldokusha.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import my.noveldokusha.data.database.DAOs.ChapterBodyDao
import my.noveldokusha.data.database.DAOs.ChapterDao
import my.noveldokusha.data.database.DAOs.LibraryDao
import my.noveldokusha.data.database.tables.Book
import my.noveldokusha.data.database.tables.Chapter
import my.noveldokusha.data.database.tables.ChapterBody
import java.io.InputStream


interface AppDatabaseOperations {
    /**
     * Execute the whole database calls as an atomic operation
     */
    suspend fun <T> transaction(block: suspend () -> T): T
}

@Database(
    entities = [
        Book::class,
        Chapter::class,
        ChapterBody::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(), AppDatabaseOperations {
    abstract fun libraryDao(): LibraryDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterBodyDao(): ChapterBodyDao

    override suspend fun <T> transaction(block: suspend () -> T): T = withTransaction(block)

    companion object {
        fun createRoom(ctx: Context, name: String) = Room
            .databaseBuilder(ctx, AppDatabase::class.java, name)
            .addMigrations(*migrations())
            .build()

        fun createRoomFromStream(ctx: Context, name: String, inputStream: InputStream) = Room
            .databaseBuilder(ctx, AppDatabase::class.java, name)
            .addMigrations(*migrations())
            .createFromInputStream { inputStream }
            .build()
    }
}

private fun migrations() = arrayOf(
    migration(1, 2) {
        it.execSQL("ALTER TABLE Chapter ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    },
    migration(2, 3) {
        it.execSQL("ALTER TABLE Book ADD COLUMN inLibrary INTEGER NOT NULL DEFAULT 0")
        it.execSQL("UPDATE Book SET inLibrary = 1")
    },
    migration(3, 4) {
        it.execSQL("ALTER TABLE Book ADD COLUMN coverImageUrl TEXT NOT NULL DEFAULT ''")
        it.execSQL("ALTER TABLE Book ADD COLUMN description TEXT NOT NULL DEFAULT ''")
    },
    migration(4, 5) {
        it.execSQL("ALTER TABLE Book ADD COLUMN lastReadEpochTimeMilli INTEGER NOT NULL DEFAULT 0")
    }
)

private fun migration(vi: Int, vf: Int, migrate: (SupportSQLiteDatabase) -> Unit) =
    object : Migration(vi, vf) {
        override fun migrate(database: SupportSQLiteDatabase) = migrate(database)
    }
