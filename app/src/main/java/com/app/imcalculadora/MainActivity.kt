package com.app.imcalculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.imcalculadora.ui.theme.IMCalculadoraTheme
import kotlin.math.pow

// Enumeració per al sexe
enum class Sexe {
    HOME, DONA
}

// Classe de dades actualitzada
data class ResultatIMC(
    val imc: Float,
    val classificacio: String,
    val color: Color,
    val rangPesSaludable: Pair<Float, Float>,
    val consellPersonalitzat: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IMCalculadoraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IMCScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun IMCScreen(modifier: Modifier = Modifier) {
    var pes by remember { mutableStateOf("") }
    var alcada by remember { mutableStateOf("") }
    var edat by remember { mutableStateOf("") }
    var sexe by remember { mutableStateOf<Sexe?>(null) }
    var resultat by remember { mutableStateOf<ResultatIMC?>(null) }
    var errorInput by remember { mutableStateOf<String?>(null) }
    // Estat per controlar la visibilitat del diàleg informatiu
    var mostrarInfoDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Mostraria el diàleg si l'estat lo te en true
    if (mostrarInfoDialog) {
        InfoDialog(onDismiss = { mostrarInfoDialog = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila per al títol i el botó d'informació
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Calculadora d'IMC Avançada",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { mostrarInfoDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = "Més informació sobre l'IMC", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Text(
            text = "Introdueix les teves dades",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Selecció per al Sexe
                Text("Sexe", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BotoSeleccio(
                        text = "Home",
                        seleccionat = sexe == Sexe.HOME,
                        onClick = { sexe = Sexe.HOME },
                        modifier = Modifier.weight(1f)
                    )
                    BotoSeleccio(
                        text = "Dona",
                        seleccionat = sexe == Sexe.DONA,
                        onClick = { sexe = Sexe.DONA },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Camps de text (Pes, alçada edat)
                OutlinedTextField(
                    value = pes,
                    onValueChange = { pes = it.replace(',', '.'); errorInput = null },
                    label = { Text("Pes (kg)") },
                    leadingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = "Pes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = alcada,
                    onValueChange = { alcada = it.replace(',', '.'); errorInput = null },
                    label = { Text("Alçada (metres)") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Alçada") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = edat,
                    onValueChange = { edat = it.filter { char -> char.isDigit() }; errorInput = null },
                    label = { Text("Edat") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Edat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                errorInput?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp), textAlign = TextAlign.Center)
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val pesFloat = pes.toFloatOrNull()
                        val alcadaFloat = alcada.toFloatOrNull()
                        val edatInt = edat.toIntOrNull()

                        if (pesFloat != null && alcadaFloat != null && edatInt != null && sexe != null && pesFloat > 0 && alcadaFloat > 0 && edatInt in 18..120) {
                            resultat = calcularResultatComplet(pesFloat, alcadaFloat, edatInt, sexe!!)
                            errorInput = null
                        } else {
                            errorInput = "Si us plau, omple tots els camps amb valors vàlids (Edat entre 18 i 120)."
                            resultat = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CALCULAR", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        resultat?.let { res ->
            ResultatCard(res)
        }
    }
}

// Composable per al diàleg informatiu
@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Què és l'IMC?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "L'Índex de Massa Corporal (IMC) és una mesura que relaciona el pes i l'alçada per estimar el greix corporal d'una persona.\n\n" +
                        "És una eina útil per identificar ràpidament possibles problemes de pes (pes insuficient, sobrepès o obesitat) en adults.\n\n" +
                        "Important: L'IMC no distingeix entre greix i múscul. Per això, pot no ser precís per a atletes, gent gran o dones embarassades. Sempre ha de ser interpretat en el context d'un estil de vida global i, si cal, amb l'ajuda d'un professional de la salut."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entesos")
            }
        }
    )
}

// Botó personalitzat per a la selecció
@Composable
fun BotoSeleccio(text: String, seleccionat: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, if (seleccionat) MaterialTheme.colorScheme.primary else Color.Gray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (seleccionat) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
        )
    ) {
        Text(text, color = if (seleccionat) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

// Funció de càlcul amb rangs ajustats per sexe
fun calcularResultatComplet(pes: Float, alcada: Float, edat: Int, sexe: Sexe): ResultatIMC {
    val imc = pes / alcada.pow(2)

    // Ajustem els llindars segons el sexe. Aquests valors són il·lustratius.
    val (pesInsuficient, pesNormal, sobrepes) = when (sexe) {
        Sexe.DONA -> Triple(19.0f, 25.9f, 30.9f) // Rangs lleugerament diferents per a dones
        Sexe.HOME -> Triple(18.5f, 24.9f, 29.9f) // Rangs estàndard
    }

    val (classificacio, color) = when {
        imc < pesInsuficient -> "Pes insuficient" to Color(0xFFADD8E6) // Blau clar
        imc < pesNormal -> "Pes normal" to Color(0xFF8BC34A)     // Verd
        imc < sobrepes -> "Sobrepès" to Color(0xFFFFC107)         // Taronja
        else -> "Obesitat" to Color(0xFFF44336)             // Vermell
    }

    // El rang de pes saludable recomanat es manté basat en els valors estàndard (18.5 - 24.9)
    val pesMin = 18.5f * alcada.pow(2)
    val pesMax = 24.9f * alcada.pow(2)

    val consell = when {
        edat > 65 && imc < 22 -> "Per a la gent gran, un IMC lleugerament superior pot ser protector. Parla amb el teu metge."
        sexe == Sexe.DONA && imc > sobrepes -> "Recorda que les dones solen tenir una major proporció de greix corporal. És un bon moment per revisar hàbits."
        sexe == Sexe.HOME && imc > sobrepes -> "L'excés de greix abdominal és un risc particular per als homes. Considera augmentar l'activitat cardiovascular."
        imc < pesInsuficient -> "És important assegurar una ingesta suficient de nutrients. Consulta un professional."
        else -> "Mantenir un estil de vida actiu i una dieta equilibrada és clau per a la salut a llarg termini."
    }

    return ResultatIMC(imc, classificacio, color, Pair(pesMin, pesMax), consell)
}

// ResultatCard i InfoSection
@Composable
fun ResultatCard(resultat: ResultatIMC) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = resultat.color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Resultat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("El teu IMC és", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "%.2f".format(resultat.imc),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = resultat.color
            )
            Text(
                text = resultat.classificacio,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = resultat.color,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            InfoSection(
                titol = "Rang de Pes Saludable Estàndard",
                contingut = "Per a la teva alçada, un pes saludable es troba entre %.1f kg i %.1f kg.".format(
                    resultat.rangPesSaludable.first,
                    resultat.rangPesSaludable.second
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoSection(
                titol = "Consell Personalitzat",
                contingut = resultat.consellPersonalitzat
            )
        }
    }
}

@Composable
fun InfoSection(titol: String, contingut: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = titol, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Text("  $titol", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = contingut, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun IMCScreenPreview() {
    IMCalculadoraTheme {
        IMCScreen()
    }
}
