package fi.vladi.makeit
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File

private val slotLabels=mapOf(
"en" to "3D templates|Snowman|Tree|Rabbit|Pumpkin|Width (cm)|Height (cm)|Material (mm)|Clearance (mm)|Export PDF|Done|Error",
"ru" to "Объёмные фигуры|Снеговик|Ёлка|Заяц|Тыква|Ширина (см)|Высота (см)|Материал (мм)|Допуск (мм)|Экспорт PDF|Готово|Ошибка",
"fi" to "3D-mallit|Lumiukko|Kuusi|Jänis|Kurpitsa|Leveys (cm)|Korkeus (cm)|Materiaali (mm)|Välys (mm)|Vie PDF|Valmis|Virhe",
"et" to "3D-mallid|Lumememm|Kuusk|Jänes|Kõrvits|Laius (cm)|Kõrgus (cm)|Materjal (mm)|Lõtk (mm)|Ekspordi PDF|Valmis|Viga",
"sv" to "3D-mallar|Snögubbe|Gran|Kanin|Pumpa|Bredd (cm)|Höjd (cm)|Material (mm)|Spel (mm)|Exportera PDF|Klart|Fel",
"de" to "3D-Vorlagen|Schneemann|Tannenbaum|Hase|Kürbis|Breite (cm)|Höhe (cm)|Material (mm)|Spiel (mm)|PDF exportieren|Fertig|Fehler",
"fr" to "Modèles 3D|Bonhomme de neige|Sapin|Lapin|Citrouille|Largeur (cm)|Hauteur (cm)|Matériau (mm)|Jeu (mm)|Exporter PDF|Terminé|Erreur",
"pl" to "Szablony 3D|Bałwan|Choinka|Królik|Dynia|Szerokość (cm)|Wysokość (cm)|Materiał (mm)|Luz (mm)|Eksport PDF|Gotowe|Błąd",
"es" to "Plantillas 3D|Muñeco de nieve|Árbol|Conejo|Calabaza|Ancho (cm)|Alto (cm)|Material (mm)|Holgura (mm)|Exportar PDF|Listo|Error",
"it" to "Modelli 3D|Pupazzo di neve|Albero|Coniglio|Zucca|Larghezza (cm)|Altezza (cm)|Materiale (mm)|Gioco (mm)|Esporta PDF|Fatto|Errore",
"lv" to "3D veidnes|Sniegavīrs|Egle|Zaķis|Ķirbis|Platums (cm)|Augstums (cm)|Materiāls (mm)|Atstarpe (mm)|Eksportēt PDF|Gatavs|Kļūda",
"lt" to "3D šablonai|Sniego senis|Eglė|Kiškis|Moliūgas|Plotis (cm)|Aukštis (cm)|Medžiaga (mm)|Tarpas (mm)|Eksportuoti PDF|Atlikta|Klaida",
"nb" to "3D-maler|Snømann|Juletre|Kanin|Gresskar|Bredde (cm)|Høyde (cm)|Materiale (mm)|Klaring (mm)|Eksporter PDF|Ferdig|Feil",
"da" to "3D-skabeloner|Snemand|Juletræ|Kanin|Græskar|Bredde (cm)|Højde (cm)|Materiale (mm)|Spillerum (mm)|Eksportér PDF|Færdig|Fejl")
private fun slotText(lang:String)=(slotLabels[lang]?:slotLabels.getValue("en")).split('|')
private fun slotPreview(id:Int,w:Float,h:Float,gap:Float,top:Boolean):Bitmap{
 val b=Bitmap.createBitmap(320,360,Bitmap.Config.ARGB_8888);val c=Canvas(b);c.drawColor(android.graphics.Color.WHITE)
 val scale=minOf(300f/w,340f/h);c.translate((320-w*scale)/2,(360-h*scale)/2);c.scale(scale,scale)
 val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=0xffd7bb91.toInt()}
 val path=slotPart(id,w,h,gap,top);c.drawPath(path,p);p.color=android.graphics.Color.BLACK;p.style=Paint.Style.STROKE;p.strokeWidth=1f/scale;c.drawPath(path,p);return b
}
@Composable internal fun SlotIdeas(lang:String,onOpen:(Int)->Unit){
 val t=slotText(lang);Text(t[0],style=MaterialTheme.typography.titleLarge)
 for(row in (0..3).chunked(2))Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
 for(id in row){val b=remember(id){slotPreview(id,200f,300f,6f,false)}
 Card(onClick={onOpen(id)},modifier=Modifier.weight(1f)){Image(b.asImageBitmap(),t[id+1],Modifier.fillMaxWidth().aspectRatio(.89f));Text(t[id+1],Modifier.padding(8.dp))}}
 }
}
@Composable internal fun SlotEditor(lang:String,id:Int){
 val t=slotText(lang);val context=LocalContext.current;val scope=rememberCoroutineScope()
 var width by remember(id){mutableStateOf("30")};var height by remember(id){mutableStateOf("60")}
 var thickness by remember{mutableStateOf("6")};var tolerance by remember{mutableStateOf("0.2")}
 var a3 by remember{mutableStateOf(false)};var busy by remember{mutableStateOf(false)};var status by remember{mutableStateOf("")};var pending by remember{mutableStateOf<File?>(null)}
 val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->val f=pending;pending=null;scope.launch{try{if(uri!=null&&f!=null){withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri)?.use{out->f.inputStream().use{it.copyTo(out)}}?:error("output")};status=t[10]}}catch(e:Exception){status=t[11]}finally{f?.delete();busy=false}}}
 Text(t[id+1],style=MaterialTheme.typography.titleLarge)
 OutlinedTextField(width,{width=it},label={Text(t[5])},enabled=!busy)
 OutlinedTextField(height,{height=it},label={Text(t[6])},enabled=!busy)
 OutlinedTextField(thickness,{thickness=it},label={Text(t[7])},enabled=!busy)
 OutlinedTextField(tolerance,{tolerance=it},label={Text(t[8])},enabled=!busy)
 Row{FilterChip(!a3,{a3=false},{Text("A4")});FilterChip(a3,{a3=true},{Text("A3")})}
 fun number(s:String)=s.replace(',','.').toFloatOrNull()
 val w=(number(width)?:0f)*10;val h=(number(height)?:0f)*10;val gap=(number(thickness)?:0f)+(number(tolerance)?:0f)
 val valid=w in 30f..5000f&&h in 30f..5000f&&gap in .5f..minOf(w*.08f,h*.08f)&&(number(thickness)?:0f)>0f
 if(valid){
 val layout=SheetLayout(w.toDouble(),h.toDouble(),a3)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){for(part in 0..1){val b=remember(id,w,h,gap,part){slotPreview(id,w,h,gap,part==1)};Column(Modifier.weight(1f)){Text(if(part==0)"A" else "B");Image(b.asImageBitmap(),null,Modifier.fillMaxWidth().aspectRatio(.89f))}}}
 Text("PDF: ${layout.count*2+1}")
 Button(enabled=!busy&&layout.count in 1..200,onClick={busy=true;scope.launch{var temp:File?=null;try{val f=withContext(Dispatchers.IO){File.createTempFile("slots-",".pdf",context.cacheDir).also{temp=it;it.outputStream().use{out->exportSlots(id,layout,gap,out)}}};pending=f;saver.launch("makeit-3d.pdf")}catch(e:Exception){temp?.delete();busy=false;status=t[11]}}}){Text(t[9])}
 }
 if(busy)CircularProgressIndicator()
 if(status.isNotEmpty())Text(status)
}
