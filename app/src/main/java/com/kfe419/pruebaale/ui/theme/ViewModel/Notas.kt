package com.kfe419.pruebaale.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Note(
    val id: String = "",
    val text: String = "",
    val uid: String = "",
    val timestamp: Timestamp? = null
)

class NotesViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val uid = auth.currentUser?.uid

    init {
        if (uid != null) {
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    _loading.value = false
                    if (error != null) return@addSnapshotListener
                    _notes.value = snapshot?.documents
                        ?.mapNotNull { doc ->
                            doc.toObject(Note::class.java)?.copy(id = doc.id)
                        } ?: emptyList()
                }
        } else {
            _loading.value = false
        }
    }

    fun addNote(text: String, onComplete: (Boolean) -> Unit) {
        val uid = this.uid ?: return
        val note = Note(text = text, uid = uid, timestamp = Timestamp.now())
        firestore.collection("users")
            .document(uid)
            .collection("notes")
            .add(note)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteNote(note: Note, onComplete: (Boolean) -> Unit) {
        val uid = this.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("notes")
            .document(note.id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUserUid() = uid
}

class NotesViewModelFactory(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotesViewModel(auth, firestore) as T
    }
}
