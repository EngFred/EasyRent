package com.engineerfred.easyrent.presentation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@Composable
fun BoxScope.CustomSyncToast(
    modifier: Modifier = Modifier,
    showSyncRequired: Boolean,
    dataCount: Int,
    dataName: String
) {

    androidx.compose.animation.AnimatedVisibility(
        modifier = modifier.align(Alignment.TopCenter),
        visible = showSyncRequired,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(500)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(500)
        )

    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(.85f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(MyPrimary, MyTertiary)))
                .padding(16.dp)
        ) {
            val message = if (dataCount == 1) {
                "1 $dataName is pending sync! Tap "
            } else {
                "$dataCount ${dataName}s are pending sync! Tap "
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Sync",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}