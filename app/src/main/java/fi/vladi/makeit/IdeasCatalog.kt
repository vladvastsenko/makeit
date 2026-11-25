package fi.vladi.makeit
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

private val labels=mapOf(
 "en" to "Girls|Boys|Pairs|With animals|Christmas gingerbread".split('|'),
 "fi" to "Tytöt|Pojat|Parit|Eläinten kanssa|Joulupiparit".split('|'),
 "et" to "Tüdrukud|Poisid|Paarid|Loomadega|Jõulupiparkoogid".split('|'),
 "ru" to "Девочки|Мальчики|Пары|С животными|Рождественские пряники".split('|'),
 "sv" to "Flickor|Pojkar|Par|Med djur|Julpepparkakor".split('|'),
 "de" to "Mädchen|Jungen|Paare|Mit Tieren|Weihnachtslebkuchen".split('|'),
 "fr" to "Filles|Garçons|Duos|Avec des animaux|Pain d’épices de Noël".split('|'),
 "pl" to "Dziewczynki|Chłopcy|Pary|Ze zwierzętami|Pierniki świąteczne".split('|'),
 "es" to "Niñas|Niños|Parejas|Con animales|Galletas navideñas".split('|'),
 "it" to "Bambine|Bambini|Coppie|Con animali|Pan di zenzero natalizio".split('|'),
 "lv" to "Meitenes|Zēni|Pāri|Ar dzīvniekiem|Ziemassvētku piparkūkas".split('|'),
 "lt" to "Mergaitės|Berniukai|Poros|Su gyvūnais|Kalėdiniai meduoliai".split('|'),
 "nb" to "Jenter|Gutter|Par|Med dyr|Julepepperkaker".split('|'),
 "da" to "Piger|Drenge|Par|Med dyr|Julepeberkager".split('|')
)
private data class CatalogImage(val category:Int,val number:Int,val file:String)
private fun catalog(context:Context):List<CatalogImage>{
 val json=JSONArray(context.assets.open("catalog.json").bufferedReader().use{it.readText()})
 return buildList {
  for(c in 0 until json.length()){
   val images=json.getJSONObject(c).getJSONArray("images")
   for(i in 0 until images.length())add(CatalogImage(c,i+1,images.getJSONObject(i).getString("file").removeSuffix(".png")+".webp"))
  }
 }
}
internal fun ideaName(context:Context,lang:String,id:Int):String{
 val item=catalog(context)[id]
 return (labels[lang]?:labels.getValue("en"))[item.category]+" "+item.number
}
internal fun ideaBitmap(context:Context,id:Int,size:Int=2048):Bitmap{
 val file=catalog(context)[id].file
 val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
 context.assets.open(file).use{BitmapFactory.decodeStream(it,null,bounds)}
 var sample=1
 while(maxOf(bounds.outWidth,bounds.outHeight)/sample>size*2)sample*=2
 val bitmap=context.assets.open(file).use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})} ?: error("Image unavailable")
 val scale=size.toFloat()/maxOf(bitmap.width,bitmap.height)
 if(scale>=1f)return bitmap
 return Bitmap.createScaledBitmap(bitmap,maxOf(1,(bitmap.width*scale).toInt()),maxOf(1,(bitmap.height*scale).toInt()),true).also{if(it!==bitmap)bitmap.recycle()}
}
@Composable
fun IdeasCatalog(lang:String,onCreate:(Int)->Unit){
 val context=LocalContext.current
 val items=remember(context){catalog(context)}
 val titles=labels[lang]?:labels.getValue("en")
 for(category in titles.indices){
  Text(titles[category],style=MaterialTheme.typography.titleLarge)
  val ids=items.indices.filter{items[it].category==category}
  for(row in ids.chunked(2)){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    for(id in row){
     val name=titles[category]+" "+items[id].number
     val preview by produceState<Bitmap?>(null,id){value=withContext(Dispatchers.IO){runCatching{ideaBitmap(context,id,480)}.getOrNull()}}
     Card(onClick={onCreate(id)},modifier=Modifier.weight(1f)){
      Column(Modifier.padding(6.dp)){
       val b=preview
       if(b!=null)Image(b.asImageBitmap(),name,Modifier.fillMaxWidth().aspectRatio(0.75f),contentScale=ContentScale.Fit)
       else Box(Modifier.fillMaxWidth().aspectRatio(0.75f))
      }
     }
    }
    if(row.size==1)Spacer(Modifier.weight(1f))
   }
  }
 }
}
