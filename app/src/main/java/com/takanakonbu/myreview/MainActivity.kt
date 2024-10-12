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
import com.takanakonbu.myreview.review.AddReviewScreen as ReviewAddScreen
import com.takanakonbu.myreview.category.AddReviewScreen as CategoryAddScreen
import com.takanakonbu.myreview.review.ReviewScreen
import com.takanakonbu.myreview.review.ReviewDetailScreen
import com.takanakonbu.myreview.ui.theme.MyReviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyReviewTheme(darkTheme = false) {
                MyReviewApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewApp() {
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
                    IconButton(onClick = { /* 設定画面に遷移するロジックをここに追加 */ }) {
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
            // カテゴリー一覧画面
            // - "add_category"へ遷移：新規カテゴリー追加
            // - "edit_category/{categoryId}"へ遷移：既存カテゴリーの編集
            // - "review_list/{categoryId}/{categoryName}"へ遷移：特定カテゴリーのレビュー一覧表示
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

            // 新規カテゴリー追加画面
            // - 前の画面（通常はカテゴリー一覧）に戻る
            composable("add_category") {
                CategoryAddScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // カテゴリー編集画面
            // - 前の画面（通常はカテゴリー一覧）に戻る
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

            // レビュー一覧画面（特定のカテゴリーに属するレビュー）
            // - "review_detail/{reviewId}"へ遷移：レビューの詳細表示
            // - "add_review"へ遷移：新規レビュー追加
            // - 前の画面（通常はカテゴリー一覧）に戻る
            composable(
                "review_list/{categoryId}/{categoryName}",
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: return@composable
                ReviewScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            // レビュー詳細画面
            // - "edit_review/{reviewId}"へ遷移：レビューの編集
            // - 前の画面（通常はレビュー一覧）に戻る
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

            // 新規レビュー追加画面
            // - 前の画面（通常はレビュー一覧）に戻る
            composable("add_review") {
                ReviewAddScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // レビュー編集画面
            // - 前の画面（通常はレビュー詳細）に戻る
            composable(
                "edit_review/{reviewId}",
                arguments = listOf(navArgument("reviewId") { type = NavType.IntType })
            ) { backStackEntry ->
                val reviewId = backStackEntry.arguments?.getInt("reviewId") ?: return@composable
                ReviewAddScreen(
                    onNavigateBack = { navController.popBackStack() },
                    reviewId = reviewId
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