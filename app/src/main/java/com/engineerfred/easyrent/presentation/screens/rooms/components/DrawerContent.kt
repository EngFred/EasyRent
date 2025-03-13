package com.engineerfred.easyrent.presentation.screens.rooms.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MySurface
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    onPaymentsClicked: () -> Unit,
    onExpensesClicked: () -> Unit,
    onTenantsClicked: () -> Unit,
    onProfileClicked: () -> Unit,
) {

    Column(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(MySecondary, MyTertiary)))
            .padding(top = 30.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Easy Rent!",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        blurRadius = 6f,
                        offset = Offset(2f, 2f)
                    )
                ),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(2.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null,
                    tint = MySurface
                )
            },
            label = {
                Text(
                    text = "Tenants",
                    style = TextStyle(fontSize = 19.sp, color = MySurface)
                )
            },
            selected = false,
            onClick = onTenantsClicked
        )
        Spacer(modifier = Modifier.height(4.dp))
        NavigationDrawerItem(
            icon = {
                Icon(imageVector = Icons.Rounded.MonetizationOn, contentDescription = null, tint = MySurface)
            },
            label = {
                Text(
                    text = "Payments",
                    style = TextStyle(fontSize = 19.sp, color = MySurface)
                )
            },
            selected = false,
            onClick = onPaymentsClicked
        )
        Spacer(modifier = Modifier.height(4.dp))
        NavigationDrawerItem(
            icon = {
                Icon(imageVector = Icons.Rounded.Money, contentDescription = null, tint = MySurface)
            },
            label = {
                Text(
                    text = "Expenses",
                    style = TextStyle(fontSize = 19.sp, color = MySurface)
                )
            },
            selected = false,
            onClick = {
                onExpensesClicked()
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        NavigationDrawerItem(
            icon = {
                Icon(imageVector = Icons.Rounded.Person2, contentDescription = null, tint = MySurface)
            },
            label = {
                Text(
                    text = "Profile",
                    style = TextStyle(fontSize = 19.sp, color = MySurface)
                )
            },
            selected = false,
            onClick = onProfileClicked
        )
    }
}