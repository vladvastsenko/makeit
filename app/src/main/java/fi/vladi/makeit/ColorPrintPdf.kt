package fi.vladi.makeit
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import kotlin.math.roundToInt

internal fun exportColorPrint(bitmap:Bitmap,layout:SheetLayout,out:OutputStream){
 require(layout.count in 1..400)
 val doc=PdfDocument();val pt=72f/25.4f
 val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 fun page(i:Int)=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),i).create())
 try{
 for(row in 0 until layout.rows)for(col in 0 until layout.columns){
  val i=row*layout.columns+col+1;val p=page(i);val c=p.canvas
  c.scale(pt,pt);paint.color=Color.BLACK;paint.textSize=3f
  c.drawText("$i / ${layout.count}  R${row+1} C${col+1}  100%",10f,7f,paint)
  c.save();c.clipRect(10f,10f,10f+layout.usableW,10f+layout.usableH)
  c.translate(10f-col*layout.stepX,10f-row*layout.stepY)
  c.drawBitmap(bitmap,null,RectF(0f,0f,layout.width.toFloat(),layout.height.toFloat()),paint)
  c.restore();paint.style=Paint.Style.STROKE;paint.strokeWidth=.15f
  c.drawRect(10f,10f,10f+layout.usableW,10f+layout.usableH,paint)
  paint.style=Paint.Style.FILL;doc.finishPage(p)
 }
 val p=page(layout.count+1);p.canvas.scale(pt,pt)
 paint.style=Paint.Style.STROKE;paint.strokeWidth=.2f;p.canvas.drawRect(20f,20f,70f,70f,paint)
 paint.style=Paint.Style.FILL;paint.textSize=4f;p.canvas.drawText("50 × 50 mm / 100%",20f,78f,paint)
 doc.finishPage(p);doc.writeTo(out);out.flush()
 }finally{doc.close()}
}
