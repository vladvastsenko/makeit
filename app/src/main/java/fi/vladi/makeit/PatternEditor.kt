package fi.vladi.makeit
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
 var lockRatio by remember { mutableStateOf(true) }
 var exterior by remember { mutableStateOf(false) }
 var skipBlank by remember { mutableStateOf(true) }
 var thickness by remember { mutableStateOf(0.5f) }
 var editing by remember { mutableStateOf(false) }
 var erase by remember { mutableStateOf(false) }
 val strokes=remember { mutableStateListOf<Triple<Offset,Offset,Boolean>>() }
 val redo=remember { mutableStateListOf<Triple<Offset,Offset,Boolean>>() }
 var zoom by remember { mutableStateOf(1f) }
 var pan by remember { mutableStateOf(Offset.Zero) }
 var holeMode by remember { mutableStateOf(0) }
 val more=advancedLabels[lang] ?: advancedLabels.getValue("en")
 val options=printLabels[lang] ?: printLabels.getValue("en")
 var threshold by remember { mutableStateOf(80f) }
 var processed by remember { mutableStateOf<Bitmap?>(null) }
 var busy by remember { mutableStateOf(false) }
 var status by remember { mutableStateOf("") }
 val extra=exportLabels[lang] ?: exportLabels.getValue("en")
 val scope=rememberCoroutineScope()
 LaunchedEffect(bitmap,outline,threshold,exterior) {
  processed=null
  val b=bitmap
  if(outline && b!=null) processed=withContext(Dispatchers.Default){outlineBitmap(b,threshold.toInt()).let { if(exterior)outerOnly(it) else it }}
 }
 val preview=if(outline)processed else bitmap
 LaunchedEffect(uri){strokes.clear()}
 LaunchedEffect(bitmap){bitmap?.let { if(lockRatio) height=((width.replace(',','.').toDoubleOrNull() ?: 110.0)*it.height/it.width).toString() }}
 val w=width.replace(',','.').toDoubleOrNull()
 val h=height.replace(',','.').toDoubleOrNull()
 val layout=if(w!=null&&h!=null&&w in 0.1..1000.0&&h in 0.1..1000.0) SheetLayout(w*10,h*10,a3) else null
 val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { dest ->
  val image=preview
  val plan=layout
  if(dest!=null&&image!=null&&plan!=null){
   busy=true
   scope.launch {
    status=try { withContext(Dispatchers.IO){context.contentResolver.openOutputStream(dest)?.use{val base=if(outline)image else outlineBitmap(image,threshold.toInt()).let { b -> if(exterior)outerOnly(b) else b }
 val edited=base.copy(Bitmap.Config.ARGB_8888,true)
 val canvas=android.graphics.Canvas(edited)
 val pen=android.graphics.Paint().apply { strokeWidth=3f;strokeCap=android.graphics.Paint.Cap.ROUND }
 for((a,b,e) in strokes.toList()){pen.color=if(e)android.graphics.Color.WHITE else android.graphics.Color.BLACK;canvas.drawLine(a.x*edited.width,a.y*edited.height,b.x*edited.width,b.y*edited.height,pen)}
 exportPattern(edited,plan,it,skipBlank,thickness)} ?: error("output")};extra[2] } catch(e:Exception){extra[3]} finally {busy=false}
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
 preview?.let { b ->
 Box(Modifier.fillMaxWidth().aspectRatio(b.width.toFloat()/b.height).clipToBounds().pointerInput(editing){if(!editing)detectTransformGestures { _,delta,scale,_ -> zoom=(zoom*scale).coerceIn(1f,6f);pan=if(zoom==1f)Offset.Zero else pan+delta }}) {
 Box(Modifier.matchParentSize().graphicsLayer { scaleX=zoom;scaleY=zoom;translationX=pan.x;translationY=pan.y }) {
 Image(b.asImageBitmap(),contentDescription=t[1],modifier=Modifier.matchParentSize(),contentScale=ContentScale.FillBounds)
 Canvas(Modifier.matchParentSize().pointerInput(editing,erase,b){if(editing)detectDragGestures { change,drag ->
 val end=change.position
 val start=end-drag
 strokes.add(Triple(Offset((start.x/size.width).coerceIn(0f,1f),(start.y/size.height).coerceIn(0f,1f)),Offset((end.x/size.width).coerceIn(0f,1f),(end.y/size.height).coerceIn(0f,1f)),erase))
 redo.clear()
 change.consume()
 }}) { for((a,z,e) in strokes)drawLine(if(e)Color.White else Color.Black,Offset(a.x*size.width,a.y*size.height),Offset(z.x*size.width,z.y*size.height),strokeWidth=3f) }
 }
 }
 }
 Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){
 OutlinedButton(enabled=strokes.isNotEmpty(),onClick={redo.add(strokes.removeAt(strokes.lastIndex))}){Text(more[0])}
 OutlinedButton(enabled=redo.isNotEmpty(),onClick={strokes.add(redo.removeAt(redo.lastIndex))}){Text(more[1])}
 }
 Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){
 OutlinedButton(onClick={holeMode=1}){Text(more[2])}
 OutlinedButton(onClick={holeMode=2}){Text(more[3])}
 }
 if(holeMode!=0){
 var cx by remember { mutableStateOf(0.5f) }
 var cy by remember { mutableStateOf(0.5f) }
 var radius by remember { mutableStateOf(0.1f) }
 Text("X");Slider(cx,{cx=it},valueRange=0.1f..0.9f)
 Text("Y");Slider(cy,{cy=it},valueRange=0.1f..0.9f)
 Text("Ø");Slider(radius,{radius=it},valueRange=0.01f..0.2f)
 Button(onClick={
 val b=bitmap
 val aspect=if(w!=null&&h!=null&&h>0) (w/h).toFloat() else if(b!=null)b.width.toFloat()/b.height else 1f
 for(i in 0 until 96){
 fun point(j:Int):Offset{val angle=j*2*Math.PI/96;return Offset((cx+radius*kotlin.math.cos(angle).toFloat()).coerceIn(0f,1f),(cy+radius*aspect*(if(holeMode==2)0.6f else 1f)*kotlin.math.sin(angle).toFloat()).coerceIn(0f,1f))}
 strokes.add(Triple(point(i),point(i+1),false))
 }
 redo.clear();holeMode=0
 }){Text(more[4])}
 }
 Row { Checkbox(editing,{editing=it});Text(options[4],Modifier.padding(top=12.dp)) }
 if(editing){ Row { Checkbox(erase,{erase=it});Text(options[5],Modifier.padding(top=12.dp)) };OutlinedButton(onClick={strokes.clear()}){Text(options[6])} }
 Row { Checkbox(exterior,{exterior=it;outline=true});Text(options[0],Modifier.padding(top=12.dp)) }
 Row { Checkbox(skipBlank,{skipBlank=it});Text(options[1],Modifier.padding(top=12.dp)) }
 Text(options[2]+" · "+String.format(java.util.Locale.ROOT,"%.1f",thickness)+" mm")
 Slider(thickness,{thickness=it},valueRange=0.2f..1f,steps=7)
 Row { Checkbox(lockRatio,{lockRatio=it});Text(options[3],Modifier.padding(top=12.dp)) }
 Row { Checkbox(checked=outline,onCheckedChange={outline=it}); Text(extra[0],modifier=Modifier.padding(top=12.dp)) }
 if(outline) Slider(value=threshold,onValueChange={threshold=it},valueRange=20f..250f)
 if(failed) Text(t[5],color=MaterialTheme.colorScheme.error)
 OutlinedTextField(width,{width=it; val n=it.replace(',','.').toDoubleOrNull(); val b=bitmap;if(lockRatio&&n!=null&&b!=null)height=(n*b.height/b.width).toString()},label={Text(t[2]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 OutlinedTextField(height,{height=it; val n=it.replace(',','.').toDoubleOrNull(); val b=bitmap;if(lockRatio&&n!=null&&b!=null)width=(n*b.width/b.height).toString()},label={Text(t[3]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=!a3,onClick={a3=false},label={Text("A4")});FilterChip(selected=a3,onClick={a3=true},label={Text("A3")})}
 if(layout!=null){
  Text(t[4]+": "+layout.count+" ("+layout.columns+" × "+layout.rows+") + 1",style=MaterialTheme.typography.titleLarge)
  Text("10 mm / 10 mm · 100%")
  if(layout.count<=400) Canvas(Modifier.fillMaxWidth().height(180.dp)){
   val cell=minOf(size.width/layout.columns,size.height/layout.rows)
   for(row in 0 until layout.rows)for(col in 0 until layout.columns){
    drawRect(Color(0xFFD8E7DC),Offset(col*cell,row*cell),androidx.compose.ui.geometry.Size(cell-1,cell-1))
   }
  }
  Button(enabled=bitmap!=null&&!busy,onClick={
   val b=bitmap ?: return@Button
   val snapshot=strokes.toList()
   scope.launch {
    busy=true
    status=try {withContext(Dispatchers.IO){
     val dir=java.io.File(context.filesDir,"projects").apply{mkdirs()}
     val id=java.util.UUID.randomUUID().toString()
     val image=java.io.File(dir,id+".png")
     image.outputStream().use{check(b.compress(Bitmap.CompressFormat.PNG,100,it))}
     val json=org.json.JSONObject().put("image",image.name).put("width",width).put("height",height).put("a3",a3).put("threshold",threshold.toDouble()).put("exterior",exterior).put("thickness",thickness.toDouble()).put("skipBlank",skipBlank).put("strokes",org.json.JSONArray().apply{snapshot.forEach{(a,z,e)->put(org.json.JSONArray().put(a.x.toDouble()).put(a.y.toDouble()).put(z.x.toDouble()).put(z.y.toDouble()).put(e))}})
     java.io.File(dir,id+".json").writeText(json.toString())
    };more[6]}catch(e:Exception){extra[3]}finally{busy=false}
   }
  }){Text(more[5])}
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

private val printLabels=mapOf(
"en" to listOf("Outer contour only","Skip blank sheets","Line thickness","Keep proportions","Edit lines","Erase","Clear edits"),
"fi" to listOf("Vain ulkoreuna","Ohita tyhjät sivut","Viivan paksuus","Säilytä mittasuhteet","Muokkaa viivoja","Pyyhi","Tyhjennä muutokset"),
"et" to listOf("Ainult väliskontuur","Jäta tühjad lehed vahele","Joone paksus","Säilita proportsioonid","Muuda jooni","Kustuta","Tühista muudatused"),
"ru" to listOf("Только внешний контур","Пропустить пустые листы","Толщина линии","Сохранять пропорции","Редактировать линии","Ластик","Очистить правки"),
"sv" to listOf("Endast ytterkontur","Hoppa över tomma ark","Linjetjocklek","Behåll proportioner","Redigera linjer","Sudda","Rensa ändringar"),
"de" to listOf("Nur Außenkontur","Leere Blätter überspringen","Linienstärke","Proportionen beibehalten","Linien bearbeiten","Radieren","Änderungen löschen"),
"fr" to listOf("Contour extérieur uniquement","Ignorer les feuilles vides","Épaisseur du trait","Conserver les proportions","Modifier les traits","Gommer","Effacer les modifications"),
"pl" to listOf("Tylko kontur zewnętrzny","Pomiń puste arkusze","Grubość linii","Zachowaj proporcje","Edytuj linie","Gumka","Usuń zmiany"),
"es" to listOf("Solo contorno exterior","Omitir hojas vacías","Grosor de línea","Mantener proporciones","Editar líneas","Borrar","Eliminar cambios"),
"it" to listOf("Solo contorno esterno","Salta fogli vuoti","Spessore linea","Mantieni proporzioni","Modifica linee","Gomma","Cancella modifiche"),
"lv" to listOf("Tikai ārējā kontūra","Izlaist tukšās lapas","Līnijas biezums","Saglabāt proporcijas","Rediģēt līnijas","Dzēst","Notīrīt izmaiņas"),
"lt" to listOf("Tik išorinis kontūras","Praleisti tuščius lapus","Linijos storis","Išlaikyti proporcijas","Redaguoti linijas","Trinti","Išvalyti pakeitimus"),
"nb" to listOf("Bare ytre kontur","Hopp over tomme ark","Linjetykkelse","Behold proporsjoner","Rediger linjer","Visk ut","Fjern endringer"),
"da" to listOf("Kun ydre kontur","Spring tomme ark over","Linjetykkelse","Bevar proportioner","Rediger linjer","Visk ud","Ryd ændringer"))

private val advancedLabels=mapOf(
"en" to listOf("Undo","Redo","Circle","Oval","Add","Save project","Project saved"),
"fi" to listOf("Kumoa","Tee uudelleen","Ympyrä","Soikio","Lisää","Tallenna projekti","Projekti tallennettu"),
"et" to listOf("Võta tagasi","Tee uuesti","Ring","Ovaal","Lisa","Salvesta projekt","Projekt salvestatud"),
"ru" to listOf("Отменить","Повторить","Круг","Овал","Добавить","Сохранить проект","Проект сохранён"),
"sv" to listOf("Ångra","Gör om","Cirkel","Oval","Lägg till","Spara projekt","Projekt sparat"),
"de" to listOf("Rückgängig","Wiederholen","Kreis","Oval","Hinzufügen","Projekt speichern","Projekt gespeichert"),
"fr" to listOf("Annuler","Rétablir","Cercle","Ovale","Ajouter","Enregistrer le projet","Projet enregistré"),
"pl" to listOf("Cofnij","Ponów","Koło","Owal","Dodaj","Zapisz projekt","Projekt zapisany"),
"es" to listOf("Deshacer","Rehacer","Círculo","Óvalo","Añadir","Guardar proyecto","Proyecto guardado"),
"it" to listOf("Annulla","Ripeti","Cerchio","Ovale","Aggiungi","Salva progetto","Progetto salvato"),
"lv" to listOf("Atsaukt","Atjaunot","Aplis","Ovāls","Pievienot","Saglabāt projektu","Projekts saglabāts"),
"lt" to listOf("Atšaukti","Pakartoti","Apskritimas","Ovalas","Pridėti","Išsaugoti projektą","Projektas išsaugotas"),
"nb" to listOf("Angre","Gjør om","Sirkel","Oval","Legg til","Lagre prosjekt","Prosjekt lagret"),
"da" to listOf("Fortryd","Gentag","Cirkel","Oval","Tilføj","Gem projekt","Projekt gemt"))
