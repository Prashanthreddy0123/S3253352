package uk.ac.tees.mad.estore.repository

import uk.ac.tees.mad.estore.data.ApiService
import uk.ac.tees.mad.estore.domain.Product
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {
    override suspend fun getAllProducts(): Result<List<Product>> =
        try {
            Result.success(apiService.getAllProducts())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getProductsByCategory(category: String): Result<List<Product>> =
        try {
            Result.success(apiService.getProductsByCategory(category))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getCategories(): Result<List<String>> =
        try {
            Result.success(apiService.getCategories())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getProduct(productId: String): Result<Product> =
        try {
            Result.success(apiService.getProduct(productId))
        } catch (e: Exception) {
            Result.failure(e)
        }
}