package fi.vladi.makeit
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import kotlin.math.roundToInt

internal fun exportNumberPainting(result:NumberPainting,layout:SheetLayout,out:OutputStream,lineMm:Float=0.25f){
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
  if(!layout.fitOne)c.drawText("$index / ${layout.count}  R${row+1} C${col+1}  100%",10f,7f,paint)
  c.save();c.clipRect(10f,10f,10f+layout.usableW,10f+layout.usableH)
  c.translate(10f-col*layout.stepX+if(layout.fitOne)(layout.usableW-w)/2 else 0f,10f-row*layout.stepY+if(layout.fitOne)(layout.usableH-h)/2 else 0f)
  paint.isFilterBitmap=true
  val vector=Path(result.boundaries)
  vector.transform(Matrix().apply{setScale(w/result.outline.width,h/result.outline.height)})
  paint.style=Paint.Style.STROKE;paint.strokeWidth=lineMm;paint.strokeCap=Paint.Cap.ROUND;paint.strokeJoin=Paint.Join.ROUND
  c.drawPath(vector,paint);paint.style=Paint.Style.FILL
  paint.textAlign=Paint.Align.CENTER
  for(r in result.regions){
   val x=(r.x+0.5f)*w/result.outline.width;val y=(r.y+0.5f)*h/result.outline.height
   val radius=r.clearance*minOf(w/result.outline.width,h/result.outline.height)
   val label=(r.color+1).toString()
   paint.textSize=2.5f
   val extent=kotlin.math.hypot(paint.measureText(label)/2f,(paint.descent()-paint.ascent())/2f)
   paint.textSize=minOf(2.5f,2.5f*radius*0.65f/extent)
   if(paint.textSize<1.2f)continue
   c.drawText((r.color+1).toString(),x,y-(paint.ascent()+paint.descent())/2,paint)
  }
  c.restore();doc.finishPage(p)
 }
 val p=page(layout.count+1);val c=p.canvas;c.scale(pt,pt)
 paint.textAlign=Paint.Align.LEFT;paint.textSize=3.5f
 for(i in result.palette.indices){
  val columns=if(layout.landscape)6 else 4
  val x=12f+(i%columns)*46f;val y=15f+(i/columns)*12f
  paint.color=result.palette[i];c.drawRect(x,y,x+9,y+8,paint)
  paint.color=Color.BLACK;c.drawText("${i+1} #%06X".format(result.palette[i] and 0xffffff),x+11,y+6,paint)
 }
 val previewTop=if(layout.landscape)115f else 165f
 val previewW=(layout.paperW-90).toFloat();val previewH=(layout.paperH-previewTop-12).coerceAtLeast(1f)
 val previewScale=minOf(previewW/result.preview.width,previewH/result.preview.height)
 c.drawBitmap(result.preview,null,RectF(80f,previewTop,80f+result.preview.width*previewScale,previewTop+result.preview.height*previewScale),paint)
 val calibrationY=if(layout.landscape)125f else 170f
 paint.style=Paint.Style.STROKE;paint.strokeWidth=.2f;c.drawRect(15f,calibrationY,65f,calibrationY+50f,paint)
 paint.style=Paint.Style.FILL;c.drawText("50 × 50 mm / 100%",15f,calibrationY+56f,paint)
 doc.finishPage(p);doc.writeTo(out);out.flush()
 }finally{doc.close()}
}
