package fi.vladi.makeit
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.math.roundToInt

private val paintLabels=mapOf(
"en" to "Choose image|Colors|Detail|Minimum region|Generate|Color preview|Width (cm)|Height (cm)|Export PDF|Processing|Done|Error",
"ru" to "Выбрать изображение|Цвета|Детализация|Минимальный фрагмент|Создать|Цветной вид|Ширина (см)|Высота (см)|Экспорт PDF|Обработка|Готово|Ошибка",
"fi" to "Valitse kuva|Värit|Yksityiskohdat|Pienin alue|Luo|Väriesikatselu|Leveys (cm)|Korkeus (cm)|Vie PDF|Käsitellään|Valmis|Virhe",
"et" to "Vali pilt|Värvid|Detailsus|Väikseim ala|Loo|Värvivaade|Laius (cm)|Kõrgus (cm)|Ekspordi PDF|Töötlemine|Valmis|Viga",
"sv" to "Välj bild|Färger|Detaljer|Minsta område|Skapa|Färgvy|Bredd (cm)|Höjd (cm)|Exportera PDF|Bearbetar|Klart|Fel",
"de" to "Bild wählen|Farben|Details|Mindestfläche|Erstellen|Farbansicht|Breite (cm)|Höhe (cm)|PDF exportieren|Verarbeitung|Fertig|Fehler",
"fr" to "Choisir une image|Couleurs|Détails|Zone minimale|Créer|Aperçu couleur|Largeur (cm)|Hauteur (cm)|Exporter PDF|Traitement|Terminé|Erreur",
"pl" to "Wybierz obraz|Kolory|Szczegóły|Minimalny obszar|Utwórz|Podgląd kolorów|Szerokość (cm)|Wysokość (cm)|Eksport PDF|Przetwarzanie|Gotowe|Błąd",
"es" to "Elegir imagen|Colores|Detalle|Área mínima|Crear|Vista en color|Ancho (cm)|Alto (cm)|Exportar PDF|Procesando|Listo|Error",
"it" to "Scegli immagine|Colori|Dettagli|Area minima|Crea|Anteprima colore|Larghezza (cm)|Altezza (cm)|Esporta PDF|Elaborazione|Pronto|Errore",
"lv" to "Izvēlies attēlu|Krāsas|Detalizācija|Minimālais laukums|Izveidot|Krāsu skats|Platums (cm)|Augstums (cm)|Eksportēt PDF|Apstrāde|Gatavs|Kļūda",
"lt" to "Pasirinkti vaizdą|Spalvos|Detalumas|Mažiausia sritis|Kurti|Spalvų peržiūra|Plotis (cm)|Aukštis (cm)|Eksportuoti PDF|Apdorojama|Atlikta|Klaida",
"nb" to "Velg bilde|Farger|Detaljer|Minste område|Opprett|Fargevisning|Bredde (cm)|Høyde (cm)|Eksporter PDF|Behandler|Ferdig|Feil",
"da" to "Vælg billede|Farver|Detaljer|Mindste område|Opret|Farvevisning|Bredde (cm)|Højde (cm)|Eksportér PDF|Behandler|Færdig|Fejl")
@Composable
internal fun NumberPaintingEditor(lang:String,fullColor:Boolean=false,selectedIdea:Int?=null,creativeId:String?=null){
 val t=(paintLabels[lang]?:paintLabels.getValue("en")).split('|')
 val context=LocalContext.current;val scope=rememberCoroutineScope()
 var isolated by remember{mutableStateOf<Bitmap?>(null)}
 val removeLabel=mapOf("en" to "Remove background","ru" to "Удалить фон","fi" to "Poista tausta","et" to "Eemalda taust","sv" to "Ta bort bakgrund","de" to "Hintergrund entfernen","fr" to "Supprimer le fond","pl" to "Usuń tło","es" to "Eliminar fondo","it" to "Rimuovi sfondo","lv" to "Noņemt fonu","lt" to "Pašalinti foną","nb" to "Fjern bakgrunn","da" to "Fjern baggrund").getValue(lang)
 var source by remember{mutableStateOf<Bitmap?>(null)}
 var result by remember{mutableStateOf<NumberPainting?>(null)}
 var lineMm by remember{mutableStateOf(0.25f)}
 val complexityLabels=mapOf("en" to "Easy|Medium|Hard|Line width","ru" to "Простая|Средняя|Сложная|Толщина линий","fi" to "Helppo|Keskitaso|Vaikea|Viivan paksuus","et" to "Lihtne|Keskmine|Raske|Joone paksus","sv" to "Enkel|Medel|Svår|Linjebredd","de" to "Einfach|Mittel|Schwer|Linienbreite","fr" to "Facile|Moyen|Difficile|Épaisseur du trait","pl" to "Łatwa|Średnia|Trudna|Grubość linii","es" to "Fácil|Media|Difícil|Grosor de línea","it" to "Facile|Media|Difficile|Spessore linea","lv" to "Viegla|Vidēja|Grūta|Līnijas biezums","lt" to "Lengva|Vidutinė|Sunki|Linijos storis","nb" to "Enkel|Middels|Vanskelig|Linjetykkelse","da" to "Let|Mellem|Svær|Linjetykkelse").getValue(lang).split('|')
 var colors by remember{mutableIntStateOf(16)}
 var detail by remember{mutableIntStateOf(480)}
 var minimum by remember{mutableIntStateOf(80)}
 var width by remember{mutableStateOf("30")}
 var height by remember{mutableStateOf("30")}
 var landscape by remember{mutableStateOf(false)}
 var fitOne by remember{mutableStateOf(false)}
 val printLabels=(mapOf("en" to "Portrait|Landscape|Fit on one sheet","ru" to "Книжная|Альбомная|На одном листе","fi" to "Pysty|Vaaka|Sovita yhdelle arkille","et" to "Püstpaigutus|Rõhtpaigutus|Mahuta ühele lehele","sv" to "Stående|Liggande|Anpassa till ett ark","de" to "Hochformat|Querformat|Auf ein Blatt","fr" to "Portrait|Paysage|Sur une feuille","pl" to "Pionowa|Pozioma|Na jednym arkuszu","es" to "Vertical|Horizontal|En una hoja","it" to "Verticale|Orizzontale|Su un foglio","lv" to "Portrets|Ainava|Vienā lapā","lt" to "Vertikali|Horizontali|Viename lape","nb" to "Stående|Liggende|På ett ark","da" to "Stående|Liggende|På ét ark").getValue(lang)).split('|')
 var a3 by remember{mutableStateOf(false)}
 var colorView by remember{mutableStateOf(false)}
 var busy by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("")}
 var pending by remember{mutableStateOf<java.io.File?>(null)}
 LaunchedEffect(creativeId){if(creativeId!=null){busy=true;try{val folder=java.io.File(creativeRoot(context),creativeId);val j=withContext(Dispatchers.IO){creativeSettings(folder)};val b=withContext(Dispatchers.IO){creativeImage(folder,"source.png")?:error("image")};source=b;isolated=withContext(Dispatchers.IO){creativeImage(folder,"isolated.png")};width=j.getString("width");height=j.getString("height");a3=j.getBoolean("a3");lineMm=j.optDouble("lineMm",0.25).toFloat().coerceIn(0.15f,0.6f);landscape=j.optBoolean("landscape");fitOne=j.optBoolean("fitOne");colors=j.optInt("colors",16);detail=j.optInt("detail",480);minimum=j.optInt("minimum",80);if(!fullColor)result=withContext(Dispatchers.Default){numberPainting(b,colors,detail,minimum)}}catch(e:Exception){status=t[11]}finally{busy=false}}}
 LaunchedEffect(selectedIdea){if(selectedIdea!=null){busy=true;try{val b=withContext(Dispatchers.IO){ideaBitmap(context,selectedIdea)};source=b;isolated=null;result=null;height="30";width=(30.0*b.width/b.height).toString()}catch(e:Exception){status=t[11]}finally{busy=false}}}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null){busy=true;scope.launch{
   try{
    val b=withContext(Dispatchers.IO){
     val options=BitmapFactory.Options().apply{inJustDecodeBounds=true}
     context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,options)}
     var sample=1
     while(maxOf(options.outWidth,options.outHeight)/sample>(if(fullColor)4096 else 1600))sample*=2
     context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}?:error("image")
    }
    source=b;isolated=null;result=null;height="30";width=(30.0*b.width/b.height).toString();status=""
   }catch(e:Exception){status=t[11]}finally{busy=false}
  }}
 }
 val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
  val file=pending;pending=null
  if(file!=null)scope.launch{try{if(uri!=null){withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri,"wt")?.use{out->file.inputStream().use{it.copyTo(out)}}?:error("output")};status=t[10]}}catch(e:Exception){status=t[11]}finally{file.delete();busy=false}}
  else busy=false
 }
 Button(onClick={picker.launch("image/*")},enabled=!busy){Text(t[0])}
 if(!fullColor){
 Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){for(i in 0..2){val preset=listOf(Triple(8,320,160),Triple(16,480,100),Triple(24,720,65))[i];FilterChip(colors==preset.first&&detail==preset.second&&minimum==preset.third,{colors=preset.first;detail=preset.second;minimum=preset.third;result=null},{Text(complexityLabels[i])},modifier=Modifier.weight(1f),enabled=!busy)}}
 Text("${complexityLabels[3]}: ${(lineMm*100).roundToInt()/100f} mm")
 Slider(lineMm,{lineMm=it},valueRange=0.15f..0.6f,enabled=!busy)
 Text("${t[1]}: $colors");Slider(colors.toFloat(),{colors=it.roundToInt();result=null},valueRange=4f..48f,steps=43,enabled=!busy)
 Text("${t[2]}: $detail");Slider(detail.toFloat(),{detail=it.roundToInt();result=null},valueRange=240f..800f,enabled=!busy)
 Text("${t[3]}: $minimum");Slider(minimum.toFloat(),{minimum=it.roundToInt();result=null},valueRange=20f..500f,enabled=!busy)
 Button(onClick={val b=source?:return@Button;busy=true;status="";scope.launch{try{result=withContext(Dispatchers.Default){numberPainting(b,colors,detail,minimum)}}catch(e:Exception){status=t[11]}finally{busy=false}}},enabled=source!=null&&!busy){Text(t[4])}
 val r=result
 if(r!=null){
  Row{Checkbox(colorView,{colorView=it});Text(t[5],Modifier.padding(top=12.dp))}
  val numbered=remember(r){r.outline.copy(Bitmap.Config.ARGB_8888,true).apply{val c=Canvas(this);val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.BLACK;textAlign=Paint.Align.CENTER;textSize=8f};for(region in r.regions)c.drawText((region.color+1).toString(),region.x.toFloat(),region.y-(p.ascent()+p.descent())/2,p)}}
  val image=if(colorView)r.preview else numbered
  Image(image.asImageBitmap(),null,Modifier.fillMaxWidth().aspectRatio(image.width.toFloat()/image.height))
  for(row in r.palette.indices.chunked(6)){Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){for(i in row){Column(Modifier.weight(1f)){Box(Modifier.fillMaxWidth().height(24.dp).background(Color(r.palette[i])));Text((i+1).toString())}}}}
 }else source?.let{Image(it.asImageBitmap(),null,Modifier.fillMaxWidth().aspectRatio(it.width.toFloat()/it.height))}
 }else {
 val original=source
 if(original!=null){
 Row{Checkbox(isolated!=null,{checked->if(!checked)isolated=null else {busy=true;scope.launch{try{isolated=withContext(Dispatchers.Default){smartSelection(original,0.01f,0.01f,0.99f,0.99f,30)}}catch(e:Exception){status=t[11]}finally{busy=false}}}},enabled=!busy);Text(removeLabel,Modifier.padding(top=12.dp))}
 val shown=isolated?:original
 Image(shown.asImageBitmap(),null,Modifier.fillMaxWidth().aspectRatio(shown.width.toFloat()/shown.height))
 }
 }
 if(!fitOne||fullColor){
 OutlinedTextField(width,{width=it;val b=source;val n=it.replace(',','.').toDoubleOrNull();if(b!=null&&n!=null)height=(n*b.height/b.width).toString()},label={Text(t[6])},singleLine=true,enabled=!busy)
 OutlinedTextField(height,{height=it;val b=source;val n=it.replace(',','.').toDoubleOrNull();if(b!=null&&n!=null)width=(n*b.width/b.height).toString()},label={Text(t[7])},singleLine=true,enabled=!busy)
 }
 Row{FilterChip(!a3,{a3=false},{Text("A4")});Spacer(Modifier.width(8.dp));FilterChip(a3,{a3=true},{Text("A3")})}
 if(!fullColor){
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(!landscape,{landscape=false},{Text(printLabels[0])},enabled=!busy);FilterChip(landscape,{landscape=true},{Text(printLabels[1])},enabled=!busy)}
 Row{Checkbox(fitOne,{fitOne=it},enabled=!busy);Text(printLabels[2],Modifier.padding(top=12.dp))}
 }
 val w=width.replace(',','.').toDoubleOrNull();val h=height.replace(',','.').toDoubleOrNull()
 val layout=if(!fullColor&&fitOne){source?.let{b->val paper=SheetLayout(1.0,1.0,a3,landscape);val scale=minOf(paper.usableW.toDouble()/b.width,paper.usableH.toDouble()/b.height);SheetLayout(minOf(paper.usableW.toDouble(),b.width*scale),minOf(paper.usableH.toDouble(),b.height*scale),a3,landscape,true)}}else if(w!=null&&h!=null&&w in 0.1..1000.0&&h in 0.1..1000.0)SheetLayout(w*10,h*10,a3,!fullColor&&landscape)else null
 Button(onClick={val plan=layout?:return@Button;val data=result;val image=isolated?:source;if(fullColor&&image==null||!fullColor&&data==null)return@Button;busy=true;scope.launch{
  var temp:java.io.File?=null
  try{val file=withContext(Dispatchers.IO){java.io.File.createTempFile("painting-", ".pdf",context.cacheDir).also{temp=it;it.outputStream().use{out->if(fullColor)exportColorPrint(requireNotNull(image),plan,out)else exportNumberPainting(requireNotNull(data),plan,out,lineMm)}}};pending=file;saver.launch(if(fullColor)"makeit-color.pdf" else "makeit-painting.pdf")}
  catch(e:Exception){temp?.delete();pending=null;status=t[11];busy=false}
 }},enabled=!busy&&(if(fullColor)source!=null else result!=null)&&layout!=null&&layout.count in 1..400){Text(t[8])}
 val saveLabel=mapOf("en" to "Save project","ru" to "Сохранить проект","fi" to "Tallenna projekti","et" to "Salvesta projekt","sv" to "Spara projekt","de" to "Projekt speichern","fr" to "Enregistrer le projet","pl" to "Zapisz projekt","es" to "Guardar proyecto","it" to "Salva progetto","lv" to "Saglabāt projektu","lt" to "Išsaugoti projektą","nb" to "Lagre prosjekt","da" to "Gem projekt").getValue(lang)
 var projectName by remember{mutableStateOf("")}
 OutlinedTextField(projectName,{projectName=it},label={Text(saveLabel)},singleLine=true,enabled=!busy)
 Button(onClick={val b=source?:return@Button;val mask=isolated;val j=org.json.JSONObject().put("name",projectName.ifBlank{if(fullColor)"Color" else "Painting"}).put("fullColor",fullColor).put("width",width).put("height",height).put("a3",a3).put("lineMm",lineMm.toDouble()).put("landscape",landscape).put("fitOne",fitOne).put("colors",colors).put("detail",detail).put("minimum",minimum);busy=true;scope.launch{try{withContext(Dispatchers.IO){creativeSave(context,b,mask,j)};status=t[10]}catch(e:Exception){status=t[11]}finally{busy=false}}},enabled=!busy&&source!=null&&layout!=null){Text(saveLabel)}
 if(busy){CircularProgressIndicator();Text(t[9])}
 if(status.isNotEmpty())Text(status)
}
