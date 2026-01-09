package com.asistente.planificador.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FootPage(currentTab: String, onTabSelected: (String) -> Unit) {
    val colorMarron = Color(0xFFAC5343)

    // Un Box para poder "encimar" la raya naranja sin empujar los iconos
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
    ) {
        // 1. La barra naranja (Flota arriba del todo, centrada)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp) // Distancia desde el borde superior del pie
                .width(35.dp)
                .height(4.dp)
                .background(colorMarron, RoundedCornerShape(2.dp))
        )

        // 2. El menú de navegación real
        NavigationBar(
            containerColor = Color.Transparent, // Transparente para ver el fondo del Box
            modifier = Modifier.height(130.dp).padding(top = 30.dp),
            tonalElevation = 0.dp
        ) {
            // CALENDAR
            NavigationBarItem(
                selected = currentTab == "calendar",
                onClick = { onTabSelected("calendar") },
                label = { Text("CALENDAR", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Outlined.CalendarMonth, null) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorMarron,
                    selectedTextColor = colorMarron,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )

            // TODAY (Ahora el icono estará alineado con los otros)
            NavigationBarItem(
                selected = currentTab == "today",
                onClick = { onTabSelected("today") },
                label = { Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = {
                    // El icono ya no lleva la barra dentro, así que no se baja
                    Icon(Icons.Outlined.Today, null)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorMarron,
                    selectedTextColor = colorMarron,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )

            // PROFILE
            NavigationBarItem(
                selected = currentTab == "profile",
                onClick = { onTabSelected("profile") },
                label = { Text("PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Outlined.Person, null) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorMarron,
                    selectedTextColor = colorMarron,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}