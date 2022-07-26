package com.jarica.runnica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Olvido_Activity : AppCompatActivity() {

    //variable Firebase
    private lateinit var auth: FirebaseAuth

    //Variables del layout
    private lateinit var etEmail:EditText
    private lateinit var btNuevaContrasena:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_olvido)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //Pillo las referencias del layout
        referencias()

        //Implanto funcionalidades
        funcionalidades()
    }

    private fun funcionalidades() {

        btNuevaContrasena.setOnClickListener {

            val email = etEmail.text.toString()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Contraseña enviada a $email", Toast.LENGTH_SHORT).show()
                        goHome()
                    } else {
                        Toast.makeText(baseContext, "Usuario no existe", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun goHome() {
        val intento = Intent(this, Login_Activity::class.java)
        startActivity(intento)
    }

    //Metodo que me pilla las referencias del layout
    private fun referencias() {

        etEmail = findViewById(R.id.etEmail)
        btNuevaContrasena = findViewById(R.id.btSolicitaNuevaContraseña)

    }
}
