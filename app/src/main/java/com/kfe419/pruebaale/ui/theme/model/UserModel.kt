package com.kfe419.pruebaale.ui.theme.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

class UserViewModel : ViewModel() {

    private val _user = MutableStateFlow(UserModel())
    val user: StateFlow<UserModel> = _user.asStateFlow()

    fun saveUser(user: UserModel) {
        _user.value = user
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reloadUser(uid: String, onFinish: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val doc = Firebase.firestore.collection("users").document(uid).get().await()
                doc.toObject(UserModel::class.java)?.let {
                    _user.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onFinish?.invoke()
            }
        }
    }
}
