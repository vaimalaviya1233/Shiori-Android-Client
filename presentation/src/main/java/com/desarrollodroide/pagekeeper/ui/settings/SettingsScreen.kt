package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.pagekeeper.ui.components.ErrorDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    goToLogin: () -> Unit
) {
    val settingsUiState = settingsViewModel.settingsUiState.collectAsState().value

    SettingsContent(
        settingsUiState = settingsUiState,
        onLogout = { settingsViewModel.logout() },
        goToLogin = goToLogin,
        themeMode = settingsViewModel.getThemeMode(),
        onThemeChanged = { newMode ->
            settingsViewModel.setTheme(newMode)
        },
        makeArchivePublic = settingsViewModel.makeArchivePublic,
        createEbook = settingsViewModel.createEbook,
        createArchive = settingsViewModel.createArchive,
    )
}

@Composable
fun SettingsContent(
    settingsUiState: UiState<String>,
    makeArchivePublic: MutableStateFlow<Boolean>,
    createEbook: MutableStateFlow<Boolean>,
    createArchive: MutableStateFlow<Boolean>,
    onLogout: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    themeMode: ThemeMode,
    goToLogin: () -> Unit
) {
    if (settingsUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
        Log.v("SettingsContent!!", "settingsUiState.isLoading")
    }
    if (!settingsUiState.error.isNullOrEmpty()) {
        ErrorDialog(
            title = "Error",
            content = settingsUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                goToLogin()
            }
        )
        Log.v("SettingsContent!!", settingsUiState.error)
    } else if (settingsUiState.data == null) {
        Log.v("SettingsContent!!", "settingsUiState.data is null")
    } else {
        Log.v("SettingsContent!!", "settingsUiState.data is not null")
        LaunchedEffect(Unit) {
            goToLogin()
        }
    }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
        Divider(Modifier.fillMaxWidth(), color = Color.Black, thickness = 1.dp)
        VisualSection(
            themeMode = themeMode,
            onThemeChanged = onThemeChanged
        )
        DefaultsSection(
            makeArchivePublic = makeArchivePublic,
            createEbook = createEbook,
            createArchive = createArchive
        )
        AccountSection(onLogout = onLogout)
    }
}

data class Item(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {},
    val switchState: MutableStateFlow<Boolean> = MutableStateFlow(false)
)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        onLogout = {},
        goToLogin = {},
        onThemeChanged = {},
        settingsUiState = UiState(isLoading = false),
        themeMode = ThemeMode.AUTO,
        makeArchivePublic = remember { MutableStateFlow(false) },
        createArchive = remember { MutableStateFlow(false) },
        createEbook = remember { MutableStateFlow(false) }
    )
}