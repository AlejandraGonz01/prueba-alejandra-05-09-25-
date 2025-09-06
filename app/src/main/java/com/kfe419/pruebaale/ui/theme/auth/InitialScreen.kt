package com.kfe419.pruebaale.ui.theme.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kfe419.pruebaale.ui.theme.model.UserModel
import com.kfe419.pruebaale.ui.theme.navigation.AppScreens

@Composable
fun InitialScreen(navHostController: NavHostController, auth: FirebaseAuth) {
    val context = LocalContext.current

    val googleSignOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1079552279416-1f8c4psg262vvn5jl4fdckl8j29aompq.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignOptions)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            val firestore = Firebase.firestore
                            val userRef = firestore.collection("users").document(user.uid)

                            val userModel = UserModel(
                                uid = user.uid,
                                name = user.displayName ?: "Sin nombre",
                                email = user.email ?: ""
                            )

                            userRef.get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    userRef.set(userModel, SetOptions.merge())
                                }
                            }
                        }

                        Toast.makeText(context, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                        navHostController.navigate(AppScreens.NotasScreen.route) {
                            popUpTo(AppScreens.InitialScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        Toast.makeText(context, "Algo salió mal", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Algo salió mal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navHostController.navigate(AppScreens.SignUpScreen.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Continuar con Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                auth.signOut()
                auth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: return@addOnSuccessListener
                        val userModel = UserModel(
                            uid = uid,
                            name = "Invitado",
                            email = ""
                        )
                        val docRef = Firebase.firestore.collection("users").document(uid)
                        docRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                docRef.set(userModel)
                            }
                            navHostController.navigate(AppScreens.NotasScreen.route) {
                                popUpTo(AppScreens.InitialScreen.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al iniciar como invitado", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Seguir como invitado")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { navHostController.navigate(AppScreens.LoginScreen.route) }
        ) {
            Text("Ya tengo cuenta")
        }
    }
}
