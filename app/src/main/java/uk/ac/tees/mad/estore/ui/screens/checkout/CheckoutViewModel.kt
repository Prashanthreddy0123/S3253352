package uk.ac.tees.mad.estore.ui.screens.checkout

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.estore.domain.Product
import uk.ac.tees.mad.estore.repository.ProductRepository
import uk.ac.tees.mad.estore.utils.LocationManager
import uk.ac.tees.mad.estore.utils.LocationUtils
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val locationManager: LocationManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState = _uiState.asStateFlow()

    private val productId: String = checkNotNull(savedStateHandle["productId"])


    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            try {
                val product = productRepository.getProduct(productId).getOrThrow()
                _uiState.update { state ->
                    state.copy(
                        product = product,
                        total = calculateTotal(product.price, state.deliveryFee)
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
        validateFields()
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
        validateFields()
    }

    fun getCurrentLocation(context: Context) {
        if (locationManager.checkLocationPermission()) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoadingLocation = true) }

                    // Get the location
                    val location = locationManager.getCurrentLocation()

                    // Convert location to address
                    val address = locationManager.getAddressFromLocation(location)

                    // Format the address
                    val formattedAddress = locationManager.formatAddress(address)

                    _uiState.update {
                        it.copy(
                            address = formattedAddress,
                            isLoadingLocation = false
                        )
                    }
                    validateFields()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            locationError = e.message ?: "Error getting location",
                            isLoadingLocation = false
                        )
                    }
                }
            }
        } else {
            // Request location permission if not granted
            if (context is ComponentActivity) {
                LocationUtils.requestLocationPermission(context)
            }
        }


    }

    private fun validateFields() {
        _uiState.update { state ->
            state.copy(
                isValid = state.address.isNotBlank() &&
                        state.selectedPaymentMethod != PaymentMethod.NONE
            )
        }
    }

    private fun calculateTotal(productPrice: Double, deliveryFee: Double): Double {
        return productPrice + deliveryFee
    }

    fun placeOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // For demo purpose simulating the delay of 1sec
            delay(1000)
            onSuccess()
        }
    }
}

data class CheckoutUiState(
    val product: Product? = null,
    val address: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.NONE,
    val deliveryFee: Double = 5.0,
    val total: Double = 0.0,
    val isValid: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null
)

enum class PaymentMethod(val displayName: String) {
    NONE("Select Payment Method"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CASH_ON_DELIVERY("Cash on Delivery")
}