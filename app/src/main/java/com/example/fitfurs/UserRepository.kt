package com.example.fitfurs


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserData(
    val username: String = "",
    val phone: String = "",
    val email: String = "",
    val secondaryEmail: String = ""
)

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun userDoc(userId: String) = db.collection("users").document(userId)

    suspend fun getUserData(userId: String): UserData? {
        val snapshot = userDoc(userId).get().await()
        return snapshot.toObject(UserData::class.java)
    }

    suspend fun updateField(userId: String, field: String, value: String) {
        userDoc(userId).update(field, value).await()
    }
}
