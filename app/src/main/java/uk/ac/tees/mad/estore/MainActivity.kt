package uk.ac.tees.mad.estore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import uk.ac.tees.mad.estore.ui.screens.SplashScreen
import uk.ac.tees.mad.estore.ui.screens.auth.AuthScreen
import uk.ac.tees.mad.estore.ui.screens.checkout.CheckoutScreen
import uk.ac.tees.mad.estore.ui.screens.favorite.FavoritesScreen
import uk.ac.tees.mad.estore.ui.screens.home.HomeScreen
import uk.ac.tees.mad.estore.ui.screens.productdetail.ProductDetailsScreen
import uk.ac.tees.mad.estore.ui.screens.profile.ProfileScreen
import uk.ac.tees.mad.estore.ui.theme.EstoreTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EstoreTheme {
                EstoreNavigation()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }

    object Checkout : Screen("checkout/{productId}") {
        fun createRoute(productId: String) = "checkout/$productId"
    }

    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
}

@Composable
fun EstoreNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Auth.route) { AuthScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(
            Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { ProductDetailsScreen(navController = navController) }
        composable(
            Screen.Checkout.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { CheckoutScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Favorites.route) { FavoritesScreen(navController) }
    }
}