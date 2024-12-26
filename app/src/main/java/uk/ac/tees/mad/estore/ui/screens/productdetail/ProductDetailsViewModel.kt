package uk.ac.tees.mad.estore.ui.screens.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.estore.data.FavoriteDao
import uk.ac.tees.mad.estore.domain.FavoriteProduct
import uk.ac.tees.mad.estore.domain.Product
import uk.ac.tees.mad.estore.repository.ProductRepository
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoriteDao: FavoriteDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState = _uiState.asStateFlow()

    val isFavorite = favoriteDao.isFavorite(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val product = productRepository.getProduct(productId).getOrThrow()
                _uiState.update {
                    it.copy(
                        product = product,
                        isLoading = false,
                        error = null
                    )
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

    fun toggleFavorite() {
        viewModelScope.launch {
            val product = _uiState.value.product ?: return@launch
            if (isFavorite.value) {
                favoriteDao.removeFromFavorites(product.id)
            } else {
                favoriteDao.addToFavorites(
                    FavoriteProduct(
                        id = product.id,
                        title = product.title,
                        price = product.price,
                        description = product.description,
                        category = product.category,
                        image = product.image,
                        rating = product.rating.rate,
                        ratingCount = product.rating.count
                    )
                )
            }
        }
    }

    fun retryLoading() {
        loadProduct()
    }
}

data class ProductDetailsUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)