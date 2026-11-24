package fi.vladi.makeit
import android.content.Context
import java.io.*
import java.util.zip.*
import org.json.JSONObject

internal object ProjectBackup {
 private val valid=Regex("[a-fA-F0-9-]{36}\\.(json|png)")
 fun export(context:Context,output:OutputStream){
  val dir=File(context.filesDir,"projects")
  ZipOutputStream(output).use { zip ->
   dir.listFiles()?.filter{it.isFile&&valid.matches(it.name)}?.forEach{file ->
    zip.putNextEntry(ZipEntry(file.name));file.inputStream().use{it.copyTo(zip)};zip.closeEntry()
   }
  }
 }
 fun restore(context:Context,input:InputStream){
  val target=File(context.filesDir,"projects").apply{mkdirs()}
  val staging=File(context.cacheDir,"restore-"+java.util.UUID.randomUUID()).apply{mkdirs()}
  try {
   var total=0L;var count=0
   ZipInputStream(input).use{zip ->
    while(true){
     val entry=zip.nextEntry ?: break
     require(!entry.isDirectory&&valid.matches(entry.name)&&++count<=2000)
     val file=File(staging,entry.name);require(!file.exists())
     file.outputStream().use{out ->
      val buffer=ByteArray(8192)
      while(true){val n=zip.read(buffer);if(n<0)break;total+=n;require(total<=256L*1024*1024);out.write(buffer,0,n)}
     }
     zip.closeEntry()
    }
   }
   val records=staging.listFiles()?.filter{it.extension=="json"} ?: emptyList()
   val validated=records.map{file ->
    val json=JSONObject(file.readText());val image=json.getString("image")
    require(valid.matches(image)&&image==file.nameWithoutExtension+".png")
    require(File(staging,image).isFile)
    require(json.getString("width").replace(',','.').toDouble() in 0.1..1000.0)
    require(json.getString("height").replace(',','.').toDouble() in 0.1..1000.0)
    file to json
   }
   for((file,json) in validated){
    val id=java.util.UUID.randomUUID().toString()
    val image=File(target,id+".png")
    File(staging,json.getString("image")).copyTo(image)
    try{File(target,id+".json").writeText(json.put("image",image.name).toString())}catch(e:Exception){image.delete();throw e}
   }
  }finally{staging.deleteRecursively()}
 }
}
