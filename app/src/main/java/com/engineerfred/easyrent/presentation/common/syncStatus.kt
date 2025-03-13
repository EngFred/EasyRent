package com.engineerfred.easyrent.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.engineerfred.easyrent.presentation.theme.MySurface

@Composable
fun SyncStatus(
    size: Dp = 20.dp,
    isSynced: Boolean
) {
    val statusIcon = if(isSynced ) Icons.Rounded.DoneAll else Icons.Rounded.Done
    Icon(
        imageVector = statusIcon,
        contentDescription = null,
        modifier = Modifier.size(size),
        tint = MySurface
    )
}