package fi.vladi.makeit
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.roundToInt

internal class SheetLayout(val width: Double,val height: Double,val a3:Boolean) {
 val paperW=if(a3)297 else 210
 val paperH=if(a3)420 else 297
 val usableW=paperW-20
 val usableH=paperH-20
 val stepX=usableW-10
 val stepY=usableH-10
 val columns=ceil((width-10).coerceAtLeast(1.0)/stepX).toInt()
 val rows=ceil((height-10).coerceAtLeast(1.0)/stepY).toInt()
 val count=columns*rows
}
internal fun exportPattern(bitmap:Bitmap, layout:SheetLayout, output:OutputStream, skipBlank:Boolean=true, lineMm:Float=0.5f) {
 require(layout.count in 1..400)
 val pt=72f/25.4f
 val doc=PdfDocument()
 val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 try {
 val pixels=IntArray(bitmap.width*bitmap.height)
 bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
 val vector=vectorLines(pixels,bitmap.width,bitmap.height,layout.width.toFloat(),layout.height.toFloat())
 val tiles=(0 until layout.rows).flatMap { row -> (0 until layout.columns).map { col -> row to col } }.filter { (row,col) ->
 if(!skipBlank) true else {
 val left=(col*layout.stepX/layout.width*bitmap.width).toInt().coerceIn(0,bitmap.width)
 val top=(row*layout.stepY/layout.height*bitmap.height).toInt().coerceIn(0,bitmap.height)
 val right=ceil((col*layout.stepX+layout.usableW)/layout.width*bitmap.width).toInt().coerceIn(0,bitmap.width)
 val bottom=ceil((row*layout.stepY+layout.usableH)/layout.height*bitmap.height).toInt().coerceIn(0,bitmap.height)
 (top until bottom).any { y -> (left until right).any { x -> pixels[y*bitmap.width+x] != Color.WHITE } }
 }
 }
 require(tiles.isNotEmpty())
 for((i,tile) in tiles.withIndex()) {
 val (row,col)=tile
 val index=i+1
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),index).create())
 val c=page.canvas
 c.save()
 c.scale(pt,pt)
 c.clipRect(10f,10f,10f+layout.usableW,10f+layout.usableH)
 val x=10f-col*layout.stepX
 val y=10f-row*layout.stepY
 c.translate(x,y)
 paint.color=Color.BLACK
 paint.style=Paint.Style.STROKE
 paint.strokeWidth=lineMm
 paint.strokeCap=Paint.Cap.ROUND
 paint.strokeJoin=Paint.Join.ROUND
 c.drawPath(vector,paint)
 c.restore()
 c.save()
 c.scale(pt,pt)
 paint.color=Color.BLACK
 paint.style=Paint.Style.STROKE
 paint.strokeWidth=0.2f
 c.drawRect(10f,10f,10f+layout.usableW,10f+layout.usableH,paint)
 paint.style=Paint.Style.FILL
 paint.textSize=3f
 c.drawText("$index / ${tiles.size}   R${row+1} C${col+1}   100%",10f,7f,paint)
 c.restore()
 doc.finishPage(page)
 }
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),tiles.size+1).create())
 page.canvas.scale(pt,pt)
 paint.style=Paint.Style.STROKE
 paint.strokeWidth=0.2f
 page.canvas.drawRect(20f,20f,70f,70f,paint)
 paint.style=Paint.Style.FILL
 paint.textSize=4f
 page.canvas.drawText("50 × 50 mm / 100%",20f,78f,paint)
 doc.finishPage(page)
 doc.writeTo(output)
 } finally { doc.close() }
}
internal fun outlineBitmap(source:Bitmap, threshold:Int):Bitmap {
 val w=source.width; val h=source.height
 val pixels=IntArray(w*h)
 source.getPixels(pixels,0,w,0,0,w,h)
 val rgb=IntArray(pixels.size){i -> val p=pixels[i];val a=Color.alpha(p);fun blend(c:Int)=(c*a+255*(255-a))/255
 Color.rgb(blend(Color.red(p)),blend(Color.green(p)),blend(Color.blue(p))) }
 fun diff(a:Int,b:Int):Int=maxOf(kotlin.math.abs(Color.red(a)-Color.red(b)),kotlin.math.abs(Color.green(a)-Color.green(b)),kotlin.math.abs(Color.blue(a)-Color.blue(b)))
 val strength=IntArray(pixels.size)
 var peak=0
 for(y in 1 until h-1)for(x in 1 until w-1){
 val i=y*w+x
 val e=maxOf(diff(rgb[i-1],rgb[i+1]),diff(rgb[i-w],rgb[i+w]),diff(rgb[i-w-1],rgb[i+w+1]),diff(rgb[i-w+1],rgb[i+w-1]))
 strength[i]=e;peak=maxOf(peak,e)
 }
 val out=IntArray(pixels.size){Color.WHITE}
 val limit=minOf(threshold.coerceAtLeast(4),maxOf(4,peak/3))
 if(peak>=4)for(i in strength.indices)if(strength[i]>=limit)out[i]=Color.BLACK
 return Bitmap.createBitmap(out,w,h,Bitmap.Config.ARGB_8888)
}

