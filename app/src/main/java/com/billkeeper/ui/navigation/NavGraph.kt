package com.billkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.billkeeper.ui.bill.BillDetailScreen
import com.billkeeper.ui.bill.BillListScreen
import com.billkeeper.ui.home.HomeScreen
import com.billkeeper.ui.record.RecordScreen
import com.billkeeper.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val RECORD = "record"
    const val BILLS = "bills"
    const val BILL_DETAIL = "bill_detail/{billId}"
    const val SETTINGS = "settings"

    fun billDetail(billId: Long) = "bill_detail/$billId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToRecord = { navController.navigate(Routes.RECORD) },
                onNavigateToBills = { navController.navigate(Routes.BILLS) },
                onNavigateToBillDetail = { billId ->
                    navController.navigate(Routes.billDetail(billId))
                }
            )
        }

        composable(Routes.RECORD) {
            RecordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBillDetail = { billId ->
                    navController.navigate(Routes.billDetail(billId))
                },
                onNavigateToManualRecord = {
                    navController.navigate(Routes.MANUAL_RECORD)
                }
            )
        }

        composable(Routes.MANUAL_RECORD) {
            ManualRecordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen()
        }

        composable(Routes.BILLS) {
            BillListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBillDetail = { billId ->
                    navController.navigate(Routes.billDetail(billId))
                }
            )
        }

        composable(
            route = Routes.BILL_DETAIL,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: 0L
            BillDetailScreen(
                billId = billId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
