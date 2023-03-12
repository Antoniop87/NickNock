package br.com.nicknock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import br.com.nicknock.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            goPostActivity()
        }
        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Email ou Senha invalidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                binding.btnLogin.isEnabled = true
               if(task.isSuccessful){
                   Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                   goPostActivity()
               } else{
                   Log.i("ifelse", "signWithEmail failed", task.exception)
                   Toast.makeText(this, "Autenticação falhou", Toast.LENGTH_SHORT).show()
               }
            }
        }
    }

    private fun goPostActivity() {
        Log.i("this", "goPostActivity")
        val intent = Intent(this, PostsActitivy::class.java)
        startActivity(intent)
        finish()
    }
}