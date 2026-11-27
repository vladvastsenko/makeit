package fi.vladi.makeit
import android.graphics.*
internal fun slotMaskPath(source:Bitmap):Path{
 val b=Bitmap.createScaledBitmap(source,256,256,false)
 val left=IntArray(256){256};val right=IntArray(256){-1}
 for(y in 0..255)for(x in 0..255)if(Color.alpha(b.getPixel(x,y))>127){left[y]=minOf(left[y],x);right[y]=maxOf(right[y],x)}
 val first=right.indexOfFirst{it>=0};val last=right.indexOfLast{it>=0}
 require(first>=0&&last-first>=20)
 require((first..last).all{right[it]>=left[it]})
 val bottom=(last-(last-first)*.03f).toInt();require(right[bottom]-left[bottom]>=8);require((first until bottom).all{left[it]<=right[it+1]+1&&left[it+1]<=right[it]+1})
 return Path().apply{moveTo(left[first].toFloat(),first.toFloat());for(y in first+1..bottom)lineTo(left[y].toFloat(),y.toFloat());for(y in bottom downTo first)lineTo(right[y].toFloat(),y.toFloat());close()}
}
internal fun slotConnected(path:Path,w:Float,h:Float):Boolean{
 val n=384;val b=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888)
 Canvas(b).apply{scale((n-2)/w,(n-2)/h);translate(w/(n-2),h/(n-2));drawPath(path,Paint().apply{color=Color.WHITE})}
 val pixels=IntArray(n*n);b.getPixels(pixels,0,n,0,0,n,n);b.recycle()
 val start=pixels.indexOfFirst{it!=0};if(start<0)return false
 val queue=IntArray(n*n);var head=0;var tail=1;queue[0]=start;pixels[start]=0
 fun visit(i:Int){if(pixels[i]!=0){pixels[i]=0;queue[tail++]=i}}
 while(head<tail){val i=queue[head++];if(i%n>0)visit(i-1);if(i%n<n-1)visit(i+1);if(i>=n)visit(i-n);if(i<n*(n-1))visit(i+n)}
 return pixels.none{it!=0}
}
