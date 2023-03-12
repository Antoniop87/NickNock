package br.com.nicknock

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import br.com.nicknock.databinding.ActivityCreateBinding
import br.com.nicknock.models.Post
import br.com.nicknock.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*

private const val PICK_PHOTO_CODE = 1234

class CreateActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private val binding by lazy {
        ActivityCreateBinding.inflate(layoutInflater)
    }


    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        storageReference = FirebaseStorage.getInstance().reference

        firestoreDb = FirebaseFirestore.getInstance()
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get().addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.e("Post", "teste da profile: ${signedInUser}")
            }.addOnFailureListener { exception ->
                Log.e("Post", "Falha no usuario", exception)
            }

        binding.btnPickImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null){
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }

    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null){
            Toast.makeText(this, "Foto não selecionada", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.etDescription.text.isBlank()){
            Toast.makeText(this, "A descrição não pode ser vazia", Toast.LENGTH_SHORT).show()
            return
        }

        if(signedInUser == null){
            Toast.makeText(this, "nenhum usuario conectado", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")

        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis().toDouble(),
                    signedInUser
                )
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful){
                    Toast.makeText(this, "Falha ao salvar o post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                val profileintent = Intent(this, ProfileActivity::class.java)
                profileintent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileintent)
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE){
            if (resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                Log.e("photo", "photouri: $photoUri")
                imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

}