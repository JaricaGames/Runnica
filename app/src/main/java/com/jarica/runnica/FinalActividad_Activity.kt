package com.jarica.runnica

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jarica.runnica.Login_Activity.Companion.deporteSeleccionado
import com.jarica.runnica.Login_Activity.Companion.usuarioEmail
import java.text.SimpleDateFormat
import java.util.*

class FinalActividad_Activity : AppCompatActivity() {

    private lateinit var tvTiempo:TextView
    private lateinit var tvDistancia:TextView
    private lateinit var tvRitmoMedio:TextView
    private lateinit var tvFechaActividad: TextView
    private lateinit var tvDiaSemanaActividad: TextView
    private lateinit var btGuardar : Button
    private lateinit var imDeporteSeleccionado : ImageView
    private lateinit var appBarFinalActividad : androidx.appcompat.widget.Toolbar

    //Variables a recibir y enviar
    private var distancia = ""
    private var ritmoMedio = ""
    private var tiempoTranscurrido = ""
    private var diaSemana = ""
    private var diaMes = 0
    private var mes = ""
    private var año = ""
    private var hora = ""
    private var mesNumero = 0
    private lateinit var idCarrera : String
    private lateinit var fechaActividad : String


    private var registroBBDD =  FirebaseFirestore.getInstance()





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_actividad)



        refencias()
        funcionalidades()
        actualizarInterfaz()
    }

    private fun funcionalidades() {

        val bundle = intent.extras
        distancia = bundle?.getString("distancia").toString()
        ritmoMedio = bundle?.getString("ritmoMedio").toString()
        diaSemana = bundle?.getString("diaSemana").toString()
        diaMes = bundle?.getInt("diaMes")!!
        mes = bundle?.getString("mes").toString()
        año = bundle?.getString("año").toString()
        hora = bundle?.getString("hora").toString()
        mesNumero = bundle?.getInt("MesNumero")!!
        tiempoTranscurrido = bundle?.getString("tiempotranscurrido").toString()




        var mesNumeroFormato = ""
        var diaMesFormato = ""
        diaMesFormato = String.format("%02d",diaMes)
        mesNumeroFormato = String.format("%02d",mesNumero)

        fechaActividad = año+mesNumeroFormato+diaMesFormato
        idCarrera = usuarioEmail+año+mesNumeroFormato+diaMesFormato+hora

                btGuardar.setOnClickListener {
            crearRegistroBBDD()
        }

        //Codigo boton felcha tras del actionBar
        var im = appBarFinalActividad.get(0)
        im.setOnClickListener{
            onBackPressed()
        }

    }

    private fun crearRegistroBBDD() {



            registroBBDD.collection("Actividades").document(idCarrera).set(
                hashMapOf(
                    "usuario" to usuarioEmail,
                    "deporte" to deporteSeleccionado,
                    "distancia" to distancia,
                    "ritmoMedio" to ritmoMedio,
                    "diaSemana" to diaSemana,
                    "diaMes" to diaMes,
                    "mes" to mes,
                    "hora" to hora,
                    "tiempoTranscurrido" to tiempoTranscurrido,
                    "fechaActividad" to fechaActividad

                ))


        /*if(deporteSeleccionado == "Walk"){

            registroBBDD.collection("carreras_andando").document(idCarrera).set(
                hashMapOf(
                    "usuario" to usuarioEmail,
                    "deporte" to deporteSeleccionado,
                    "distancia" to distancia,
                    "ritmoMedio" to ritmoMedio,
                    "diaSemana" to diaSemana,
                    "diaMes" to diaMes,
                    "mes" to mes,
                    "hora" to hora,
                    "tiempoTranscurrido" to tiempoTranscurrido,
                    "fechaActividad" to fechaActividad

                ))
        }

        if(deporteSeleccionado == "Bike"){

            registroBBDD.collection("carreras_bici").document(idCarrera).set(
                hashMapOf(
                    "usuario" to usuarioEmail,
                    "deporte" to deporteSeleccionado,
                    "distancia" to distancia,
                    "ritmoMedio" to ritmoMedio,
                    "diaSemana" to diaSemana,
                    "diaMes" to diaMes,
                    "mes" to mes,
                    "hora" to hora,
                    "tiempoTranscurrido" to tiempoTranscurrido

                ))
        }*/

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun actualizarInterfaz() {

        actualizarCronometro()
        tvDistancia.text = roundNumber(distancia,2)
        tvRitmoMedio.text = roundNumber(ritmoMedio, 2)
        tvTiempo.text = tiempoTranscurrido
        tvFechaActividad.text = diaMes.toString() + " de " + mes + " de " + año + " a las " + hora
        when (diaSemana){
            "MONDAY" -> tvDiaSemanaActividad.text = "LUNES"
            "TUESDAY" -> tvDiaSemanaActividad.text = "MARTES"
            "WEDNESDAY" -> tvDiaSemanaActividad.text = "MIERCOLES"
            "THURSDAY" -> tvDiaSemanaActividad.text = "JUEVES"
            "FRIDAY" -> tvDiaSemanaActividad.text = "VIERNES"
            "SATURDAY" -> tvDiaSemanaActividad.text = "SABADO"
            "SUNDAY" -> tvDiaSemanaActividad.text = "DOMINGO"
        }

        diaSemana = tvDiaSemanaActividad.text as String

        if(deporteSeleccionado == "Running") imDeporteSeleccionado.setImageResource(R.drawable.ic_run)
        if(deporteSeleccionado == "Walk") imDeporteSeleccionado.setImageResource(R.drawable.ic_walk)
        if(deporteSeleccionado == "Bike") imDeporteSeleccionado.setImageResource(R.drawable.ic_bike)

    }

    private fun refencias() {

        tvTiempo = findViewById(R.id.tvTiempoFA) // FA ----> FINAL ACTIVITY
        tvDistancia = findViewById(R.id.tvDistanciaFA)
        tvRitmoMedio = findViewById(R.id.tvRitmoFA)
        tvFechaActividad = findViewById(R.id.tvfechaRegistro)
        tvDiaSemanaActividad = findViewById(R.id.tvdiaRegistro)
        btGuardar = findViewById(R.id.btnGuardar)
        imDeporteSeleccionado = findViewById(R.id.iviconoActividad)
        appBarFinalActividad = findViewById(R.id.actionbar)
    }

    private fun actualizarCronometro() {
        //tvTiempo.text = String.format("%02d",horas)+ ":" + String.format("%02d",minutos) + ":" + String.format("%02d",segundos)
    }

    //Metodo que redondea los datos calculados
    private fun roundNumber(data: String, decimals: Int) : String{
        var d : String = data
        var p= d.indexOf(".", 0)

        if (p != null){
            var limit: Int = p+decimals +1
            if (d.length <= p+decimals+1) limit = d.length //-1
            d = d.subSequence(0, limit).toString()
        }

        return d
    }

    //Metodo que al darle atras nos envia a la pantalla principal del movil
    override fun onBackPressed() {

            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

    }

}