package fi.vladi.makeit
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PatternEditor(lang: String, openProjects:Boolean=false, selectedProject:String?=null, onOpenProject:(String)->Unit={}, selectedIdea:Int?=null) {
 val t = editorLabels[lang] ?: editorLabels.getValue("en")
 val context = LocalContext.current
 var uri by remember { mutableStateOf<Uri?>(null) }
 var bitmap by remember { mutableStateOf<Bitmap?>(null) }
 var failed by remember { mutableStateOf(false) }
 var width by remember { mutableStateOf("110") }
 var height by remember { mutableStateOf("123") }
 var a3 by remember { mutableStateOf(false) }
 var readyTemplate by remember { mutableStateOf(false) }
 var outline by remember { mutableStateOf(true) }
 var lockRatio by remember { mutableStateOf(true) }
 var exterior by remember { mutableStateOf(false) }
 var skipBlank by remember { mutableStateOf(true) }
 var thickness by remember { mutableStateOf(0.5f) }
 var editing by remember { mutableStateOf(false) }
 var erase by remember { mutableStateOf(false) }
 val strokes=remember { mutableStateListOf<Triple<Offset,Offset,Boolean>>() }
 val redo=remember { mutableStateListOf<List<Triple<Offset,Offset,Boolean>>>() }
 val groups=remember { mutableStateListOf<Int>() }
 var gestureStart by remember { mutableIntStateOf(0) }
 var zoom by remember { mutableStateOf(1f) }
 var pan by remember { mutableStateOf(Offset.Zero) }
 var holeMode by remember { mutableStateOf(0) }
 val more=advancedLabels[lang] ?: advancedLabels.getValue("en")
 val options=printLabels[lang] ?: printLabels.getValue("en")
 var threshold by remember { mutableStateOf(25f) }
 var isolate by remember { mutableStateOf(false) }
 var left by remember { mutableStateOf(0f) }
 var top by remember { mutableStateOf(0f) }
 var right by remember { mutableStateOf(1f) }
 var bottom by remember { mutableStateOf(1f) }
 var tolerance by remember { mutableStateOf(12f) }
 var cutOnly by remember { mutableStateOf(false) }
 var processing by remember { mutableStateOf(false) }
 var emptyContour by remember { mutableStateOf(false) }
 val seg=segmentLabels[lang] ?: segmentLabels.getValue("en")
 var processed by remember { mutableStateOf<Bitmap?>(null) }
 var busy by remember { mutableStateOf(false) }
 var status by remember { mutableStateOf("") }
 val extra=exportLabels[lang] ?: exportLabels.getValue("en")
 val scope=rememberCoroutineScope()
 val dir=remember { java.io.File(context.filesDir,"projects").apply{mkdirs()} }
 var projectId by remember { mutableStateOf<String?>(null) }
 var projectName by remember { mutableStateOf("") }
 var refresh by remember { mutableIntStateOf(0) }
 var showProjects by remember { mutableStateOf(openProjects) }
 var deleteId by remember { mutableStateOf<String?>(null) }
 val pm=projectLabels[lang] ?: projectLabels.getValue("en")
 val projects=remember(refresh){dir.listFiles()?.filter{it.extension=="json"}?.sortedByDescending{it.lastModified()} ?: emptyList()}
 fun readProject(file:java.io.File):org.json.JSONObject=org.json.JSONObject(file.readText())
 fun writeProject(file:java.io.File,j:org.json.JSONObject){
 val atomic=android.util.AtomicFile(file)
 val stream=atomic.startWrite()
 try{stream.write(j.toString().toByteArray(Charsets.UTF_8));atomic.finishWrite(stream)}catch(e:Exception){atomic.failWrite(stream);throw e}
 }
 val draftFile=remember{java.io.File(context.filesDir,"draft.json")}
 val draftImage=remember{java.io.File(context.filesDir,"draft.png")}
 var restored by remember{mutableStateOf(false)}
 fun snapshot():org.json.JSONObject=org.json.JSONObject().put("readyTemplate",readyTemplate).put("isolate",isolate).put("cutOnly",cutOnly).put("left",left.toDouble()).put("top",top.toDouble()).put("right",right.toDouble()).put("bottom",bottom.toDouble()).put("tolerance",tolerance.toDouble()).put("projectId",projectId ?: "").put("name",projectName).put("width",width).put("height",height).put("a3",a3).put("outline",outline).put("lockRatio",lockRatio).put("exterior",exterior).put("skipBlank",skipBlank).put("threshold",threshold.toDouble()).put("thickness",thickness.toDouble()).put("groups",org.json.JSONArray(groups.toList())).put("strokes",org.json.JSONArray().apply{strokes.forEach{(a,z,e)->put(org.json.JSONArray().put(a.x.toDouble()).put(a.y.toDouble()).put(z.x.toDouble()).put(z.y.toDouble()).put(e))}})
 LaunchedEffect(Unit){
  runCatching{
   if(selectedIdea!=null){
    bitmap=ideaBitmap(selectedIdea);projectName=ideaName(lang,selectedIdea);width="30";height="30";outline=false;readyTemplate=true
   } else if(draftFile.exists()&&draftImage.exists()){
    val j=readProject(draftFile)
    val b=BitmapFactory.decodeFile(draftImage.path) ?: error("draft")
    projectId=j.optString("projectId").ifBlank{null};projectName=j.optString("name")
    readyTemplate=j.optBoolean("readyTemplate");isolate=j.optBoolean("isolate");cutOnly=j.optBoolean("cutOnly");left=j.optDouble("left",0.0).toFloat();top=j.optDouble("top",0.0).toFloat();right=j.optDouble("right",1.0).toFloat();bottom=j.optDouble("bottom",1.0).toFloat();tolerance=j.optDouble("tolerance",12.0).toFloat();width=j.getString("width");height=j.getString("height");a3=j.optBoolean("a3");outline=j.optBoolean("outline",true);lockRatio=j.optBoolean("lockRatio",true);exterior=j.optBoolean("exterior");skipBlank=j.optBoolean("skipBlank",true);threshold=j.optDouble("threshold",80.0).toFloat();thickness=j.optDouble("thickness",0.5).toFloat()
    val arr=j.getJSONArray("strokes")
    strokes.clear();groups.clear()
    for(i in 0 until arr.length()){val a=arr.getJSONArray(i);strokes.add(Triple(Offset(a.getDouble(0).toFloat(),a.getDouble(1).toFloat()),Offset(a.getDouble(2).toFloat(),a.getDouble(3).toFloat()),a.getBoolean(4)))}
    val g=j.optJSONArray("groups");if(g!=null)for(i in 0 until g.length())groups.add(g.getInt(i))
    if(groups.sum()!=strokes.size){groups.clear();repeat(strokes.size){groups.add(1)}}
    bitmap=b
   }
  }
  restored=true
 }
 LaunchedEffect(bitmap,restored){
  val b=bitmap
  if(restored&&b!=null){
   withContext(Dispatchers.IO){
    val atomic=android.util.AtomicFile(draftImage);val out=atomic.startWrite()
    try{check(b.compress(Bitmap.CompressFormat.PNG,100,out));atomic.finishWrite(out)}catch(e:Exception){atomic.failWrite(out)}
   }
  }
 }
 val draftState=if(restored&&bitmap!=null)snapshot().toString() else null
 LaunchedEffect(draftState){
  if(draftState!=null){kotlinx.coroutines.delay(350);withContext(Dispatchers.IO){runCatching{writeProject(draftFile,org.json.JSONObject(draftState))}}}
 }
 DisposableEffect(Unit){
  onDispose{if(restored&&bitmap!=null)runCatching{writeProject(draftFile,snapshot())}}
 }
 LaunchedEffect(restored,selectedProject){
 if(restored&&selectedProject!=null&&!openProjects){
 val file=java.io.File(dir,selectedProject+".json")
 val j=runCatching{readProject(file)}.getOrNull()
 if(j!=null){ try{
 val b=BitmapFactory.decodeFile(java.io.File(dir,j.getString("image")).path) ?: error("image")
 projectId=file.nameWithoutExtension;projectName=j.optString("name",file.nameWithoutExtension)
 readyTemplate=j.optBoolean("readyTemplate");isolate=j.optBoolean("isolate");cutOnly=j.optBoolean("cutOnly");left=j.optDouble("left",0.0).toFloat();top=j.optDouble("top",0.0).toFloat();right=j.optDouble("right",1.0).toFloat();bottom=j.optDouble("bottom",1.0).toFloat();tolerance=j.optDouble("tolerance",12.0).toFloat();width=j.getString("width");height=j.getString("height");a3=j.optBoolean("a3");threshold=j.optDouble("threshold",80.0).toFloat();exterior=j.optBoolean("exterior");thickness=j.optDouble("thickness",0.5).toFloat();skipBlank=j.optBoolean("skipBlank",true);outline=j.optBoolean("outline",true);lockRatio=j.optBoolean("lockRatio",true)
 strokes.clear();groups.clear();redo.clear()
 val arr=j.optJSONArray("strokes") ?: org.json.JSONArray()
 for(i in 0 until arr.length()){val a=arr.getJSONArray(i);strokes.add(Triple(Offset(a.getDouble(0).toFloat(),a.getDouble(1).toFloat()),Offset(a.getDouble(2).toFloat(),a.getDouble(3).toFloat()),a.getBoolean(4)))}
 val g=j.optJSONArray("groups");if(g!=null)for(i in 0 until g.length())groups.add(g.getInt(i))
 if(groups.sum()!=strokes.size){groups.clear();repeat(strokes.size){groups.add(1)}}
 bitmap=b;zoom=1f;pan=Offset.Zero;showProjects=false
 }catch(e:Exception){status=t[5]}
}
 }
 }
 LaunchedEffect(bitmap,outline,threshold,exterior,isolate,left,top,right,bottom,tolerance,cutOnly) {
  processed=null
  val b=bitmap
  emptyContour=false
  processing=outline&&b!=null
  try {
   if(outline && b!=null) {
    val result=withContext(Dispatchers.Default){
     val subject=if(isolate)smartSelection(b,left,top,right,bottom,tolerance.toInt()) else b
     val lines=if(isolate&&cutOnly)silhouettePattern(subject) else outlineBitmap(subject,threshold.toInt()).let { if(exterior)outerOnly(it) else it }
     val pixels=IntArray(lines.width*lines.height)
     lines.getPixels(pixels,0,lines.width,0,0,lines.width,lines.height)
     lines to pixels.none{it==android.graphics.Color.BLACK}
    }
    processed=result.first;emptyContour=result.second
   }
  } finally {processing=false}
 }
 val preview=if(outline)processed ?: bitmap else bitmap
 LaunchedEffect(uri){if(uri!=null){strokes.clear();groups.clear();redo.clear()}}
 LaunchedEffect(bitmap){bitmap?.let { if(lockRatio&&projectId==null&&uri!=null) height=((width.replace(',','.').toDoubleOrNull() ?: 110.0)*it.height/it.width).toString() }}
 val w=width.replace(',','.').toDoubleOrNull()
 val h=height.replace(',','.').toDoubleOrNull()
 val layout=if(w!=null&&h!=null&&w in 0.1..1000.0&&h in 0.1..1000.0) SheetLayout(w*10,h*10,a3) else null
 var pendingPdf by remember { mutableStateOf<java.io.File?>(null) }
 val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { dest ->
  val temp=pendingPdf
  pendingPdf=null
  if(temp!=null){
   scope.launch {
    try {
     if(dest!=null){
      withContext(Dispatchers.IO){
       context.contentResolver.openOutputStream(dest,"wt")?.use{out->temp.inputStream().use{it.copyTo(out)};out.flush()} ?: error("output")
      }
      status=extra[2]
     }
    }catch(e:Exception){status=extra[3]+" ("+e.javaClass.simpleName+")"}
    finally{temp.delete();busy=false}
   }
  }else{busy=false}
 }
 fun preparePdf(){
  val image=(if(outline)processed else bitmap) ?: return
  val plan=layout ?: return
  if(busy||processing||emptyContour)return
  val exportStrokes=strokes.toList()
  val direct=outline||readyTemplate
  val sensitivity=threshold.toInt()
  val outer=exterior
  val skip=skipBlank
  val line=thickness
  val name=projectName.ifBlank{"makeit"}.replace(Regex("[^\\p{L}\\p{N} _-]"),"_")+".pdf"
  busy=true;status=""
  scope.launch {
   var temp:java.io.File?=null
   try{
    val prepared=withContext(Dispatchers.IO){
     val file=java.io.File.createTempFile("makeit-", ".pdf",context.cacheDir)
     try{
      val base=if(direct)image else outlineBitmap(image,sensitivity).let{if(outer)outerOnly(it) else it}
      val edited=base.copy(Bitmap.Config.ARGB_8888,true)
      val canvas=android.graphics.Canvas(edited)
      val pen=android.graphics.Paint().apply{strokeWidth=3f;strokeCap=android.graphics.Paint.Cap.ROUND}
      for((a,b,e) in exportStrokes){pen.color=if(e)android.graphics.Color.WHITE else android.graphics.Color.BLACK;canvas.drawLine(a.x*edited.width,a.y*edited.height,b.x*edited.width,b.y*edited.height,pen)}
      file.outputStream().use{exportPattern(edited,plan,it,skip,line)}
      android.os.ParcelFileDescriptor.open(file,android.os.ParcelFileDescriptor.MODE_READ_ONLY).use{fd->android.graphics.pdf.PdfRenderer(fd).use{check(it.pageCount>0)}}
      file
     }catch(e:Exception){file.delete();throw e}
    }
    temp=prepared;pendingPdf=prepared;saver.launch(name)
   }catch(e:Exception){temp?.delete();pendingPdf=null;busy=false;status=extra[3]+" ("+e.javaClass.simpleName+")"}
  }
 }
 var pendingReplace by remember { mutableStateOf<(() -> Unit)?>(null) }
 fun replaceDraft(action:()->Unit){if(bitmap!=null)pendingReplace=action else action()}
 val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if(it != null) {projectId=null;projectName="";readyTemplate=false;isolate=false;cutOnly=false;left=0f;top=0f;right=1f;bottom=1f;zoom=1f;pan=Offset.Zero;uri=it} }
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
 fun saveCurrent(after:()->Unit){
 val b=bitmap ?: return
 if(layout==null||busy)return
 val id=projectId ?: java.util.UUID.randomUUID().toString()
 val json=snapshot().put("name",projectName.ifBlank{id.take(8)}).put("image",id+".png")
 busy=true
 scope.launch{
 try{
 withContext(Dispatchers.IO){
 val atomic=android.util.AtomicFile(java.io.File(dir,id+".png"))
 val out=atomic.startWrite()
 try{check(b.compress(Bitmap.CompressFormat.PNG,100,out));atomic.finishWrite(out)}catch(e:Exception){atomic.failWrite(out);throw e}
 writeProject(java.io.File(dir,id+".json"),json)
 }
 projectId=id;refresh++;status=more[6];busy=false;after()
 }catch(e:Exception){status=extra[3]}finally{busy=false}
 }
 }
 pendingReplace?.let{action->AlertDialog(onDismissRequest={pendingReplace=null},title={Text(replaceLabels[lang] ?: replaceLabels.getValue("en"))},text={Column{Text(more[5]+" · "+pm[6]);Button(enabled=!busy&&layout!=null,onClick={saveCurrent{pendingReplace=null;action()}}){Text(more[5])}}},confirmButton={TextButton(enabled=!busy,onClick={pendingReplace=null;action()}){Text(pm[2])}},dismissButton={TextButton(onClick={pendingReplace=null}){Text(pm[6])}})}
 deleteId?.let { id -> AlertDialog(onDismissRequest={deleteId=null},title={Text(pm[5]+"?")},confirmButton={TextButton(onClick={
 val file=java.io.File(dir,id+".json")
 runCatching{val j=readProject(file);check(file.delete());java.io.File(dir,j.getString("image")).delete();if(projectId==id){projectId=null;bitmap=null;draftFile.delete();draftImage.delete()};refresh++}.onFailure{status=extra[3]}
 deleteId=null
 }){Text(pm[5])}},dismissButton={TextButton(onClick={deleteId=null}){Text(pm[6])}}) }
 if(openProjects){
 Text(pm[0],style=MaterialTheme.typography.headlineMedium)
 for(file in projects){
 val j=remember(file){runCatching{readProject(file)}.getOrNull()} ?: continue
 Card(modifier=Modifier.fillMaxWidth()){
 Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
 Column(Modifier.weight(1f)){
 TextButton(onClick={onOpenProject(file.nameWithoutExtension)}){Text(j.optString("name",file.nameWithoutExtension),style=MaterialTheme.typography.titleLarge)}
 Text(j.optString("width")+" × "+j.optString("height")+" cm")
 }
 TextButton(onClick={deleteId=file.nameWithoutExtension}){Text("🗑",color=MaterialTheme.colorScheme.error)}
 }
 }
 }
 if(status.isNotBlank())Text(status)
 return
 }
 Text(t[0],style=MaterialTheme.typography.headlineMedium)
 OutlinedButton(onClick={showProjects=!showProjects},modifier=Modifier.fillMaxWidth()){Text(pm[0])}
 if(showProjects){
 for(file in projects){
 val j=remember(file,refresh){runCatching{readProject(file)}.getOrNull()} ?: continue
 var name by remember(file,refresh){mutableStateOf(j.optString("name",file.nameWithoutExtension))}
 Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){
 Column(Modifier.padding(12.dp)){
 OutlinedTextField(name,{name=it},singleLine=true,label={Text(pm[1])})
 Text(j.optString("width")+" × "+j.optString("height")+" cm")
 Row{
 TextButton(onClick={replaceDraft{
 try{
 val b=BitmapFactory.decodeFile(java.io.File(dir,j.getString("image")).path) ?: error("image")
 projectId=file.nameWithoutExtension;projectName=name
 readyTemplate=j.optBoolean("readyTemplate");isolate=j.optBoolean("isolate");cutOnly=j.optBoolean("cutOnly");left=j.optDouble("left",0.0).toFloat();top=j.optDouble("top",0.0).toFloat();right=j.optDouble("right",1.0).toFloat();bottom=j.optDouble("bottom",1.0).toFloat();tolerance=j.optDouble("tolerance",12.0).toFloat();width=j.getString("width");height=j.getString("height");a3=j.optBoolean("a3");threshold=j.optDouble("threshold",80.0).toFloat();exterior=j.optBoolean("exterior");thickness=j.optDouble("thickness",0.5).toFloat();skipBlank=j.optBoolean("skipBlank",true);outline=j.optBoolean("outline",true);lockRatio=j.optBoolean("lockRatio",true)
 strokes.clear();groups.clear();redo.clear()
 val arr=j.optJSONArray("strokes") ?: org.json.JSONArray()
 for(i in 0 until arr.length()){val a=arr.getJSONArray(i);strokes.add(Triple(Offset(a.getDouble(0).toFloat(),a.getDouble(1).toFloat()),Offset(a.getDouble(2).toFloat(),a.getDouble(3).toFloat()),a.getBoolean(4)))}
 val g=j.optJSONArray("groups");if(g!=null)for(i in 0 until g.length())groups.add(g.getInt(i))
 if(groups.sum()!=strokes.size){groups.clear();repeat(strokes.size){groups.add(1)}}
 bitmap=b;zoom=1f;pan=Offset.Zero;showProjects=false
 }catch(e:Exception){status=t[5]}
 }}){Text(pm[2])}
 TextButton(onClick={runCatching{j.put("name",name);writeProject(file,j);if(projectId==file.nameWithoutExtension)projectName=name;refresh++}.onFailure{status=extra[3]}}){Text(pm[3])}
 }
 Row{
 TextButton(onClick={runCatching{
 val id=java.util.UUID.randomUUID().toString()
 val copy=org.json.JSONObject(j.toString())
 java.io.File(dir,j.getString("image")).copyTo(java.io.File(dir,id+".png"))
 copy.put("image",id+".png").put("name",name+" (2)")
 writeProject(java.io.File(dir,id+".json"),copy);refresh++
 }.onFailure{status=extra[3]}}){Text(pm[4])}
 TextButton(onClick={deleteId=file.nameWithoutExtension}){Text(pm[5])}
 }
 }
 }
 }
 }

 OutlinedTextField(projectName,{projectName=it},label={Text(pm[1])},singleLine=true,modifier=Modifier.fillMaxWidth())
 Button(onClick={replaceDraft{picker.launch("image/*")}},modifier=Modifier.fillMaxWidth()){Text(t[1])}
 if(bitmap!=null&&!readyTemplate){
 Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(isolate,{isolate=it;if(it)outline=true});Text(seg[0])}
 if(isolate){
 Text(seg[1])
 Text("←");Slider(left,{left=it},valueRange=0f..(right-0.05f).coerceAtLeast(0.01f))
 Text("↑");Slider(top,{top=it},valueRange=0f..(bottom-0.05f).coerceAtLeast(0.01f))
 Text("→");Slider(right,{right=it},valueRange=(left+0.05f).coerceAtMost(0.99f)..1f)
 Text("↓");Slider(bottom,{bottom=it},valueRange=(top+0.05f).coerceAtMost(0.99f)..1f)
 Text(seg[2]);Slider(tolerance,{tolerance=it},valueRange=2f..40f)
 Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(cutOnly,{cutOnly=it});Text(seg[3])}
 }
 if(processing)LinearProgressIndicator(Modifier.fillMaxWidth())
 if(emptyContour)Text(seg[4],color=MaterialTheme.colorScheme.error)
 }
 preview?.let { b ->
 Box(Modifier.fillMaxWidth().aspectRatio(b.width.toFloat()/b.height).clipToBounds().pointerInput(editing){if(!editing)detectTransformGestures { _,delta,scale,_ -> zoom=(zoom*scale).coerceIn(1f,6f);pan=if(zoom==1f)Offset.Zero else pan+delta }}) {
 Box(Modifier.matchParentSize().graphicsLayer { scaleX=zoom;scaleY=zoom;translationX=pan.x;translationY=pan.y }) {
 Image(b.asImageBitmap(),contentDescription=t[1],modifier=Modifier.matchParentSize().background(Color.White),contentScale=ContentScale.FillBounds)
 Canvas(Modifier.matchParentSize().pointerInput(editing,erase,b){if(editing)detectDragGestures(onDragStart={gestureStart=strokes.size},onDragEnd={if(strokes.size>gestureStart)groups.add(strokes.size-gestureStart)},onDragCancel={if(strokes.size>gestureStart)groups.add(strokes.size-gestureStart)}) { change,drag ->
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
 OutlinedButton(enabled=strokes.isNotEmpty(),onClick={val n=if(groups.isEmpty())1 else groups.removeAt(groups.lastIndex);redo.add(strokes.takeLast(n));repeat(n){strokes.removeAt(strokes.lastIndex)}}){Text(more[0])}
 OutlinedButton(enabled=redo.isNotEmpty(),onClick={val group=redo.removeAt(redo.lastIndex);strokes.addAll(group);groups.add(group.size)}){Text(more[1])}
 }
 Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){
 OutlinedButton(onClick={holeMode=1}){Text(more[2])}
 OutlinedButton(onClick={holeMode=2}){Text(more[3])}
 }
 if(holeMode!=0){
 var cx by remember { mutableStateOf(0.5f) }
 var cy by remember { mutableStateOf(0.5f) }
 var holeW by remember { mutableStateOf("20") }
 var holeH by remember { mutableStateOf("15") }
 Text("X");Slider(cx,{cx=it},valueRange=0.1f..0.9f)
 Text("Y");Slider(cy,{cy=it},valueRange=0.1f..0.9f)
 OutlinedTextField(holeW,{holeW=it},label={Text(t[2]+" (cm)")},singleLine=true)
 if(holeMode==2)OutlinedTextField(holeH,{holeH=it},label={Text(t[3]+" (cm)")},singleLine=true)
 val dw=holeW.replace(',','.').toDoubleOrNull()
 val dh=if(holeMode==1)dw else holeH.replace(',','.').toDoubleOrNull()
 val rx=if(dw!=null&&w!=null&&w>0) (dw/w/2).toFloat() else 0f
 val ry=if(dh!=null&&h!=null&&h>0) (dh/h/2).toFloat() else 0f
 Button(enabled=rx>0&&ry>0&&rx<=minOf(cx,1-cx)&&ry<=minOf(cy,1-cy),onClick={
 for(i in 0 until 96){
 fun point(j:Int):Offset{val angle=j*2*Math.PI/96;return Offset((cx+rx*kotlin.math.cos(angle).toFloat()).coerceIn(0f,1f),(cy+ry*kotlin.math.sin(angle).toFloat()).coerceIn(0f,1f))}
 strokes.add(Triple(point(i),point(i+1),false))
 }
 groups.add(96);redo.clear();holeMode=0
 }){Text(more[4])}
 }
 Row { Checkbox(editing,{editing=it});Text(options[4],Modifier.padding(top=12.dp)) }
 if(editing){ Row { Checkbox(erase,{erase=it});Text(options[5],Modifier.padding(top=12.dp)) };OutlinedButton(onClick={strokes.clear();groups.clear();redo.clear()}){Text(options[6])} }
 if(!readyTemplate){ Row { Checkbox(exterior,{exterior=it;outline=true});Text(options[0],Modifier.padding(top=12.dp)) } }
 Row { Checkbox(skipBlank,{skipBlank=it});Text(options[1],Modifier.padding(top=12.dp)) }
 Text(options[2]+" · "+String.format(java.util.Locale.ROOT,"%.1f",thickness)+" mm")
 Slider(thickness,{thickness=it},valueRange=0.2f..1f,steps=7)
 Row { Checkbox(lockRatio,{lockRatio=it});Text(options[3],Modifier.padding(top=12.dp)) }
 if(!readyTemplate){ Row { Checkbox(checked=outline,onCheckedChange={outline=it}); Text(extra[0],modifier=Modifier.padding(top=12.dp)) }
 if(outline) Slider(value=threshold,onValueChange={threshold=it},valueRange=4f..250f) }
 if(failed) Text(t[5],color=MaterialTheme.colorScheme.error)
 OutlinedTextField(width,{width=it; val n=it.replace(',','.').toDoubleOrNull(); val b=bitmap;if(lockRatio&&n!=null&&b!=null)height=(n*b.height/b.width).toString()},label={Text(t[2]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 OutlinedTextField(height,{height=it; val n=it.replace(',','.').toDoubleOrNull(); val b=bitmap;if(lockRatio&&n!=null&&b!=null)width=(n*b.width/b.height).toString()},label={Text(t[3]+" (cm)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=!a3,onClick={a3=false},label={Text("A4")});FilterChip(selected=a3,onClick={a3=true},label={Text("A3")})}
 if(layout!=null){
  Text(t[4]+": "+layout.count+" ("+layout.columns+" × "+layout.rows+") + 1",style=MaterialTheme.typography.titleLarge)
  Text("10 mm / 10 mm · 100%")
  if(layout.count<=400) BoxWithConstraints(Modifier.fillMaxWidth().height(220.dp).background(Color.White,RoundedCornerShape(12.dp))){
   val totalW=(layout.columns-1)*layout.stepX+layout.usableW
   val totalH=(layout.rows-1)*layout.stepY+layout.usableH
   val scale=minOf(maxWidth.value/totalW,maxHeight.value/totalH)
   Box(Modifier.size((totalW*scale).dp,(totalH*scale).dp)){
    preview?.let { Image(it.asImageBitmap(),null,Modifier.size((layout.width*scale).dp,(layout.height*scale).dp),contentScale=ContentScale.FillBounds) }
    Canvas(Modifier.matchParentSize()){
     val sx=size.width/totalW;val sy=size.height/totalH
     for((a,z,e) in strokes)drawLine(if(e)Color.White else Color.Black,Offset((a.x*layout.width*sx).toFloat(),(a.y*layout.height*sy).toFloat()),Offset((z.x*layout.width*sx).toFloat(),(z.y*layout.height*sy).toFloat()),strokeWidth=1f)
     for(row in 0 until layout.rows)for(col in 0 until layout.columns){
      drawRect(Color(0xFF668A7B),Offset(col*layout.stepX*sx,row*layout.stepY*sy),androidx.compose.ui.geometry.Size(layout.usableW*sx,layout.usableH*sy),style=androidx.compose.ui.graphics.drawscope.Stroke(1f))
     }
    }
   }
  }
  Button(enabled=bitmap!=null&&!busy,onClick={saveCurrent{}},modifier=Modifier.fillMaxWidth()){Text(more[5])}
  Button(enabled=preview!=null&&!busy&&!processing&&!emptyContour&&(!outline||processed!=null)&&layout.count<=400,onClick={preparePdf()},modifier=Modifier.fillMaxWidth()){Text(extra[1])}
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

private val projectLabels=mapOf(
"en" to listOf("Projects","Name","Open","Rename","Duplicate","Delete","Cancel"),
"fi" to listOf("Projektit","Nimi","Avaa","Nimeä uudelleen","Kopioi","Poista","Peruuta"),
"et" to listOf("Projektid","Nimi","Ava","Nimeta ümber","Dubleeri","Kustuta","Loobu"),
"ru" to listOf("Проекты","Название","Открыть","Переименовать","Дублировать","Удалить","Отмена"),
"sv" to listOf("Projekt","Namn","Öppna","Byt namn","Duplicera","Ta bort","Avbryt"),
"de" to listOf("Projekte","Name","Öffnen","Umbenennen","Duplizieren","Löschen","Abbrechen"),
"fr" to listOf("Projets","Nom","Ouvrir","Renommer","Dupliquer","Supprimer","Annuler"),
"pl" to listOf("Projekty","Nazwa","Otwórz","Zmień nazwę","Duplikuj","Usuń","Anuluj"),
"es" to listOf("Proyectos","Nombre","Abrir","Renombrar","Duplicar","Eliminar","Cancelar"),
"it" to listOf("Progetti","Nome","Apri","Rinomina","Duplica","Elimina","Annulla"),
"lv" to listOf("Projekti","Nosaukums","Atvērt","Pārdēvēt","Dublēt","Dzēst","Atcelt"),
"lt" to listOf("Projektai","Pavadinimas","Atverti","Pervadinti","Dubliuoti","Ištrinti","Atšaukti"),
"nb" to listOf("Prosjekter","Navn","Åpne","Gi nytt navn","Dupliser","Slett","Avbryt"),
"da" to listOf("Projekter","Navn","Åbn","Omdøb","Dupliker","Slet","Annuller"))

private val replaceLabels=mapOf("en" to "Replace current draft?","fi" to "Korvataanko nykyinen luonnos?","et" to "Asendada praegune mustand?","ru" to "Заменить текущий черновик?","sv" to "Ersätta aktuellt utkast?","de" to "Aktuellen Entwurf ersetzen?","fr" to "Remplacer le brouillon actuel ?","pl" to "Zastąpić bieżący szkic?","es" to "¿Reemplazar el borrador actual?","it" to "Sostituire la bozza attuale?","lv" to "Aizstāt pašreizējo melnrakstu?","lt" to "Pakeisti dabartinį juodraštį?","nb" to "Erstatte gjeldende utkast?","da" to "Erstat den aktuelle kladde?")

private val segmentLabels=mapOf(
"en" to listOf("Remove background","Object bounds","Background tolerance","Cut silhouette only","No contour found. Adjust selection or sensitivity."),
"fi" to listOf("Poista tausta","Kohteen rajat","Taustan toleranssi","Vain leikkausääriviiva","Ääriviivaa ei löytynyt. Säädä valintaa tai herkkyyttä."),
"et" to listOf("Eemalda taust","Objekti piirid","Tausta tolerants","Ainult lõikekontuur","Kontuuri ei leitud. Muuda valikut või tundlikkust."),
"ru" to listOf("Удалить фон","Границы объекта","Допуск удаления фона","Только контур резки","Контур не найден. Измените область или чувствительность."),
"sv" to listOf("Ta bort bakgrund","Objektgränser","Bakgrundstolerans","Endast skärkontur","Ingen kontur hittades. Justera urval eller känslighet."),
"de" to listOf("Hintergrund entfernen","Objektgrenzen","Hintergrundtoleranz","Nur Schnittkontur","Keine Kontur gefunden. Auswahl oder Empfindlichkeit ändern."),
"fr" to listOf("Supprimer le fond","Limites de l’objet","Tolérance du fond","Contour de découpe seul","Aucun contour trouvé. Ajustez la sélection ou la sensibilité."),
"pl" to listOf("Usuń tło","Granice obiektu","Tolerancja tła","Tylko kontur cięcia","Nie znaleziono konturu. Zmień zaznaczenie lub czułość."),
"es" to listOf("Eliminar fondo","Límites del objeto","Tolerancia del fondo","Solo contorno de corte","No se encontró contorno. Ajusta selección o sensibilidad."),
"it" to listOf("Rimuovi sfondo","Limiti oggetto","Tolleranza sfondo","Solo contorno di taglio","Nessun contorno. Regola selezione o sensibilità."),
"lv" to listOf("Noņemt fonu","Objekta robežas","Fona tolerance","Tikai griešanas kontūra","Kontūra nav atrasta. Mainiet atlasi vai jutību."),
"lt" to listOf("Pašalinti foną","Objekto ribos","Fono tolerancija","Tik pjovimo kontūras","Kontūras nerastas. Keiskite pasirinkimą arba jautrumą."),
"nb" to listOf("Fjern bakgrunn","Objektgrenser","Bakgrunnstoleranse","Kun skjærekontur","Ingen kontur funnet. Juster utvalg eller følsomhet."),
"da" to listOf("Fjern baggrund","Objektgrænser","Baggrundstolerance","Kun skærekontur","Ingen kontur fundet. Juster valg eller følsomhed."))