internal fun outerOnly(source:Bitmap):Bitmap {
 val w=source.width; val h=source.height
 val p=IntArray(w*h); source.getPixels(p,0,w,0,0,w,h)
 val seen=BooleanArray(p.size); val queue=IntArray(p.size); var head=0; var tail=0
 fun visit(i:Int){if(!seen[i]&&p[i]==Color.WHITE){seen[i]=true;queue[tail++]=i}}
 for(x in 0 until w){visit(x);visit((h-1)*w+x)}
 for(y in 0 until h){visit(y*w);visit(y*w+w-1)}
 while(head<tail){val i=queue[head++];val x=i%w;val y=i/w
 if(x>0)visit(i-1);if(x<w-1)visit(i+1);if(y>0)visit(i-w);if(y<h-1)visit(i+w)}
 val out=IntArray(p.size){i ->
 val x=i%w;val y=i/w
 if(!seen[i]&&(x==0||x==w-1||y==0||y==h-1||seen[i-1]||seen[i+1]||seen[i-w]||seen[i+w]))Color.BLACK else Color.WHITE
 }
 return Bitmap.createBitmap(out,w,h,Bitmap.Config.ARGB_8888)
}
internal fun thicken(source:Bitmap,mm:Float,widthMm:Double,heightMm:Double):Bitmap {
 val w=source.width;val h=source.height
 val p=IntArray(w*h);source.getPixels(p,0,w,0,0,w,h)
 val out=p.copyOf()
 val rx=(mm*w/widthMm/2).toInt().coerceIn(0,32)
 val ry=(mm*h/heightMm/2).toInt().coerceIn(0,32)
 for(y in 0 until h)for(x in 0 until w)if(p[y*w+x]==Color.BLACK)
 for(dy in -ry..ry)for(dx in -rx..rx)if(x+dx in 0 until w&&y+dy in 0 until h)out[(y+dy)*w+x+dx]=Color.BLACK
 return Bitmap.createBitmap(out,w,h,Bitmap.Config.ARGB_8888)
}

private fun vectorLines(p:IntArray,w:Int,h:Int,wm:Float,hm:Float):Path {
 val path=Path()
 val sx=wm/w;val sy=hm/h
 val visited=BooleanArray(p.size)
 fun neighbors(i:Int):List<Int>{
 val x=i%w;val y=i/w
 return buildList{
 for(dy in -1..1)for(dx in -1..1){
 if(dx==0&&dy==0)continue
 if(x+dx !in 0 until w||y+dy !in 0 until h)continue
 val j=i+dy*w+dx
 if(p[j]!=Color.BLACK)continue
 if(dx!=0&&dy!=0&&(p[i+dx]==Color.BLACK||p[i+dy*w]==Color.BLACK))continue
 add(j)
 }
 }
 }
 fun trace(start:Int){
 val points=ArrayList<Int>();var current=start
 while(!visited[current]){
 visited[current]=true;points.add(current)
 val next=neighbors(current).firstOrNull{!visited[it]} ?: break
 current=next
 }
 fun x(i:Int)=(i%w+0.5f)*sx
 fun y(i:Int)=(i/w+0.5f)*sy
 path.moveTo(x(start),y(start))
 if(points.size==1){path.lineTo(x(start)+0.001f,y(start));return}
 for(k in 1 until points.lastIndex){
 val a=points[k];val b=points[k+1]
 path.quadTo(x(a),y(a),(x(a)+x(b))/2,(y(a)+y(b))/2)
 }
 val end=points.last()
 path.lineTo(x(end),y(end))
 if(points.size>3&&start in neighbors(end))path.close()
 }
 for(i in p.indices)if(p[i]==Color.BLACK&&!visited[i]&&neighbors(i).size<=1)trace(i)
 for(i in p.indices)if(p[i]==Color.BLACK&&!visited[i])trace(i)
 return path
}
