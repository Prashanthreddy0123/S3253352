package uk.ac.tees.mad.estore.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.estore.domain.Product
import uk.ac.tees.mad.estore.repository.ProductRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var allProducts = listOf<Product>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load categories and products in parallel
            try {
                coroutineScope {
                    val categoriesDeferred = async { productRepository.getCategories() }
                    val productsDeferred = async { productRepository.getAllProducts() }

                    val categories = categoriesDeferred.await().getOrThrow()
                    allProducts = productsDeferred.await().getOrThrow()

                    _uiState.update {
                        it.copy(
                            categories = categories,
                            filteredProducts = allProducts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterProducts(query, _uiState.value.selectedCategory)
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        filterProducts(_searchQuery.value, category)
    }

    private fun filterProducts(query: String, category: String?) {
        val filtered = allProducts.filter { product ->
            val matchesQuery = product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category == category
            matchesQuery && matchesCategory
        }
        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    fun retryLoading() {
        loadInitialData()
    }
}

data class HomeUiState(
    val categories: List<String> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)