package uk.ac.tees.mad.estore.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.estore.domain.FavoriteProduct

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_products ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteProduct>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_products WHERE id = :productId)")
    fun isFavorite(productId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favoriteProduct: FavoriteProduct)

    @Query("DELETE FROM favorite_products WHERE id = :productId")
    suspend fun removeFromFavorites(productId: String)
}