package com.engineerfred.easyrent.domain.modals

//enum class UserInfoUpdateStatus {
//    UpdatingProfileImg,
//    UpdatingNames,
//    Updating
//}

sealed class UserInfoUpdateStatus(val status: String) {
    data object UpdatingProfileImage : UserInfoUpdateStatus("updating_profile_img")
    data object UpdatingNames : UserInfoUpdateStatus("updating_names")
    data object UpdatingHostelName: UserInfoUpdateStatus("updating_hostel_name")
    data object UpdatingPhoneNumber: UserInfoUpdateStatus("updating_phone_number")
}