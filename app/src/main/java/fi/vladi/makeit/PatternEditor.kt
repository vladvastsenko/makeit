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
import kotlinx.coroutines.launch
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
 var outline by remember { mutableStateOf(true) }
 var threshold by remember { mutableStateOf(80f) }
 var processed by remember { mutableStateOf<Bitmap?>(null) }
 var busy by remember { mutableStateOf(false) }
 var status by remember { mutableStateOf("") }
 val extra=exportLabels[lang] ?: exportLabels.getValue("en")
 val scope=rememberCoroutineScope()
 LaunchedEffect(bitmap,outline,threshold) {
  processed=null
  val b=bitmap
  if(outline && b!=null) processed=withContext(Dispatchers.Default){outlineBitmap(b,threshold.toInt())}
 }
 val preview=if(outline)processed else bitmap
 val w=width.replace(',','.').toDoubleOrNull()
 val h=height.replace(',','.').toDoubleOrNull()
 val layout=if(w!=null&&h!=null&&w in 0.1..1000.0&&h in 0.1..1000.0) SheetLayout(w*10,h*10,a3) else null
 val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { dest ->
  val image=preview
  val plan=layout
  if(dest!=null&&image!=null&&plan!=null){
   busy=true
   scope.launch {
    status=try { withContext(Dispatchers.IO){context.contentResolver.openOutputStream(dest)?.use{exportPattern(if(outline) image else outlineBitmap(image,threshold.toInt()),plan,it)} ?: error("output")};extra[2] } catch(e:Exception){extra[3]} finally {busy=false}
   }
  }
 }
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
 preview?.let { Image(it.asImageBitmap(),contentDescription=t[1],modifier=Modifier.fillMaxWidth().heightIn(max=340.dp)) }
 Row { Checkbox(checked=outline,onCheckedChange={outline=it}); Text(extra[0],modifier=Modifier.padding(top=12.dp)) }
 if(outline) Slider(value=threshold,onValueChange={threshold=it},valueRange=20f..250f)
 if(failed) Text(t[5],color=MaterialTheme.colorScheme.error)
 OutlinedTextField(width,{width=it},label={Text(t[2]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 OutlinedTextField(height,{height=it},label={Text(t[3]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=!a3,onClick={a3=false},label={Text("A4")});FilterChip(selected=a3,onClick={a3=true},label={Text("A3")})}
 if(layout!=null){
  Text(t[4]+": "+layout.count+" ("+layout.columns+" × "+layout.rows+") + 1",style=MaterialTheme.typography.titleLarge)
  Text("10 mm / 10 mm · 100%")
  Button(enabled=preview!=null&&!busy&&layout.count<=400,onClick={saver.launch("MakeIt.pdf")},modifier=Modifier.fillMaxWidth()){Text(extra[1])}
 }
 if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
 if(status.isNotEmpty()) Text(status)

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

private val exportLabels=mapOf(
"en" to listOf("Outline","Save PDF","PDF saved","PDF export failed"),
"fi" to listOf("Ääriviivat","Tallenna PDF","PDF tallennettu","PDF-vienti epäonnistui"),
"et" to listOf("Kontuur","Salvesta PDF","PDF salvestatud","PDF eksport ebaõnnestus"),
"ru" to listOf("Контуры","Сохранить PDF","PDF сохранён","Ошибка экспорта PDF"),
"sv" to listOf("Konturer","Spara PDF","PDF sparad","PDF-export misslyckades"),
"de" to listOf("Konturen","PDF speichern","PDF gespeichert","PDF-Export fehlgeschlagen"),
"fr" to listOf("Contours","Enregistrer le PDF","PDF enregistré","Échec de l’export PDF"),
"pl" to listOf("Kontury","Zapisz PDF","PDF zapisany","Błąd eksportu PDF"),
"es" to listOf("Contornos","Guardar PDF","PDF guardado","Error al exportar PDF"),
"it" to listOf("Contorni","Salva PDF","PDF salvato","Esportazione PDF non riuscita"),
"lv" to listOf("Kontūras","Saglabāt PDF","PDF saglabāts","PDF eksportēšana neizdevās"),
"lt" to listOf("Kontūrai","Išsaugoti PDF","PDF išsaugotas","PDF eksportuoti nepavyko"),
"nb" to listOf("Konturer","Lagre PDF","PDF lagret","PDF-eksport mislyktes"),
"da" to listOf("Konturer","Gem PDF","PDF gemt","PDF-eksport mislykkedes"))
