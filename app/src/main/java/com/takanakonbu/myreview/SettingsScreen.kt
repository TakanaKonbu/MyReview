package com.takanakonbu.myreview

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    categoryRepository: CategoryRepository,
    reviewRepository: ReviewRepository
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            LocalContext.current,
            categoryRepository,
            reviewRepository
        )
    )
    val context = LocalContext.current
    var backupCompleted by remember { mutableStateOf(false) }
    var restoreCompleted by remember { mutableStateOf(false) }
    val backupProgress by viewModel.backupProgress.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.backupData(it) { success ->
                backupCompleted = success
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.restoreData(it) { success ->
                restoreCompleted = success
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    backupLauncher.launch("myreview_backup_$timeStamp.json")
                }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = "バックアップ",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "バックアップ",
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (backupProgress > 0 && backupProgress < 1) {
            LinearProgressIndicator(
                progress = backupProgress,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
        if (backupCompleted) {
            Text("バックアップが完了しました。")
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    restoreLauncher.launch(arrayOf("application/json"))
                }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "復元",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "データの復元",
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (restoreProgress > 0 && restoreProgress < 1) {
            LinearProgressIndicator(
                progress = restoreProgress,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
        if (restoreCompleted) {
            Text("データの復元が完了しました。")
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "注意事項",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "注意事項",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "バックアップファイルは必ず「Dropbox」や「GoogleDrive」などのクラウドストレージに保管してください。" +
                    "\n復元先の新たな端末からアクセスできる場所に保管することでデータの復元ができます。" +
                    "\nバックアップデータには個人情報が含まれている可能性があるため、取り扱いには十分注意してください。"
        )
    }
}