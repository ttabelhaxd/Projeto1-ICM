package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TitleMessage(modifier: Modifier = Modifier, text1: String = "", text2: String = "", ) {
    Row {
        Text(
            text = text1,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text2,
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
        )
    }

}