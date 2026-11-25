package fi.vladi.makeit
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import kotlin.math.roundToInt

internal fun exportNumberPainting(result:NumberPainting,layout:SheetLayout,out:OutputStream){
 require(layout.count in 1..400)
 val doc=PdfDocument()
 val pt=72f/25.4f
 val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 val w=layout.width.toFloat();val h=layout.height.toFloat()
 fun page(index:Int)=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),index).create())
 try{
 for(row in 0 until layout.rows)for(col in 0 until layout.columns){
  val index=row*layout.columns+col+1
  val p=page(index);val c=p.canvas;c.scale(pt,pt)
  paint.color=Color.BLACK;paint.textSize=3f;paint.textAlign=Paint.Align.LEFT
  c.drawText("$index / ${layout.count}  R${row+1} C${col+1}  100%",10f,7f,paint)
  c.save();c.clipRect(10f,10f,10f+layout.usableW,10f+layout.usableH)
  c.translate(10f-col*layout.stepX,10f-row*layout.stepY)
  c.drawBitmap(result.outline,null,RectF(0f,0f,w,h),paint)
  paint.textAlign=Paint.Align.CENTER
  for(r in result.regions){
   val x=(r.x+0.5f)*w/result.outline.width;val y=(r.y+0.5f)*h/result.outline.height
   paint.textSize=2.5f
   c.drawText((r.color+1).toString(),x,y-(paint.ascent()+paint.descent())/2,paint)
  }
  c.restore();doc.finishPage(p)
 }
 val p=page(layout.count+1);val c=p.canvas;c.scale(pt,pt)
 paint.textAlign=Paint.Align.LEFT;paint.textSize=3.5f
 for(i in result.palette.indices){
  val x=12f+(i%4)*46f;val y=15f+(i/4)*12f
  paint.color=result.palette[i];c.drawRect(x,y,x+9,y+8,paint)
  paint.color=Color.BLACK;c.drawText("${i+1} #%06X".format(result.palette[i] and 0xffffff),x+11,y+6,paint)
 }
 paint.style=Paint.Style.STROKE;paint.strokeWidth=.2f;c.drawRect(15f,170f,65f,220f,paint)
 paint.style=Paint.Style.FILL;c.drawText("50 × 50 mm / 100%",15f,226f,paint)
 doc.finishPage(p);doc.writeTo(out);out.flush()
 }finally{doc.close()}
}
