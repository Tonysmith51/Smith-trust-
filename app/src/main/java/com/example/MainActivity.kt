package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.TrustGeminiAssistant
import com.example.model.LoanItem
import com.example.security.BiometricAuthManager
import com.example.ui.TrustViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.LocalTrustColors
import com.example.ui.theme.SmithTrustTheme
import com.example.util.NotificationHelper
import com.example.util.configureAppCheck

data class TrustTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : FragmentActivity() {
    private val viewModel: TrustViewModel by viewModels()
    private val geminiAssistant = TrustGeminiAssistant()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mandatory Firebase App Check configuration with launch Intent before UI
        configureAppCheck(this, intent)

        // 2. Initialize sovereign push notification channels
        NotificationHelper.initNotificationChannels(this)

        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val isSyncingBankFeeds by viewModel.isSyncingBankFeeds.collectAsStateWithLifecycle()
            val syncFeedback by viewModel.syncFeedback.collectAsStateWithLifecycle()
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var showColorCustomizer by remember { mutableStateOf(false) }
            var showGeminiSheet by remember { mutableStateOf(false) }
            var showNotificationSheet by remember { mutableStateOf(false) }
            var isVaultUnlocked by rememberSaveable { mutableStateOf(true) }

            val tabs = remember {
                listOf(
                    TrustTab("Vault", Icons.Default.Shield, "tab_vault"),
                    TrustTab("Banks", Icons.Default.AccountBalance, "tab_banks"),
                    TrustTab("Loans", Icons.Default.CreditCard, "tab_loans"),
                    TrustTab("Cards", Icons.Default.Payment, "tab_cards"),
                    TrustTab("Insurance", Icons.Default.HealthAndSafety, "tab_insurance"),
                    TrustTab("Estates", Icons.Default.Home, "tab_estates"),
                    TrustTab("Fleet", Icons.Default.DirectionsCar, "tab_fleet"),
                    TrustTab("LLCs", Icons.Default.Business, "tab_llcs")
                )
            }

            // Android BackHandler: if on any sub-tab, return to Vault first
            BackHandler(enabled = selectedTabIndex != 0) {
                selectedTabIndex = 0
            }

