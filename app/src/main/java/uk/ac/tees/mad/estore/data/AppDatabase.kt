package uk.ac.tees.mad.estore.data

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.ac.tees.mad.estore.domain.FavoriteProduct

@Database(entities = [FavoriteProduct::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "estore_db"
    }
}