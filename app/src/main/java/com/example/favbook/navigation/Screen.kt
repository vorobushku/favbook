package com.example.favbook.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth_screen")
    data object Main : Screen("main_screen")
    data object Book : Screen("book_screen")
    data object Search : Screen("search_screen")
    data object Add : Screen("add_screen")
    data object BookDetail : Screen("book_detail_screen")
    data object CategoryBooks : Screen("category_books_screen/{category}")
    data object AuthorBooks : Screen("author_books_screen/{author}")

    companion object {
        val bottomBarScreens = listOf(
            Main.route, Book.route, Search.route, Add.route, BookDetail.route,
            CategoryBooks.route, AuthorBooks.route)
    }
}
