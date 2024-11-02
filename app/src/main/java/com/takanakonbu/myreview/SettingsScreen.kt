package com.takanakonbu.myreview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.DefaultMainColor
import com.takanakonbu.myreview.ui.theme.MainColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
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

    var backupCompleted by remember { mutableStateOf(false) }
    var restoreCompleted by remember { mutableStateOf(false) }
    val backupProgress by viewModel.backupProgress.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    var showColorPicker by remember { mutableStateOf(false) }

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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "バックアップ",
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (backupProgress > 0 && backupProgress < 1) {
            LinearProgressIndicator(
                progress = { backupProgress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
        if (backupCompleted) {
            Text(
                "バックアップが完了しました。",
                modifier = Modifier.padding(8.dp)
            )
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "データの復元",
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (restoreProgress > 0 && restoreProgress < 1) {
            LinearProgressIndicator(
                progress = { restoreProgress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
        if (restoreCompleted) {
            Text("データの復元が完了しました。")
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { showColorPicker = true }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ColorLens,
                contentDescription = "色の変更",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "色の変更",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MainColor.value)
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { viewModel.updateMainColor(DefaultMainColor) }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "デフォルトカラーに戻す",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "デフォルトカラーに戻す",
                style = MaterialTheme.typography.titleMedium
            )
        }


        if (showColorPicker) {
            ColorPickerDialog(
                initialColor = MainColor.value,
                onColorSelected = { color ->
                    viewModel.updateMainColor(color)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            // LocalContext を remember で保持
            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://takanakonbu.fun/my-review%e3%82%a2%e3%83%97%e3%83%aa/"))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "ブラウザを開けません", Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = "プライバシーポリシー",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "プライバシーポリシー",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "注意事項",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "注意事項",
                style = MaterialTheme.typography.titleMedium
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

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("色を選択") },
        text = {
            Column {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    controller = controller,
                    initialColor = initialColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                AlphaTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    controller = controller
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(controller.selectedColor.value)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}