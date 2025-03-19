package com.engineerfred.easyrent.presentation.nav

sealed class MainGraphDestinations(val dest: String){
    data object RoomsList : MainGraphDestinations("rooms_list")

    data object RoomDetails : MainGraphDestinations("room_details?roomId={roomId}") {
        fun createRoute(roomId: String?) = "room_details?${roomId}"
    }
    data object TenantDetails : MainGraphDestinations("tenant_details?tenantId={tenantId}?roomId={roomId}?monthlyRent={monthlyRent}?roomNumber={roomNumber}") {
        fun createRoute(tenantId: String?, roomId: String?, monthlyRent: String?, roomNumber: String?) = "tenant_details?tenantId=$tenantId?roomId=$roomId?monthlyRent=$monthlyRent?roomNumber=$roomNumber"
    }

    data object Payments : MainGraphDestinations("payments")
    data object AddPayment: MainGraphDestinations("add_payment")
    data object Expenses: MainGraphDestinations("expenses")
    data object Tenants: MainGraphDestinations("tenants")
    data object Profile: MainGraphDestinations("profile")
    data object ImageView: MainGraphDestinations("image_view/{imageUrl}") {
        fun createRoute(imageUrl: String) = "image_view/${imageUrl}"
    }

}