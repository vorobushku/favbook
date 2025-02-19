package com.example.favbook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

@Composable
fun BookItemView(
    title: String,
    imageUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Картинка книги слева
        if (imageUrl.isNotEmpty()) {
            Image(
                painter = rememberImagePainter(imageUrl),
                contentDescription = title,
                modifier = Modifier
                    .size(50.dp) // Устанавливаем размер картинки
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Если нет изображения, показываем placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            )
        }

        // Текст книги справа
        Spacer(modifier = Modifier.width(8.dp)) // Отступ между картинкой и текстом
        Text(
            text = title,
            fontSize = 16.sp,  // Размер шрифта
            fontWeight = FontWeight.Bold,  // Жирный шрифт
            color = Color.Black,  // Цвет текста
            modifier = Modifier.weight(1f) // Заполняет оставшееся пространство
        )
    }
}