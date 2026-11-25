package fi.vladi.makeit
import android.graphics.*
import kotlin.math.*

internal data class PaintRegion(val x:Int,val y:Int,val color:Int,val area:Int,val clearance:Int)
internal data class NumberPainting(val preview:Bitmap,val outline:Bitmap,val palette:IntArray,val regions:List<PaintRegion>,val boundaries:Path)
internal fun numberPainting(source:Bitmap,colors:Int,detail:Int,minArea:Int):NumberPainting{
 val scale=minOf(1f,detail.toFloat()/maxOf(source.width,source.height))
 val w=maxOf(1,(source.width*scale).roundToInt());val h=maxOf(1,(source.height*scale).roundToInt())
 val b=Bitmap.createScaledBitmap(source,w,h,true)
 val pixels=IntArray(w*h);b.getPixels(pixels,0,w,0,0,w,h)
 if(b!==source)b.recycle()
 for(i in pixels.indices){val p=pixels[i];val a=Color.alpha(p);pixels[i]=Color.rgb((Color.red(p)*a+255*(255-a))/255,(Color.green(p)*a+255*(255-a))/255,(Color.blue(p)*a+255*(255-a))/255)}
 fun dist(a:Int,b:Int):Int{val r=Color.red(a)-Color.red(b);val g=Color.green(a)-Color.green(b);val z=Color.blue(a)-Color.blue(b);return r*r+g*g+z*z}
 val samples=pixels.filterIndexed{i,_->i%maxOf(1,pixels.size/12000)==0}
 val palette=IntArray(colors.coerceIn(4,48))
 palette[0]=samples[samples.size/2]
 val distances=IntArray(samples.size){Int.MAX_VALUE}
 for(k in 1 until palette.size){for(i in samples.indices)distances[i]=minOf(distances[i],dist(samples[i],palette[k-1]));palette[k]=samples[distances.indices.maxByOrNull{distances[it]}?:0]}
 fun nearest(p:Int):Int=palette.indices.minByOrNull{dist(p,palette[it])}?:0
 repeat(8){
  val r=LongArray(palette.size);val g=LongArray(palette.size);val z=LongArray(palette.size);val n=IntArray(palette.size)
  for(p in samples){val k=nearest(p);r[k]+=Color.red(p).toLong();g[k]+=Color.green(p).toLong();z[k]+=Color.blue(p).toLong();n[k]++}
  for(k in palette.indices)if(n[k]>0)palette[k]=Color.rgb((r[k]/n[k]).toInt(),(g[k]/n[k]).toInt(),(z[k]/n[k]).toInt())
 }
 val labels=IntArray(pixels.size){nearest(pixels[it])}
 val queue=IntArray(labels.size)
 val clearance=IntArray(labels.size)
 fun neighbors(i:Int,action:(Int)->Unit){if(i%w>0)action(i-1);if(i%w<w-1)action(i+1);if(i>=w)action(i-w);if(i+w<labels.size)action(i+w)}
 fun scan(merge:Boolean):List<PaintRegion>{
  val seen=BooleanArray(labels.size);val regions=mutableListOf<PaintRegion>()
  for(start in labels.indices){
   if(seen[start])continue
   val k=labels[start];var end=1;var pos=0;queue[0]=start;seen[start]=true
   var sx=0L;var sy=0L;val border=IntArray(palette.size)
   while(pos<end){val i=queue[pos++];sx+=(i%w).toLong();sy+=(i/w).toLong();neighbors(i){j->if(labels[j]==k){if(!seen[j]){seen[j]=true;queue[end++]=j}}else border[labels[j]]++}}
   if(merge&&end<minArea){val target=border.indices.filter{it!=k&&border[it]>0}.maxByOrNull{border[it]};if(target!=null)for(q in 0 until end)labels[queue[q]]=target}
   if(!merge){val cx=sx.toDouble()/end;val cy=sy.toDouble()/end;var best=start;var score=Double.MAX_VALUE
    for(q in 0 until end){val i=queue[q];val dx=i%w-cx;val dy=i/w-cy;val d=dx*dx+dy*dy-1000000.0*clearance[i];if(d<score){score=d;best=i}}
    regions.add(PaintRegion(best%w,best/w,k,end,clearance[best]))
   }
  }
  return regions
 }
 repeat(3){
  val old=labels.copyOf()
  for(i in labels.indices){val counts=IntArray(palette.size);val x=i%w;val y=i/w
   for(yy in maxOf(0,y-1)..minOf(h-1,y+1))for(xx in maxOf(0,x-1)..minOf(w-1,x+1))counts[old[yy*w+xx]]++
   val best=counts.indices.maxByOrNull{counts[it]}?:old[i]
   if(counts[best]>=5)labels[i]=best
  }
 }
 repeat(8){scan(true)}
 for(i in labels.indices){val x=i%w;val y=i/w;var edge=x==0||y==0||x==w-1||y==h-1;neighbors(i){if(labels[it]!=labels[i])edge=true};clearance[i]=if(edge)0 else w+h}
 for(i in labels.indices){if(i%w>0)clearance[i]=minOf(clearance[i],clearance[i-1]+1);if(i>=w)clearance[i]=minOf(clearance[i],clearance[i-w]+1)}
 for(i in labels.indices.reversed()){if(i%w<w-1)clearance[i]=minOf(clearance[i],clearance[i+1]+1);if(i+w<labels.size)clearance[i]=minOf(clearance[i],clearance[i+w]+1)}
 val regions=scan(false)
 val flat=IntArray(labels.size){palette[labels[it]]}
 val lines=IntArray(labels.size){Color.WHITE}
 for(i in labels.indices){if((i%w<w-1&&labels[i]!=labels[i+1])||(i+w<labels.size&&labels[i]!=labels[i+w]))lines[i]=Color.BLACK}
 val boundaries=Path()
 for(y in 0 until h)for(x in 0 until w){val i=y*w+x;if(x+1<w&&labels[i]!=labels[i+1]){boundaries.moveTo(x+1f,y.toFloat());boundaries.lineTo(x+1f,y+1f)};if(y+1<h&&labels[i]!=labels[i+w]){boundaries.moveTo(x.toFloat(),y+1f);boundaries.lineTo(x+1f,y+1f)}}
 return NumberPainting(Bitmap.createBitmap(flat,w,h,Bitmap.Config.ARGB_8888),Bitmap.createBitmap(lines,w,h,Bitmap.Config.ARGB_8888),palette,regions,boundaries)
}
