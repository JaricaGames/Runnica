package com.jarica.runnica

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jarica.runnica.Login_Activity.Companion.iconoUsuario
import com.jarica.runnica.Login_Activity.Companion.nombreUsuario
import com.jarica.runnica.Login_Activity.Companion.providerSesion
import com.jarica.runnica.Login_Activity.Companion.usuarioEmail
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    //Variables cronómetro
    private var mhandler: Handler? = null
    private var tiempotranscurrido: Long = -1
    private var botonPlayPulsado = false
    private var segundos = -1
    private var minutos = 0
    private var horas = 0

    //Variables fecha

    private var mesesAño = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    private var mesActividad = ""
    private var diaSemanaActividad = ""
    private var diaMesActividad = ""
    private var añoActividad = ""
    private var fechaformateada = ""


    //Variables tolllbar
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var toogle: ActionBarDrawerToggle

    //variables NavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var headerView: android.view.View

    //variables texto
    private lateinit var tvTiempo: TextView
    private lateinit var tvDistancia: TextView
    private lateinit var tvVelocidad: TextView
    private lateinit var tvRitmo: TextView
    private lateinit var tvUser: TextView
    private lateinit var sivIcon: Uri
    private lateinit var deporteSeleccionado : String

    //Variables FloatingACtion
    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabPause: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton
    private lateinit var fabCandadoAbierto: FloatingActionButton
    private lateinit var fabCandadoCerrado: FloatingActionButton

    //variableGPS
    companion object{
        val REQUIRED_PERMISSIONS_GPS =
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
    }

    //Variables de posicionamiento
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isGPSActive:Boolean = true
    private val PERMISSION_ID = 42
    private val INTERVALO_LOCALIZACION = 4

    private var flagCapturarLocalizacion = false // al ser lastlocation, la primera localizacion hay que desecharla

    //Variables del mapa
    private lateinit var miMapa: GoogleMap
    private var mapaCentrado = true
    private lateinit var listaPuntos : Iterable<LatLng>

    //Variables de mediciones
    private var latitud:Double = 0.0
    private var longitud:Double = 0.0
    private var long_inicial:Double = 0.0
    private var lat_inicial:Double = 0.0

    private var velocidad:Double = 0.0
    private var distancia:Double = 0.0
    private var ritmoMedio:Double = 0.0


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        referencias()
        iniciadorObjetos()
        initNavigationView()
        iniciarPermisosGPS()

    }

    /**************INICIO DE OBJETOS*************/

    //Metodo que inicia todos los objetos

    //Metodo que pilla las referencias del layout
    private fun referencias() {

        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.actionbar)
        drawer = findViewById(R.id.drawer_layout)
        tvTiempo = findViewById(R.id.tvTiempo)
        tvDistancia = findViewById(R.id.tvDistancia)
        tvVelocidad = findViewById(R.id.tvVelocidad)
        tvRitmo = findViewById(R.id.tvRitmo)
        fabPlay = findViewById(R.id.fabPlay)
        fabStop = findViewById(R.id.fabStop)
        fabPause = findViewById(R.id.fabPause)
        fabCandadoAbierto = findViewById(R.id.fabCandadoabierto)
        fabCandadoCerrado = findViewById(R.id.fabCandadocerrado)

    }

    //Metodo que inicia los objetos
    @RequiresApi(Build.VERSION_CODES.O)
    private fun iniciadorObjetos() {

        setSupportActionBar(toolbar) // iniciar tollbar
        tvTiempo.text = "00:00:00"
        tvDistancia.text = "0,00"
        tvVelocidad.text = "0,00"
        tvRitmo.text = "0,00"
        deporteSeleccionado = "Running"

        fabPlay.setOnClickListener() {
            administrarPlayPausa()
            calcularFecha()
        }

        fabCandadoCerrado.setOnClickListener(){
            fabCandadoCerrado.hide()
            fabCandadoAbierto.show()
            fabPause.show()
            fabStop.show()
        }

        fabCandadoAbierto.setOnClickListener(){
            fabCandadoCerrado.show()
            fabStop.hide()
            fabPause.hide()
        }

        fabPause.setOnClickListener(){
            pararTiempo()
            fabPlay.show()
            fabPause.hide()
            fabCandadoCerrado.hide()
            fabCandadoAbierto.hide()
        }

        fabStop.setOnClickListener(){
            botonPlayPulsado = false
            mapaCentrado = true
            terminarCarreraYReiniciarPantalla()
            mhandler?.removeCallbacks(cronometro)
            fabStop.hide()
            fabPause.hide()
            fabCandadoAbierto.hide()
            fabPlay.show()

        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        listaPuntos = arrayListOf()
        (listaPuntos as ArrayList<LatLng>).clear()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularFecha(){
        var formatoFechaInicioActividad = DateTimeFormatter.ofPattern("HH:mm")
        var fecha = LocalDateTime.now()
        fechaformateada = fecha.format(formatoFechaInicioActividad)
        diaMesActividad = fecha.dayOfMonth.toString()
        añoActividad = fecha.year.toString()
        mesActividad = mesesAño[fecha.monthValue-1]
        diaSemanaActividad = fecha.dayOfWeek.toString()

    }


    //Metodos que inicia el navigation View y implementa las opciones
    private fun initNavigationView() {

        toogle = ActionBarDrawerToggle(
            this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        navigationView.setNavigationItemSelectedListener(this)
        headerView =
            LayoutInflater.from(this).inflate(R.layout.nav_header_layout, navigationView, false)
        navigationView.addHeaderView((headerView))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_item_CerrarSesion -> Logout()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.seleccionar_deporte, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun clickDeporte(item: MenuItem){
        var intent = Intent(this,seleccionActividad_Activity::class.java)
        startActivity(intent)
    }

    /**************FIN DE INICIO DE OBJETOS*************/
    /*********************************************/
    /**************INICIO DE MANEJO DE INTERFAZ*************/

    //Metodo que al darle atras nos envia a la pantalla principal del movil
    override fun onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else{
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }
    }

    //Metodo que deslogea al usuario y le devuelve a la pantalla de Login
    private fun Logout() {
        FirebaseAuth.getInstance().signOut()
        nombreUsuario = ""
        usuarioEmail = ""
        providerSesion = ""
        iconoUsuario = "".toUri()


        val intento = Intent(this, Login_Activity::class.java)
        startActivity(intento)
    }

    //Comprueba si esta activo o no el GPS antes de iniciar nueva carrera y lanza el cronometro
    private fun administrarPlayPausa() {
        if(tiempotranscurrido == 0L && !isGPSActivo()){
            AlertDialog.Builder(this)
                .setTitle(R.string.GPSTitulo)
                .setMessage(R.string.GPSDescripcion)
                .setPositiveButton(R.string.GPSActivar, DialogInterface.OnClickListener { dialog, which ->
                    activarGPS()
                })
                .setNegativeButton(getString(R.string.GPSIgnorar), DialogInterface.OnClickListener { dialog, which ->
                    isGPSActive = false
                    administrarCarrera()
                })
                .setCancelable(true)
                .show()
        }else{
            administrarCarrera()
        }

    }

    //Metodo que recarga el usuario tras reinicio de activity
    private fun recargarusuario() {


        tvUser = headerView.findViewById(R.id.tvNombreUsuario)
        println(iconoUsuario)

        if (providerSesion == "Google") {
            var sivAvatar: ShapeableImageView = headerView.findViewById(R.id.sivAvatar)
            tvUser.text = nombreUsuario
            Glide.with(this).load(iconoUsuario).into(sivAvatar)
        }

        if (providerSesion == "email") {

            tvUser = headerView.findViewById(R.id.tvNombreUsuario)

        }

        var sivAvatar: ShapeableImageView = headerView.findViewById(R.id.sivAvatar)

        Glide.with(this).load(iconoUsuario).into(sivAvatar)

        var lecturaBBDD = FirebaseFirestore.getInstance()
        lecturaBBDD.collection("usuarios").document(usuarioEmail)
            .get()
            .addOnSuccessListener { document ->
                var usuario = document.toObject(Usuario::class.java)
                tvUser.text = usuario?.apodo
            }

        //println(nombreUsuario)
        //tvUser.text = nombreUsuario

    }

    //Metodo que actuzali los datos de la interfaz
    private fun actualizarDatosInterfaz() {
        tvDistancia.text = roundNumber(distancia.toString(),2)
        tvVelocidad.text = roundNumber(velocidad.toString(), 2)
        tvRitmo.text = roundNumber(ritmoMedio.toString(), 2)

    }

    /**************FIN DE MANEJO DE INTERFAZ*************/
    /*********************************************/
    /**************INICIO DE ZONA DE ADMINISISTRACION DE TIEMPO******/

    //Metodo que inicia el Cronometro
    private fun administrarCarrera(){
        botonPlayPulsado = true
        fabPlay.hide()
        fabCandadoCerrado.show()
        fabStop.hide()
        fabPause.hide()
        mhandler = Handler(Looper.getMainLooper())
        cronometro.run()

        if(isGPSActive){
            flagCapturarLocalizacion = false
            adminitrarLocalizacion()
            flagCapturarLocalizacion = true
            adminitrarLocalizacion()
        }
    }

    //Netodo que para el tiempo del cronómetro
    private fun pararTiempo(){
        botonPlayPulsado = false
        mhandler?.removeCallbacks(cronometro)
    }

    //Metodo que termina la carrera y reinicia la pantalla
    @RequiresApi(Build.VERSION_CODES.O)
    private fun terminarCarreraYReiniciarPantalla() {

        (listaPuntos as ArrayList<LatLng>).clear()



        val intent = Intent(this, FinalActividad_Activity::class.java)
        intent.putExtra("tiempotranscurrido", tvTiempo.text)
        intent.putExtra("distancia", tvDistancia.text)
        intent.putExtra("ritmoMedio", tvRitmo.text)
        intent.putExtra("diaSemana", diaSemanaActividad)
        intent.putExtra("diaMes", diaMesActividad)
        intent.putExtra("mes", mesActividad)
        intent.putExtra("año", añoActividad)
        intent.putExtra("hora", fechaformateada)
        startActivity(intent)

        tiempotranscurrido = 0
        horas = 0
        minutos = 0
        segundos = 0
        // actualizarCronometro()
        isGPSActive = true
        tvDistancia.text = "0,00"
        tvVelocidad.text = "0,00"
        tvRitmo.text = "0,00"
        isGPSActive = true

    }

    //Metodo que actualiza el cronometro
    private fun actualizarCronometro() {
        tvTiempo.text = String.format("%02d",horas)+ ":" + String.format("%02d",minutos) + ":" + String.format("%02d",segundos)
    }

    //Metodos que crean un hilo y su metodo run para administrar el cronometro
    private var cronometro: Runnable = object : Runnable {
        override fun run() {
            try {
                tiempotranscurrido++
                segundos++
                if (segundos > 59) {
                    minutos++
                    segundos = 0
                }
                if (minutos > 59) {
                    horas++
                    minutos = 0
                }

                actualizarCronometro()

                //Parte del metodo run que recoge la ubicacion cada X segundos
                if (isGPSActive && tiempotranscurrido.toInt() % INTERVALO_LOCALIZACION == 0){

                    adminitrarLocalizacion()
                }

            } finally {
                mhandler!!.postDelayed(this, 1000)
            }

        }

    }

    /**************FIN DE ZONA DE ADMINISISTRACION DE TIEMPO*************/
    /*********************************************/
    /**************INICIO DE ZONA DE ADMINISISTRACION DE GPS******/

    // Metodo que comprueba si estan los permisos y si no llama al metodo que solicita los permisos
    private fun iniciarPermisosGPS() {
        if(comprobarPermisos()){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        else{
            solicitarPermisosGPS()
        }
    }

    //Metodo que solicitia los permisis de GPS
    private fun solicitarPermisosGPS() {
        ActivityCompat.requestPermissions(this,
            REQUIRED_PERMISSIONS_GPS, PERMISSION_ID)

    }

    //Metodo que nos manda a la ventana del sistema para activar el GPS
    private fun activarGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    //Metodo que comprueba si el GPS esta actuivo
    private fun isGPSActivo(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //Metodo que comprueba los permisos
    private fun comprobarPermisos() = REQUIRED_PERMISSIONS_GPS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //Metodo que administra la localizacion del movil
    @SuppressLint("MissingPermission")
    private fun adminitrarLocalizacion() {

        if (comprobarPermisos()) {

            if (isGPSActivo()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    solicitarNuevaLocalizacion()
                }
            } else {
                activarGPS()
            }
        } else {
            solicitarPermisosGPS()

        }
    }

    //Metodo que solicita una nueva localizacion
    @SuppressLint("MissingPermission")
    private fun solicitarNuevaLocalizacion() {

        var miSolicitudLocalizacion = com.google.android.gms.location.LocationRequest()
        miSolicitudLocalizacion.interval = 4000
        miSolicitudLocalizacion.fastestInterval = 0
        miSolicitudLocalizacion.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(miSolicitudLocalizacion, miLocalizacionCallBack, Looper.getMainLooper() )

    }

    //Variable y metodo que crea el callBack para llamar recursivamente a una nueva localizacion
    private val miLocalizacionCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var miUltimaLocalizacion : Location? = locationResult.lastLocation

            if (miUltimaLocalizacion != null) {
                lat_inicial = miUltimaLocalizacion.latitude
            }
            if (miUltimaLocalizacion != null) {
                long_inicial = miUltimaLocalizacion.longitude
            }

            if(tiempotranscurrido > 0L){
                if (miUltimaLocalizacion != null) {
                    registerNewLocation(miUltimaLocalizacion)
                }


            }
        }
    }

    //Metodo que registra la nueva ubicacion
    private fun registerNewLocation(location: Location){
        var new_latitude: Double = location.latitude
        var new_longitude: Double = location.longitude

        if (flagCapturarLocalizacion){
            if (tiempotranscurrido >= INTERVALO_LOCALIZACION){
                var distanciaIntervalo = calcularDistancia(new_latitude, new_longitude)
                calcularVelocidad(distanciaIntervalo)
                actualizarDatosInterfaz()
                calcularRitmo()

                //Dicujado de la polilinea
                var nuevaPosicion = LatLng(new_latitude,new_longitude)
                (listaPuntos as ArrayList<LatLng>).add(nuevaPosicion)
                crearPolilinea(listaPuntos)


            }
        }
        latitud = new_latitude
        longitud = new_longitude

        if (mapaCentrado == true) centrarMapa(latitud, longitud)
    }

    //Metodo que dibuja la polilinea del mapa
    private fun crearPolilinea(listaPuntos: Iterable<LatLng>) {
        val polilineaOpciones = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this,R.color.azul_claro_gradient))
            .addAll(listaPuntos)

        var polilinea = miMapa.addPolyline(polilineaOpciones)
        polilinea.startCap = RoundCap()

    }

    /**************FIN DE ZONA DE ADMINISISTRACION DE GPS*************/
    /*********************************************/
    /**************INICIO DE ZONA DE ADMINISISTRACION DE google MAPS*****/

    //Metodo que es llamado una vez se inicia el maps
    override fun onMapReady(googleMap: GoogleMap) {

        miMapa = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        miMapa.setOnMyLocationButtonClickListener (this)
        miMapa.setOnMyLocationClickListener(this)
        miMapa.setOnMapLongClickListener { mapaCentrado = false }
        miMapa.setOnMapClickListener {mapaCentrado = false}

        activarMiLocalizacion()
        ubicacionDispositivo()

    }

    //Metodo que coje la ubicacion del dispositivo para centrarlo al comienzo de la actividad
    @SuppressLint("MissingPermission")
    private fun ubicacionDispositivo() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(comprobarPermisos()){
            if(isGPSActive){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var localizacion = task.result
                    if(localizacion!=null){
                        centrarMapa(localizacion.latitude, localizacion.longitude)
                    }
                }
            }

        }


    }

    //Metodo qiue activa la locaclizacion en el mapo
    private fun activarMiLocalizacion() {

        if (!::miMapa.isInitialized) return
        if (ActivityCompat.checkSelfPermission(  this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED

            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            solicitarPermisosGPS()
            return
        }
        else{
            miMapa.isMyLocationEnabled = true
        }


    }

    //Metodo que centra el mapa en la posicion del movil
    private fun centrarMapa(latInicial: Double, longInicial: Double) {
        val posicionMapa = LatLng(latInicial, longInicial)
        miMapa.animateCamera(CameraUpdateFactory.newLatLngZoom(posicionMapa, 18f), 1000, null)

    }

    //Metodo que crean los botones de centrado
    override fun onMyLocationButtonClick(): Boolean {
        mapaCentrado = true
        return false
    }
    override fun onMyLocationClick(p0: Location) {
        mapaCentrado = true
    }


    /**************FIN DE ZONA DE ADMINISISTRACION DE google MAPS*************/
    /*********************************************/
    /**************INICIO DE ZONA DE CALCULOS*****/

    //Metodo que calcula la velocidad
    private fun calcularVelocidad(distanciaIntervalo: Double) {

        velocidad = ((distanciaIntervalo * 1000) / INTERVALO_LOCALIZACION) * 3.6 //Para pasar a KM/h
    }

    //Metodo que calcula el ritmo de la carrera
    private fun calcularRitmo() {
        ritmoMedio = tiempotranscurrido/ (distancia*60)
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

    //Metodo que calcula la distancia recorrida
    private fun calcularDistancia(n_lt: Double, n_lg: Double): Double{
        val radioTierra = 6371.0 //en kilómetros

        val dLat = Math.toRadians(n_lt - latitud)
        val dLng = Math.toRadians(n_lg - longitud)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val va1 =
            Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                    * Math.cos(Math.toRadians(latitud)) * Math.cos(
                Math.toRadians( n_lt  )
            ))
        val va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1))
        var n_distance =  radioTierra * va2


        distancia += n_distance
        return n_distance
    }



    //Metodo onStart
    override fun onStart() {
        super.onStart()
        recargarusuario()

    }



}

