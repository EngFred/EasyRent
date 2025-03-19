package com.engineerfred.easyrent.presentation.screens.image_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.easyrent.presentation.theme.DarkBlueBlack
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageViewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    imageUrl: String
) {

    Scaffold{ innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
        ) {
            // Back navigation button
            IconButton(
                onClick = {
                    onBack()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Image content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlideImage(
                    model = imageUrl,
                    contentDescription = "User Profile Image",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(DarkBlueBlack)
                )
            }
        }
    }
}
