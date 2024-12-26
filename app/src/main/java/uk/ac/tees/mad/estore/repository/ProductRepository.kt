package uk.ac.tees.mad.estore.repository

import uk.ac.tees.mad.estore.domain.Product

interface ProductRepository {
    suspend fun getAllProducts(): Result<List<Product>>
    suspend fun getProductsByCategory(category: String): Result<List<Product>>
    suspend fun getCategories(): Result<List<String>>
    suspend fun getProduct(productId: String): Result<Product>
}