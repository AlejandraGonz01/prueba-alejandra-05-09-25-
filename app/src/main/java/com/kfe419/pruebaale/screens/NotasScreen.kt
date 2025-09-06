package com.kfe419.pruebaale.screens

import android.widget.Toast
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kfe419.pruebaale.ViewModel.Note
import com.kfe419.pruebaale.ViewModel.NotesViewModel
import com.kfe419.pruebaale.ViewModel.NotesViewModelFactory
import com.kfe419.pruebaale.navigation.AppScreens

@Composable
fun NotasScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    viewModel: NotesViewModel = viewModel(factory = NotesViewModelFactory(auth, firestore))
) {
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var noteText by remember { mutableStateOf("") }

    val uid = viewModel.getUserUid()
    if (uid == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Escribe una nota") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (noteText.isBlank()) return@Button
                viewModel.addNote(noteText) { success ->
                    if (success) {
                        noteText = ""
                        Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al guardar nota", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar Nota") }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            notes.isEmpty() -> Text("Aún no hay notas")
            else -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(notes) { note ->
                    NoteItem(note, viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LogoutButton(
            navHostController = navController,
            auth = auth,
            webClientId = "1079552279416-1f8c4psg262vvn5jl4fdckl8j29aompq.apps.googleusercontent.com"
        )
    }
}

@Composable
fun NoteItem(note: Note, viewModel: NotesViewModel) {
    val context = LocalContext.current
    val date = note.timestamp?.toDate()
    val formattedDate = date?.let {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("America/Mexico_City")
        sdf.format(it)
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = note.text)
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = {
            viewModel.deleteNote(note) { success ->
                if (success) {
                    Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al eliminar nota", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar nota")
        }
    }
    Divider()
}

@Composable
fun LogoutButton(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    webClientId: String
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val user = auth.currentUser
            val uid = user?.uid

            if (user != null && user.isAnonymous && uid != null) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .delete()
                    .addOnSuccessListener {
                        user.delete()
                            .addOnSuccessListener {
                                signOut(context, webClientId) {
                                    Toast.makeText(context, "Invitado eliminado y sesión cerrada", Toast.LENGTH_SHORT).show()
                                    navHostController.navigate(AppScreens.InitialScreen.route) {
                                        popUpTo(AppScreens.NotasScreen.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                    }
            } else {
                signOut(context, webClientId) {
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    navHostController.navigate(AppScreens.InitialScreen.route) {
                        popUpTo(AppScreens.NotasScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cerrar sesión")
    }
}

fun signOut(context: Context, webClientId: String, onComplete: () -> Unit) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    googleSignInClient.signOut().addOnCompleteListener {
        FirebaseAuth.getInstance().signOut()
        onComplete()
    }
}