            SmithTrustTheme(config = state.themeConfig) {
                val tokens = LocalTrustColors.current

                if (!isVaultUnlocked) {
                    BiometricLockOverlay(
                        onTriggerBiometric = {
                            BiometricAuthManager.promptBiometric(
                                activity = this@MainActivity,
                                onSuccess = { isVaultUnlocked = true },
                                onError = { /* User can use PIN fallback */ }
                            )
                        },
                        onUnlockSuccess = { isVaultUnlocked = true }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tokens.background),
                        contentWindowInsets = WindowInsets.systemBars,
                        topBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(tokens.background)
                            ) {
                                ExecutiveHeader(
                                    onOpenColorCustomizer = { showColorCustomizer = true },
                                    onResetDefaults = { viewModel.resetToDefaults() },
                                    onOpenGeminiCopilot = { showGeminiSheet = true },
                                    onOpenNotifications = { showNotificationSheet = true },
                                    onLockVault = { isVaultUnlocked = false },
                                    onToggleThemeMode = {
                                        val current = state.themeConfig
                                        viewModel.updateThemeConfig(current.copy(isLightMode = !current.isLightMode, isTrueAmoled = false))
                                    },
                                    isLightMode = state.themeConfig.isLightMode
                                )

                                ExecutiveKpiRibbon(
                                    state = state,
                                    onTabSelect = { tabIndex -> selectedTabIndex = tabIndex }
                                )

                                // 8-Tab Scrollable Tab Bar
                                ScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    containerColor = tokens.surfaceElevated,
                                    contentColor = tokens.accent,
                                    edgePadding = 12.dp,
                                    indicator = { tabPositions ->
                                        if (selectedTabIndex in tabPositions.indices) {
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                                height = 3.dp,
                                                color = tokens.accent
                                            )
                                        }
                                    },
                                    divider = {
                                        HorizontalDivider(color = tokens.border)
                                    }
                                ) {
                                    tabs.forEachIndexed { index, tab ->
                                        val isSelected = selectedTabIndex == index
                                        Tab(
                                            selected = isSelected,
                                            onClick = { selectedTabIndex = index },
                                            modifier = Modifier.testTag(tab.testTag),
                                            text = {
                                                Text(
                                                    text = tab.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) tokens.accent else tokens.textSecondary
                                                )
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    tint = if (isSelected) tokens.accent else tokens.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                onClick = { showGeminiSheet = true },
                                containerColor = tokens.accent,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("gemini_fab")
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini Copilot",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ask Gemini",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(tokens.background)
                        ) {
                            Crossfade(
                                targetState = selectedTabIndex,
                                label = "tab_transition"
                            ) { tabIndex ->
                                when (tabIndex) {
                                    0 -> VaultScreen(
                                        state = state,
                                        onUpdateLeg = { idx, leg -> viewModel.updateVaultLeg(idx, leg) },
                                        onUpdateCompounding = { comp -> viewModel.updateCompounding(comp) },
                                        onUpdateLoan = { viewModel.updateLoan(it) },
                                        onUpdateAccount = { viewModel.updateVaultAccount(it) },
                                        onAddAccount = { viewModel.addVaultAccount(it) },
                                        onDeleteAccount = { viewModel.deleteVaultAccount(it) },
                                        accountDataProvider = viewModel.vaultAccountProvider
                                    )
                                    1 -> BanksScreen(
                                        state = state,
                                        onAddAccount = { viewModel.addBankAccount(it) },
                                        onUpdateAccount = { viewModel.updateBankAccount(it) },
                                        onDeleteAccount = { viewModel.deleteBankAccount(it) },
                                        isSyncing = isSyncingBankFeeds,
                                        syncFeedback = syncFeedback,
                                        onSyncLiveFeeds = { viewModel.syncLiveBankFeeds() },
                                        onRefreshAccount = { viewModel.refreshSingleAccount(it) },
                                        onClearSyncFeedback = { viewModel.clearSyncFeedback() }
                                    )
                                    2 -> LoansScreen(
                                        state = state,
                                        onAddLoan = { viewModel.addLoan(it) },
                                        onUpdateLoan = { viewModel.updateLoan(it) },
                                        onDeleteLoan = { viewModel.deleteLoan(it) }
                                    )
                                    3 -> CardsScreen(
                                        state = state,
                                        onUpdateJpSpend = { viewModel.updateJpSpend(it) },
                                        onAddFamilyCard = { viewModel.addFamilyCard(it) },
                                        onUpdateFamilyCard = { viewModel.updateFamilyCard(it) },
                                        onDeleteFamilyCard = { viewModel.deleteFamilyCard(it) },
                                        onUpdateMinorCustodialFund = { viewModel.updateMinorCustodialFund(it) }
                                    )
                                    4 -> InsuranceScreen(
                                        state = state,
                                        onAddPolicy = { viewModel.addPolicy(it) },
                                        onUpdatePolicy = { viewModel.updatePolicy(it) },
                                        onDeletePolicy = { viewModel.deletePolicy(it) }
                                    )
                                    5 -> RealEstateScreen(
                                        state = state,
                                        onAddProperty = { viewModel.addProperty(it) },
                                        onUpdateProperty = { viewModel.updateProperty(it) },
                                        onDeleteProperty = { viewModel.deleteProperty(it) }
                                    )
                                    6 -> FleetScreen(
                                        state = state,
                                        onAddVehicle = { viewModel.addVehicle(it) },
                                        onUpdateVehicle = { viewModel.updateVehicle(it) },
                                        onDeleteVehicle = { viewModel.deleteVehicle(it) }
                                    )
                                    7 -> LlcsScreen(
                                        state = state,
                                        onUpdateBeverageSim = { viewModel.updateBeverageSim(it) },
                                        onUpdateSnackSim = { viewModel.updateSnackSim(it) },
                                        onUpdateLlcTaxEquitySim = { viewModel.updateLlcTaxEquitySim(it) }
                                    )
                                }
                            }
                        }

                        if (showColorCustomizer) {
                            ColorCustomizerSheet(
                                currentConfig = state.themeConfig,
                                onConfigChange = { newConfig ->
                                    viewModel.updateThemeConfig(newConfig)
                                },
                                onDismiss = { showColorCustomizer = false }
                            )
                        }

                        if (showGeminiSheet) {
                            GeminiCopilotSheet(
                                state = state,
                                assistant = geminiAssistant,
                                onDismiss = { showGeminiSheet = false }
                            )
                        }

                        if (showNotificationSheet) {
                            NotificationCenterSheet(
                                state = state,
                                onDismiss = { showNotificationSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
