package com.example.pc2moviles2210178022101523

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.pc2moviles2210178022101523.ui.theme.PC2MOVILES2210178022101523Theme
import com.google.firebase.firestore.FirebaseFirestore

// --- MODELO DE DATOS ---
data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val anioFundacion: String = "",
    val titulos: String = "",
    val urlImagen: String = ""
)

// --- VIEWMODEL (Lógica de Firebase Firestore) ---
class EquiposViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    // Estado observable para la lista
    var listaEquipos = mutableStateOf<List<Equipo>>(listOf())

    // Función para REGISTRAR (Requisito Pantalla 1)
    fun guardarEquipo(equipo: Equipo, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val nuevoEquipo = hashMapOf(
            "nombre" to equipo.nombre,
            "anioFundacion" to equipo.anioFundacion,
            "titulos" to equipo.titulos,
            "urlImagen" to equipo.urlImagen
        )

        db.collection("equipos") // Nombre de la colección en Firestore
            .add(nuevoEquipo)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
    }

    // Función para LISTAR (Requisito Pantalla 2)
    fun obtenerEquipos() {
        db.collection("equipos")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val lista = ArrayList<Equipo>()
                for (doc in value!!) {
                    lista.add(Equipo(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        anioFundacion = doc.getString("anioFundacion") ?: "",
                        titulos = doc.getString("titulos") ?: "",
                        urlImagen = doc.getString("urlImagen") ?: ""
                    ))
                }
                listaEquipos.value = lista
            }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita pantalla completa
        setContent {
            // Usamos tu tema original
            PC2MOVILES2210178022101523Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pasamos el padding al navegador para evitar que la barra tape contenido
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// --- NAVEGACIÓN PRINCIPAL ---
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // ViewModel compartido entre pantallas
    val viewModel: EquiposViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "registro_screen",
        modifier = modifier
    ) {
        composable("registro_screen") {
            RegistroScreen(navController, viewModel)
        }
        composable("listado_screen") {
            ListadoScreen(navController, viewModel)
        }
    }
}

// --- PANTALLA 1: REGISTRO UI [cite: 3, 4, 6] ---
@Composable
fun RegistroScreen(navController: NavController, viewModel: EquiposViewModel) {
    var nombre by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var titulos by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Registro Liga 1",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = anio,
            onValueChange = { anio = it },
            label = { Text("Año de fundación") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = titulos,
            onValueChange = { titulos = it },
            label = { Text("Número de títulos ganados") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL de la imagen del equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

// Botón Guardar [cite: 5]
        Button(
            onClick = {
                if(nombre.isNotEmpty() && anio.isNotEmpty()) {
                    val equipo = Equipo(nombre = nombre, anioFundacion = anio, titulos = titulos, urlImagen = url)
                    viewModel.guardarEquipo(equipo,
                        onSuccess = {
                            Toast.makeText(context, "Equipo Registrado", Toast.LENGTH_SHORT).show()
                            navController.navigate("listado_screen")
                        },
                        onFailure = {
                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}

// --- PANTALLA 2: LISTADO UI [cite: 18] ---
@Composable
fun ListadoScreen(navController: NavController, viewModel: EquiposViewModel) {
    // Cargar datos al entrar a la pantalla
    LaunchedEffect(Unit) { viewModel.obtenerEquipos() }

    val equipos = viewModel.listaEquipos.value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Equipos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(equipos) { equipo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen remota usando Coil [cite: 9]
                        AsyncImage(
                            model = equipo.urlImagen,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = equipo.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Fundación: ${equipo.anioFundacion}", style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(
                            text = equipo.titulos,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Botón Nuevo Registro [cite: 20]
        Button(
            onClick = { navController.popBackStack() }, // Regresa a la pantalla anterior
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Nuevo Registro")
        }
    }
}