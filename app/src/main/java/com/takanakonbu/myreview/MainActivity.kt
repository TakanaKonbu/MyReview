package com.takanakonbu.myreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.takanakonbu.myreview.category.AddReviewScreen
import com.takanakonbu.myreview.category.CategoryList
import com.takanakonbu.myreview.category.EditCategoryScreen
import com.takanakonbu.myreview.review.ReviewScreen
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

@Composable
fun MyReviewApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() }
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
                    onAddCategory = { navController.navigate("add_review") },
                    onEditCategory = { categoryId ->
                        navController.navigate("edit_category/$categoryId")
                    },
                    onCategorySelected = { categoryId, categoryName ->
                        navController.navigate("review_list/$categoryId/$categoryName")
                    }
                )
            }
            composable("add_review") {
                AddReviewScreen(
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
                ReviewScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = "My Review",
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        },
        actions = {
            IconButton(onClick = { /* 設定画面に遷移するロジックをここに追加 */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6D6DF6)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    MyReviewTheme {
        TopBar()
    }
}