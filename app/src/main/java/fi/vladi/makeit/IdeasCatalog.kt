package fi.vladi.makeit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun IdeasCatalog(lang:String,onCreate:()->Unit){
 val t=ideaLabels[lang] ?: ideaLabels.getValue("en")
 val symbols=listOf("▧","✦","◇","✂","◎")
 val colors=listOf(0xFFE8D5BA,0xFFFFD9B8,0xFFD8E7DC,0xFFDCE4F4,0xFFF1DFE7)
 for(i in 0..4){
 Card(onClick=onCreate,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color(colors[i]))){
 Row(Modifier.padding(22.dp),horizontalArrangement=Arrangement.spacedBy(20.dp)){
 Text(symbols[i],fontSize=38.sp,color=Color(0xFF243D35))
 Column{Text(t[i],style=MaterialTheme.typography.titleLarge);Text(t[5],style=MaterialTheme.typography.bodyMedium)}
 }
 }
 }
}
private val ideaLabels=mapOf(
"en" to listOf("Woodwork","Decorations","Stencils","Sewing","Photo props","Create pattern"),
"fi" to listOf("Puutyöt","Koristeet","Sabluunat","Ompelu","Kuvausrekvisiitta","Luo kaava"),
"et" to listOf("Puidutööd","Kaunistused","Šabloonid","Õmblemine","Fotorekvisiidid","Loo šabloon"),
"ru" to listOf("Работа с деревом","Декорации","Трафареты","Шитьё","Фотозоны","Создать выкройку"),
"sv" to listOf("Träarbete","Dekorationer","Schabloner","Sömnad","Fotorekvisita","Skapa mall"),
"de" to listOf("Holzarbeiten","Dekorationen","Schablonen","Nähen","Fotorequisiten","Vorlage erstellen"),
"fr" to listOf("Travail du bois","Décorations","Pochoirs","Couture","Accessoires photo","Créer un modèle"),
"pl" to listOf("Prace w drewnie","Dekoracje","Szablony","Szycie","Rekwizyty fotograficzne","Utwórz szablon"),
"es" to listOf("Carpintería","Decoraciones","Plantillas","Costura","Accesorios de fotos","Crear plantilla"),
"it" to listOf("Lavori in legno","Decorazioni","Stencil","Cucito","Accessori fotografici","Crea modello"),
"lv" to listOf("Kokapstrāde","Dekorācijas","Trafareti","Šūšana","Foto rekvizīti","Izveidot veidni"),
"lt" to listOf("Medžio darbai","Dekoracijos","Trafaretai","Siuvimas","Fotografijos rekvizitai","Kurti šabloną"),
"nb" to listOf("Trearbeid","Dekorasjoner","Sjablonger","Søm","Fotorekvisitter","Lag mal"),
"da" to listOf("Træarbejde","Dekorationer","Skabeloner","Syning","Fotorekvisitter","Opret skabelon"))
