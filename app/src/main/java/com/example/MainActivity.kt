package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.domain.RoleMode
import com.example.ui.NavTab
import com.example.ui.OmahViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: OmahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val roleMode by viewModel.roleMode.collectAsState()
                val activeCashier by viewModel.activeCashierName.collectAsState()
                val pinDialogVisible by viewModel.pinDialogVisible.collectAsState()
                val pinErrorMessage by viewModel.pinErrorMessage.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState()
                val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                // Display UI feedback messages
                LaunchedEffect(uiMessage) {
                    uiMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUiMessage()
                    }
                }

                // Handle system back navigation (Back to Dashboard if not already there)
                BackHandler(enabled = currentTab != NavTab.DASHBOARD) {
                    viewModel.selectTab(NavTab.DASHBOARD)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        OmahTopBar(
                            roleMode = roleMode,
                            activeCashier = activeCashier,
                            currentTab = currentTab,
                            onRoleClick = {
                                if (roleMode == RoleMode.OWNER) {
                                    viewModel.lockToCashier()
                                } else {
                                    viewModel.requestOwnerAction { }
                                }
                            },
                            onOpenDrawerOrMenu = { targetTab ->
                                if (targetTab == NavTab.STOK || targetTab == NavTab.KAS) {
                                    viewModel.selectTab(targetTab)
                                } else if (targetTab == NavTab.SETTINGS) {
                                    viewModel.requestOwnerAction {
                                        viewModel.selectTab(NavTab.SETTINGS)
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        OmahBottomNavBar(
                            currentTab = currentTab,
                            onTabSelected = { tab ->
                                if (tab == NavTab.CETAK && roleMode != RoleMode.OWNER) {
                                    viewModel.selectTab(tab)
                                } else {
                                    viewModel.selectTab(tab)
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            NavTab.JUAL -> PosScreen(viewModel = viewModel)
                            NavTab.BELI -> PurchasingScreen(viewModel = viewModel)
                            NavTab.BAYAR -> PaymentScreen(viewModel = viewModel)
                            NavTab.CETAK -> ReportsScreen(viewModel = viewModel)
                            NavTab.STOK -> StockOpnameScreen(viewModel = viewModel)
                            NavTab.KAS -> CashShiftScreen(viewModel = viewModel)
                            NavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }

                // Owner PIN dialog for authorization
                OwnerPinDialog(
                    isVisible = pinDialogVisible,
                    errorMessage = pinErrorMessage,
                    onDismiss = { viewModel.dismissPinDialog() },
                    onSubmitPin = { pin -> viewModel.verifyOwnerPin(pin) }
                )

                // Struk Receipt Dialog on completed sale
                lastCompletedSale?.let { (sale, items) ->
                    ReceiptDialog(
                        sale = sale,
                        items = items,
                        onDismiss = { viewModel.dismissReceipt() }
                    )
                }
            }
        }
    }
}
