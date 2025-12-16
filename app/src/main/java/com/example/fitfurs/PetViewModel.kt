import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PetViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _weight = MutableStateFlow<Float?>(null)
    val weight: StateFlow<Float?> = _weight

    fun fetchPetWeight(petId: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("pets").document(petId).get().await()
                val w = doc.getDouble("weight")?.toFloat()
                _weight.value = w
            } catch (e: Exception) {
                e.printStackTrace()
                _weight.value = null
            }
        }
    }
}
