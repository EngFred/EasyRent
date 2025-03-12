package com.engineerfred.easyrent.presentation.screens.profile

import android.content.ContentResolver
import com.engineerfred.easyrent.domain.modals.UserInfoUpdateStatus

sealed class ProfileUiEvents  {
    data object RetryClicked: ProfileUiEvents()
    data class UpdateButtonClicked(val contentResolver: ContentResolver): ProfileUiEvents()
    data object LoggedOut: ProfileUiEvents()
    data class SelectedNewProfileImage( val imageUrl: String ): ProfileUiEvents()
    data class ChangedFirstName( val newFirstName: String ): ProfileUiEvents()
    data class ChangedLastName( val newLastName: String ): ProfileUiEvents()
    data class ChangedHostelName( val newHostelName: String ): ProfileUiEvents()
    data class ChangedPhoneNumber( val newPhoneNumber: String ): ProfileUiEvents()
    data class ChangedUpdateState( val userInfoUpdateStatus: UserInfoUpdateStatus ): ProfileUiEvents()
}