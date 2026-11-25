package fi.vladi.makeit
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.File
import java.util.UUID

internal fun creativeRoot(context:Context)=File(context.filesDir,"creative-projects").apply{mkdirs()}
internal fun creativeSave(context:Context,source:Bitmap,isolated:Bitmap?,settings:JSONObject):String{
 val root=creativeRoot(context);val id=UUID.randomUUID().toString()
 val temp=File(root,".$id");check(temp.mkdir())
 try{
  fun write(name:String,b:Bitmap){File(temp,name).outputStream().use{check(b.compress(Bitmap.CompressFormat.PNG,100,it))}}
  write("source.png",source);if(isolated!=null)write("isolated.png",isolated)
  File(temp,"settings.json").writeText(settings.toString())
  check(temp.renameTo(File(root,id)));return id
 }finally{temp.deleteRecursively()}
}
internal fun creativeList(context:Context)=creativeRoot(context).listFiles()?.filter{it.isDirectory&&!it.name.startsWith(".")&&File(it,"settings.json").isFile}?.sortedByDescending{it.lastModified()}?:emptyList()
internal fun creativeSettings(folder:File)=JSONObject(File(folder,"settings.json").readText())
internal fun creativeImage(folder:File,name:String):Bitmap?=BitmapFactory.decodeFile(File(folder,name).absolutePath)
