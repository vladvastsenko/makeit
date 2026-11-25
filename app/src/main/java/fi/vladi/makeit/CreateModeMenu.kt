package fi.vladi.makeit
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val modes=mapOf(
"en" to "Cutting template|Paint by numbers|Full-size printing",
"fi" to "Leikkausmalli|Maalaus numeroiden mukaan|Täysikokoinen tulostus",
"et" to "Lõikemall|Numbrite järgi värvimine|Täissuuruses printimine",
"ru" to "Шаблон для вырезания|Раскраска по номерам|Полноразмерная печать",
"sv" to "Skärmall|Måla efter nummer|Utskrift i full storlek",
"de" to "Schneidevorlage|Malen nach Zahlen|Druck in Originalgröße",
"fr" to "Gabarit de découpe|Peinture par numéros|Impression grandeur réelle",
"pl" to "Szablon do wycinania|Malowanie po numerach|Druk w pełnym rozmiarze",
"es" to "Plantilla de corte|Pintar por números|Impresión a tamaño real",
"it" to "Sagoma da ritagliare|Dipingere con i numeri|Stampa a grandezza naturale",
"lv" to "Griešanas veidne|Krāsošana pēc numuriem|Drukāšana pilnā izmērā",
"lt" to "Pjovimo šablonas|Spalvinimas pagal skaičius|Viso dydžio spausdinimas",
"nb" to "Skjæremal|Maling etter tall|Utskrift i full størrelse",
"da" to "Skæreskabelon|Mal efter tal|Udskrivning i fuld størrelse")
@Composable
internal fun CreateModeMenu(lang:String,comingSoon:String,onSelect:(Int)->Unit){
 val titles=(modes[lang]?:modes.getValue("en")).split('|')
 titles.forEachIndexed { i,title ->
  Card(onClick={onSelect(i)},modifier=Modifier.fillMaxWidth()){
   Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Text(listOf("✂","▦","▣")[i],style=MaterialTheme.typography.headlineLarge)
    Text(title,style=MaterialTheme.typography.titleLarge)
   }
  }
 }
}
