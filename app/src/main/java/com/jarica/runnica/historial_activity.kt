package com.jarica.runnica

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jarica.runnica.Login_Activity.Companion.usuarioEmail
import java.util.ArrayList

class historial_activity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayListActividades: ArrayList<Actividades>
    private lateinit var miAdapter: Adaptador

    private lateinit var appBarHistorial : androidx.appcompat.widget.Toolbar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)



        recyclerView = findViewById(R.id.rvHistorial)
        appBarHistorial = findViewById(R.id.actionbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)


        arrayListActividades = arrayListOf()
        miAdapter = Adaptador(arrayListActividades)
        recyclerView.adapter = miAdapter




        //Codigo boton felcha tras del actionBar
        var im = appBarHistorial.get(0)
        im.setOnClickListener{
            onBackPressed()
        }


    }

    private fun cargarDatosBBDD(campo: String, order: Query.Direction) {

        var collection = "Actividades"

        arrayListActividades.clear()
        var conectorBBDD = FirebaseFirestore.getInstance()
        conectorBBDD.collection(collection).orderBy(campo, order)
            .whereEqualTo("usuario", usuarioEmail)
            .get()
            .addOnSuccessListener { documento ->
                for (actividad in documento){
                    arrayListActividades.add(actividad.toObject(Actividades::class.java))
                }
                miAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error:", exception)
            }


    }

    override fun onResume() {
        super.onResume()
        cargarDatosBBDD("fechaActividad", Query.Direction.DESCENDING)
    }



    //Metodo que al darle atras nos envia a la pantalla principal del movil
    override fun onBackPressed() {

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

}






