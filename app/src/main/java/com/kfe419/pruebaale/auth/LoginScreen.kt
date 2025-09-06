package com.kfe419.pruebaale.auth

import com.kfe419.pruebaale.navigation.AppScreens
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.kfe419.pruebaale.R

@Composable
fun LoginScreen(navHostController: NavHostController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var forgotPasswordDialogBox by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) R.drawable.eyevisi else R.drawable.eyeoff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = description,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            navHostController.navigate(AppScreens.NotasScreen.route) {
                                popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                task.exception?.message ?: "Error al iniciar sesión",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { forgotPasswordDialogBox = true }) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                navHostController.navigate(AppScreens.SignUpScreen.route)
            }
        ) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }

    if (forgotPasswordDialogBox) {
        var resetEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { forgotPasswordDialogBox = false },
            title = { Text("Recuperar contraseña") },
            text = {
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isNotBlank()) {
                        Firebase.auth.sendPasswordResetEmail(resetEmail)
                            .addOnCompleteListener { task ->
                                Toast.makeText(
                                    context,
                                    if (task.isSuccessful) "Revisa tu email" else "Email no encontrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                forgotPasswordDialogBox = false
                            }
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { forgotPasswordDialogBox = false }) { Text("Cancelar") }
            }
        )
    }
}
