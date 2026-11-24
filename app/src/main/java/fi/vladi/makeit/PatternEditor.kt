package fi.vladi.makeit
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

@Composable
fun PatternEditor(lang: String) {
 val t = editorLabels[lang] ?: editorLabels.getValue("en")
 val context = LocalContext.current
 var uri by remember { mutableStateOf<Uri?>(null) }
 var bitmap by remember { mutableStateOf<Bitmap?>(null) }
 var failed by remember { mutableStateOf(false) }
 var width by remember { mutableStateOf("110") }
 var height by remember { mutableStateOf("123") }
 var a3 by remember { mutableStateOf(false) }
 val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if(it != null) uri=it }
 LaunchedEffect(uri) {
  val selected = uri ?: return@LaunchedEffect
  bitmap=null
  failed=false
  bitmap=withContext(Dispatchers.IO) {
   runCatching {
    val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
    context.contentResolver.openInputStream(selected)?.use { BitmapFactory.decodeStream(it,null,bounds) }
    var sample=1
    while(maxOf(bounds.outWidth,bounds.outHeight)/sample>2048) sample*=2
    context.contentResolver.openInputStream(selected)?.use { BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample}) }
   }.getOrNull()
  }
  failed=bitmap==null
 }
 Text(t[0],style=MaterialTheme.typography.headlineMedium)
 Button(onClick={picker.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text(t[1])}
 bitmap?.let { Image(it.asImageBitmap(),contentDescription=t[1],modifier=Modifier.fillMaxWidth().heightIn(max=340.dp)) }
 if(failed) Text(t[5],color=MaterialTheme.colorScheme.error)
 OutlinedTextField(width,{width=it},label={Text(t[2]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 OutlinedTextField(height,{height=it},label={Text(t[3]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=!a3,onClick={a3=false},label={Text("A4")});FilterChip(selected=a3,onClick={a3=true},label={Text("A3")})}
 val w=width.replace(',','.').toDoubleOrNull()
 val h=height.replace(',','.').toDoubleOrNull()
 if(w!=null&&h!=null&&w in 0.1..1000.0&&h in 0.1..1000.0){
  val x=ceil((w*10-10).coerceAtLeast(1.0)/(if(a3)267 else 180)).toInt()
  val y=ceil((h*10-10).coerceAtLeast(1.0)/(if(a3)390 else 267)).toInt()
  Text(t[4]+": "+(x*y)+" ("+x+" × "+y+")",style=MaterialTheme.typography.titleLarge)
  Text("10 mm + 10 mm")
 }
}
private val editorLabels=mapOf(
"en" to listOf("Pattern editor","Choose image","Width","Height","Estimated sheets","Cannot open image"),
"fi" to listOf("Kaavaeditori","Valitse kuva","Leveys","Korkeus","Arvioitu arkkimäärä","Kuvaa ei voi avata"),
"et" to listOf("Šablooniredaktor","Vali pilt","Laius","Kõrgus","Hinnanguline lehtede arv","Pilti ei saa avada"),
"ru" to listOf("Редактор выкроек","Выбрать изображение","Ширина","Высота","Расчётное число листов","Не удалось открыть изображение"),
"sv" to listOf("Mallredigerare","Välj bild","Bredd","Höjd","Beräknat antal ark","Kan inte öppna bilden"),
"de" to listOf("Vorlageneditor","Bild auswählen","Breite","Höhe","Geschätzte Blattzahl","Bild kann nicht geöffnet werden"),
"fr" to listOf("Éditeur de modèles","Choisir une image","Largeur","Hauteur","Nombre estimé de feuilles","Impossible d’ouvrir l’image"),
"pl" to listOf("Edytor szablonów","Wybierz obraz","Szerokość","Wysokość","Szacowana liczba arkuszy","Nie można otworzyć obrazu"),
"es" to listOf("Editor de plantillas","Elegir imagen","Anchura","Altura","Número estimado de hojas","No se puede abrir la imagen"),
"it" to listOf("Editor di modelli","Scegli immagine","Larghezza","Altezza","Numero stimato di fogli","Impossibile aprire l’immagine"),
"lv" to listOf("Veidņu redaktors","Izvēlēties attēlu","Platums","Augstums","Aptuvenais lapu skaits","Nevar atvērt attēlu"),
"lt" to listOf("Šablonų rengyklė","Pasirinkti vaizdą","Plotis","Aukštis","Numatomas lapų skaičius","Nepavyko atverti vaizdo"),
"nb" to listOf("Malredigering","Velg bilde","Bredde","Høyde","Anslått antall ark","Kan ikke åpne bildet"),
"da" to listOf("Skabeloneditor","Vælg billede","Bredde","Højde","Anslået antal ark","Kan ikke åbne billedet"))
