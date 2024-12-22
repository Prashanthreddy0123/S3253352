package uk.ac.tees.mad.estore.data

import retrofit2.http.GET
import retrofit2.http.Path
import uk.ac.tees.mad.estore.domain.Product

interface ApiService {
    @GET("products")
    suspend fun getAllProducts(): List<Product>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String
    ): List<Product>

    @GET("products/categories")
    suspend fun getCategories(): List<String>
}