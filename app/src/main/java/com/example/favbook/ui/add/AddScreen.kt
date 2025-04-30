package com.example.favbook.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun AddScreen(navController: NavHostController, viewModel: AddViewModel = hiltViewModel()) {
    val state by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Добавить книгу",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 30.dp, start = 11.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Название книги") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        )

        OutlinedTextField(
            value = state.author,
            onValueChange = viewModel::onAuthorChange,
            label = { Text("Автор") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        )

        Text("Выберите список", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))

        if (state.lists.isEmpty()) {
            Text("Сначала необходимо добавить списки", color = Color.Red)
        } else {
            Column {
                state.lists.forEach { list ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onListSelected(list) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedList == list,
                            onClick = { viewModel.onListSelected(list) }
                        )
                        Text(text = list, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.addBook {
                    navController.popBackStack()
                }
            },
            enabled = state.title.isNotEmpty() && state.selectedList != null
        ) {
            Text("Добавить книгу")
        }
    }
}