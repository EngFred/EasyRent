package com.engineerfred.easyrent.presentation.nav

sealed class MainScreens(val dest: String){
    data object RoomsList : MainScreens("rooms_list")

    data object RoomDetails : MainScreens("room_details?roomId={roomId}") {
        fun createRoute(roomId: String?) = "room_details?${roomId}"
    }
    data object TenantDetails : MainScreens("tenant_details?tenantId={tenantId}?roomId={roomId}?monthlyRent={monthlyRent}?roomNumber={roomNumber}") {
        fun createRoute(tenantId: String?, roomId: String?, monthlyRent: String?, roomNumber: String?) = "tenant_details?tenantId=$tenantId?roomId=$roomId?monthlyRent=$monthlyRent?roomNumber=$roomNumber"
    }

    data object Payments : MainScreens("payments")
    data object AddPayment: MainScreens("add_payment")
    data object Expenses: MainScreens("expenses")
    data object Tenants: MainScreens("tenants")
    data object Profile: MainScreens("profile")

}