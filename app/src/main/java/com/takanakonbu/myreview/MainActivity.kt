package com.takanakonbu.myreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.takanakonbu.myreview.category.CategoryList
import com.takanakonbu.myreview.category.EditCategoryScreen
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.AddReviewScreen
import com.takanakonbu.myreview.category.AddReviewScreen as CategoryAddScreen
import com.takanakonbu.myreview.review.ReviewListScreen
import com.takanakonbu.myreview.review.ReviewDetailScreen
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.MyReviewTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        enableEdgeToEdge()
        // データベースとリポジトリの初期化
        val database = AppDatabase.getDatabase(applicationContext)
        categoryRepository = CategoryRepository(database.categoryDao())
        reviewRepository = ReviewRepository(database.reviewDao())
        setContent {
            MyReviewTheme(darkTheme = false) {
                MyReviewApp(categoryRepository, reviewRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewApp(categoryRepository: CategoryRepository, reviewRepository: ReviewRepository) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = remember(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route ?: "category_list"
    }

    val currentTitle = remember(currentBackStackEntry) {
        when {
            currentScreen.startsWith("review_list") ->
                currentBackStackEntry?.arguments?.getString("categoryName") ?: "My Review"
            currentScreen.startsWith("review_detail") ->
                "レビュー詳細"
            currentScreen.startsWith("add_review") ->
                "レビュー追加"
            currentScreen.startsWith("edit_review") ->
                "レビュー編集"
            currentScreen == "settings" ->
                "設定"
            else -> "My Review"
        }
    }

    val showBackButton = currentScreen != "category_list"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = currentTitle,
                showBackButton = showBackButton,
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "category_list",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("category_list") {
                CategoryList(
                    onAddCategory = { navController.navigate("add_category") },
                    onEditCategory = { categoryId ->
                        navController.navigate("edit_category/$categoryId")
                    },
                    onCategorySelected = { categoryId, categoryName ->
                        navController.navigate("review_list/$categoryId/$categoryName")
                    }
                )
            }

            composable("add_category") {
                CategoryAddScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "edit_category/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
                EditCategoryScreen(
                    categoryId = categoryId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "review_list/{categoryId}/{categoryName}",
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: return@composable
                ReviewListScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(
                "review_detail/{reviewId}",
                arguments = listOf(navArgument("reviewId") { type = NavType.IntType })
            ) { backStackEntry ->
                val reviewId = backStackEntry.arguments?.getInt("reviewId") ?: return@composable
                ReviewDetailScreen(
                    reviewId = reviewId,
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(
                "add_review/{categoryId}/{categoryName}",
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: return@composable
                AddReviewScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "edit_review/{reviewId}",
                arguments = listOf(navArgument("reviewId") { type = NavType.IntType })
            ) { backStackEntry ->
                val reviewId = backStackEntry.arguments?.getInt("reviewId") ?: return@composable
                AddReviewScreen(
                    reviewId = reviewId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    categoryRepository = categoryRepository,
                    reviewRepository = reviewRepository
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = { Text(text = title, color = Color.White) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6D6DF6)
        )
    )
}