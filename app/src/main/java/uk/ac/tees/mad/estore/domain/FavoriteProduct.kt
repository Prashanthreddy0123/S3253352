package uk.ac.tees.mad.estore.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_products")
data class FavoriteProduct(
    @PrimaryKey
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Double,
    val ratingCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)