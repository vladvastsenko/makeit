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
internal fun exportPattern(bitmap:Bitmap, layout:SheetLayout, output:OutputStream) {
 require(layout.count in 1..400)
 val pt=72f/25.4f
 val doc=PdfDocument()
 val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 try {
 for(row in 0 until layout.rows) for(col in 0 until layout.columns) {
 val index=row*layout.columns+col+1
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),index).create())
 val c=page.canvas
 c.save()
 c.scale(pt,pt)
 c.clipRect(10f,10f,10f+layout.usableW,10f+layout.usableH)
 val x=10f-col*layout.stepX
 val y=10f-row*layout.stepY
 c.drawBitmap(bitmap,null,RectF(x,y,x+layout.width.toFloat(),y+layout.height.toFloat()),paint)
 c.restore()
 c.save()
 c.scale(pt,pt)
 paint.color=Color.BLACK
 paint.style=Paint.Style.STROKE
 paint.strokeWidth=0.2f
 c.drawRect(10f,10f,10f+layout.usableW,10f+layout.usableH,paint)
 paint.style=Paint.Style.FILL
 paint.textSize=3f
 c.drawText("$index / ${layout.count}   R${row+1} C${col+1}   100%",10f,7f,paint)
 c.restore()
 doc.finishPage(page)
 }
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),layout.count+1).create())
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
 val gray=IntArray(pixels.size){i -> val p=pixels[i]; if(Color.alpha(p)<128)255 else (Color.red(p)*299+Color.green(p)*587+Color.blue(p)*114)/1000 }
 val out=IntArray(pixels.size){Color.WHITE}
 for(y in 1 until h-1) for(x in 1 until w-1) {
 val i=y*w+x
 val edge=kotlin.math.abs(gray[i+1]-gray[i-1])+kotlin.math.abs(gray[i+w]-gray[i-w])
 if(edge>=threshold)out[i]=Color.BLACK
 }
 return Bitmap.createBitmap(out,w,h,Bitmap.Config.ARGB_8888)
}
