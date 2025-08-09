@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.orgnotes.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.orgnotes.core.NotesViewModel
import com.example.orgnotes.ui.edit.NoteEditorScreen
import com.example.orgnotes.ui.list.NotesListScreen

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val vm: NotesViewModel = viewModel() // ← один общий VM на всё приложение

    NavHost(navController = nav, startDestination = "list") {
        composable("list") {
            NotesListScreen(
                onAdd = { nav.navigate("edit") },
                onOpen = { id -> nav.navigate("edit?id=$id") },
                vm = vm
            )
        }
        composable(
            route = "edit?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
        ) { backStack ->
            val id = backStack.arguments?.getString("id")
            NoteEditorScreen(noteId = id, onDone = { nav.popBackStack() }, vm = vm)
        }
        composable("edit") {
            NoteEditorScreen(noteId = null, onDone = { nav.popBackStack() }, vm = vm)
        }
    }
}
