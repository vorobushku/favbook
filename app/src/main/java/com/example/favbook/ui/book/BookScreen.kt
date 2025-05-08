package com.example.favbook.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.favbook.data.firebase.rememberFirebaseUser
import java.net.URLEncoder

@Composable
fun BookScreen(
    navController: NavHostController,
    viewModel: BookViewModel = hiltViewModel()
) {
    val user = rememberFirebaseUser()
    val context = LocalContext.current
    val state = viewModel.uiState

    LaunchedEffect(user) {
        user?.uid?.let { viewModel.loadCategories(it) }
    }

    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    user?.uid?.let { viewModel.addCategory(it, context) }
                }) {
                    Text("Добавить", color = Color(0xFF494848))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDialog(false) }) {
                    Text("Отмена", color = Color(0xFF494848))
                }
            },
            title = {
                Text("Новая категория", style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold))
            },
            text = {
                OutlinedTextField(
                    value = state.newCategory,
                    onValueChange = viewModel::onNewCategoryChange,
                    label = { Text("Название категории") },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF494848),
                        unfocusedLabelColor = Color(0xFF807D7D)
                    )
                )
            },
            shape = RoundedCornerShape(35.dp),
        )
    }

    if (state.showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEditCategoryDialogDismiss() },
            confirmButton = {
                TextButton(onClick = {
                    user?.uid?.let { viewModel.updateCategory(it, context) }
                }) {
                    Text("Сохранить", color = Color(0xFF494848))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onEditCategoryDialogDismiss()
                }) {
                    Text("Отмена", color = Color(0xFF494848))
                }
            },
            title = {
                Text("Редактировать категорию", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                OutlinedTextField(
                    value = state.editedCategory,
                    onValueChange = viewModel::onEditCategoryChange,
                    label = { Text("Новое название категории") },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF494848),
                        unfocusedLabelColor = Color(0xFF807D7D)
                    )
                )
            },
            shape = RoundedCornerShape(35.dp),
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp,  start = 15.dp, end = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Book Lists",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { viewModel.showDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        LazyColumn {

            items(state.categories) { category ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable {
                            navController.navigate("category_books_screen/${URLEncoder.encode(category, "UTF-8")}")
                        },
                    shape = RoundedCornerShape(30.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8BD))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(25.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Box{
                            IconButton(onClick = { viewModel.onCategoryOptionsExpand(category) }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Опции")
                            }

                            DropdownMenu(
                                expanded = state.expandedCategory == category,
                                onDismissRequest = { viewModel.onCategoryOptionsDismiss() }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Переименовать") },
                                    onClick = {
                                        viewModel.onEditCategoryDialogShow(category)
                                        viewModel.onCategoryOptionsDismiss()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить") },
                                    onClick = {
                                        user?.uid?.let { viewModel.deleteCategory(it, category) }
                                        viewModel.onCategoryOptionsDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

