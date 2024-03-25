package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.data.helpers.SHIORI_GITHUB_URL
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.ui.components.ErrorDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    goToLogin: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    val settingsUiState = settingsViewModel.settingsUiState.collectAsState().value
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
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
                compatView = settingsViewModel.compactView,
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onCompactViewChanged = settingsViewModel.compactView
            )
        }
    }
}

@Composable
fun SettingsContent(
    settingsUiState: UiState<String>,
    makeArchivePublic: MutableStateFlow<Boolean>,
    createEbook: MutableStateFlow<Boolean>,
    createArchive: MutableStateFlow<Boolean>,
    compatView: MutableStateFlow<Boolean>,
    onCompactViewChanged: MutableStateFlow<Boolean>,
    onLogout: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    themeMode: ThemeMode,
    goToLogin: () -> Unit
) {
    val context = LocalContext.current
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
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            VisualSection(
                themeMode = themeMode,
                compactView = compatView,
                onThemeChanged = onThemeChanged,
                onCompactViewChanged = onCompactViewChanged
            )
            Spacer(modifier = Modifier.height(18.dp))
            DefaultsSection(
                makeArchivePublic = makeArchivePublic,
                createEbook = createEbook,
                createArchive = createArchive
            )
            Spacer(modifier = Modifier.height(18.dp))
            AccountSection(
                onLogout = onLogout,
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToSeverSettings = {
                    context.openUrlInBrowser(SHIORI_GITHUB_URL)
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
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
        createEbook = remember { MutableStateFlow(false) },
        onNavigateToTermsOfUse = {},
        onNavigateToPrivacyPolicy = {},
        onCompactViewChanged = remember { MutableStateFlow(false) },
        compatView = remember { MutableStateFlow(false) }
    )
}