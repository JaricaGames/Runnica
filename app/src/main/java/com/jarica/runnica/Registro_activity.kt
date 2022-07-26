package com.jarica.runnica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jarica.runnica.Login_Activity.Companion.nombreUsuario
import com.jarica.runnica.Login_Activity.Companion.providerSesion
import com.jarica.runnica.Login_Activity.Companion.usuarioEmail
import java.text.SimpleDateFormat
import java.util.*



class Registro_activity : AppCompatActivity() {

    //Variable Firebase
    private lateinit var auth: FirebaseAuth

    //Variables del layout
    private lateinit var btRegistrate:Button
    private lateinit var etNombreUsuario:EditText
    private lateinit var etEmail:EditText
    private lateinit var etContraseña:EditText
    private lateinit var etRepiteContraseña:EditText
    private lateinit var tvTerminosYCondiciones:TextView
    private lateinit var cbTerminosYCondiciones: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //pillo referencias
        referencias()

        //Establezco funcioanlidades
        funcionalidades()

        comprobarCamposdeTexto()

    }


    // Metodo que crea un nuevo usuario y lo mete en la coleccion de FireBase usuarios
    private fun nuevoregistro() {

        var email = etEmail.text.toString()
        var password = etContraseña.text.toString()
        var passwordRepetida = etRepiteContraseña.text.toString()
        var user = etNombreUsuario.text.toString()

        if (!email.isEmpty() || !password.isEmpty() || !passwordRepetida.isEmpty() || !user.isEmpty()){

            if(password.equals(passwordRepetida)){

                if(password.length>5){

                    if(cbTerminosYCondiciones.isChecked){
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    registrarBBDD(email, user)
                                    usuarioEmail = email
                                    providerSesion = "email"
                                    nombreUsuario = user

                                    goHome()
                                } else {
                                    Toast.makeText(this, "naaada", Toast.LENGTH_SHORT).show()
                                }

                            }
                    }else{
                        Toast.makeText(this, R.string.terminosycondiciones, Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this, R.string.contrasediferente, Toast.LENGTH_SHORT).show()
                }


            }else{
                Toast.makeText(this, R.string.contrase6caracteres, Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, R.string.faltandatos, Toast.LENGTH_SHORT).show()
        }





    }

    //Metodo que inserta en la BBDD el usuario nuevo
    private fun registrarBBDD(email: String, user: String) {
        var fechaRegistro = SimpleDateFormat("dd/MM/yyyy").format(Date())
        var registroBBDD = FirebaseFirestore.getInstance()
        registroBBDD.collection("usuarios").document(email).set(hashMapOf(
            "apodo" to user,
            "fechaRegistro" to fechaRegistro,
            "emailUsuario" to email,
            "registro" to "email",
        ))
    }

    //Metodo que envio a la pagina principal
    private fun goHome() {
        val intento = Intent(this, MainActivity::class.java)
        startActivity(intento)
    }


    //Metodo donde se pillan todas las referencias del layout
    private fun referencias() {

        btRegistrate = findViewById(R.id.btRegistrate)
        etNombreUsuario = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        etContraseña = findViewById(R.id.etContraseña)
        etRepiteContraseña = findViewById(R.id.etRepiteContraseña)
        tvTerminosYCondiciones = findViewById(R.id.tvTerminosYCondiciones)
        cbTerminosYCondiciones = findViewById(R.id.cbcondiciones)

    }

    private fun funcionalidades() {



        //Boton de registrarse
        btRegistrate.setOnClickListener{
            nuevoregistro()
        }

        //Intent terminos y condiciones de uso
        tvTerminosYCondiciones.setOnClickListener{
            val intento = Intent(this, TerminosYCondiciones::class.java)
            startActivity(intento)
        }

        //EditText Email
        etEmail.addTextChangedListener {
            if (etEmail.text.toString().isEmpty()) etEmail.error = ""
        }

        //EditText Usuario
        etNombreUsuario.addTextChangedListener {
            if (etNombreUsuario.text.toString().isEmpty()) etNombreUsuario.error = ""
        }

        //EditText contraseña
            etContraseña.addTextChangedListener {
            if (etContraseña.text.toString().isEmpty()) etContraseña.error = ""
        }

        //EditText Nueva Contraseña
        etRepiteContraseña.addTextChangedListener {
            if (etRepiteContraseña.text.toString().isEmpty()) etRepiteContraseña.error = ""
        }




    }

    private fun comprobarCamposdeTexto() {
        if (etEmail.text.toString().isEmpty()) etEmail.error = ""
        if (etNombreUsuario.text.toString().isEmpty()) etNombreUsuario.error = ""
        if (etContraseña.text.toString().isEmpty()) etContraseña.error = ""
        if (etRepiteContraseña.text.toString().isEmpty()) etRepiteContraseña.error = ""
    }
}