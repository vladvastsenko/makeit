package fi.vladi.makeit
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import kotlin.math.roundToInt

internal fun slotShape(id:Int):Path=Path().apply{
 when(id){
 0->{moveTo(30f,0f);lineTo(70f,0f);lineTo(70f,15f);lineTo(82f,15f);lineTo(82f,21f);lineTo(69f,21f);cubicTo(85f,32f,76f,42f,65f,46f);cubicTo(91f,52f,88f,66f,76f,71f);cubicTo(105f,80f,100f,94f,88f,100f);lineTo(12f,100f);cubicTo(0f,94f,-5f,80f,24f,71f);cubicTo(12f,66f,9f,52f,35f,46f);cubicTo(24f,42f,15f,32f,31f,21f);lineTo(18f,21f);lineTo(18f,15f);lineTo(30f,15f)}
 1->{moveTo(50f,0f);lineTo(77f,35f);lineTo(65f,35f);lineTo(90f,65f);lineTo(76f,65f);lineTo(100f,90f);lineTo(60f,90f);lineTo(60f,100f);lineTo(40f,100f);lineTo(40f,90f);lineTo(0f,90f);lineTo(24f,65f);lineTo(10f,65f);lineTo(35f,35f);lineTo(23f,35f)}
 2->{moveTo(50f,27f);cubicTo(12f,-18f,5f,5f,28f,36f);cubicTo(0f,50f,20f,63f,28f,64f);cubicTo(-8f,80f,0f,100f,24f,100f);lineTo(76f,100f);cubicTo(100f,100f,108f,80f,72f,64f);cubicTo(80f,63f,100f,50f,72f,36f);cubicTo(95f,5f,88f,-18f,50f,27f)}
 else->{moveTo(43f,0f);lineTo(57f,0f);lineTo(57f,15f);cubicTo(95f,0f,100f,35f,100f,55f);cubicTo(100f,90f,80f,100f,65f,100f);lineTo(35f,100f);cubicTo(20f,100f,0f,90f,0f,55f);cubicTo(0f,35f,5f,0f,43f,15f)}
 };close()
}
internal fun slotPart(id:Int,w:Float,h:Float,gap:Float,top:Boolean,custom:Path?=null):Path{
 require(w.isFinite()&&h.isFinite()&&gap.isFinite()&&w>0&&h>0&&gap>0&&gap<w*.08f+0.001f)
 val p=custom?.let{Path(it)}?:slotShape(id);val bounds=RectF();val measure=PathMeasure(p,true);val xy=FloatArray(2);require(measure.length>0f);bounds.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);for(i in 0..8192){measure.getPosTan(measure.length*i/8192f,xy,null);bounds.left=minOf(bounds.left,xy[0]);bounds.top=minOf(bounds.top,xy[1]);bounds.right=maxOf(bounds.right,xy[0]);bounds.bottom=maxOf(bounds.bottom,xy[1])};require(bounds.width()>0f&&bounds.height()>0f)
 p.transform(Matrix().apply{setRectToRect(bounds,RectF(0f,0f,w,h),Matrix.ScaleToFit.FILL)})
 val notch=Path().apply{addRect(w/2-gap/2,if(top)-1f else h/2,w/2+gap/2,if(top)h/2 else h+1f,Path.Direction.CW)}
 check(p.op(notch,Path.Op.DIFFERENCE));check(!p.isEmpty);return p
}
internal fun exportSlots(id:Int,layout:SheetLayout,gap:Float,out:OutputStream,custom:Path?=null){
 require(layout.count in 1..200)
 val doc=PdfDocument();val pt=72f/25.4f;val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 var index=0
 try{
 for(part in 0..1){
 val path=slotPart(id,layout.width.toFloat(),layout.height.toFloat(),gap,part==1,custom)
 for(row in 0 until layout.rows)for(col in 0 until layout.columns){
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),++index).create())
 val c=page.canvas;c.scale(pt,pt);paint.color=Color.BLACK;paint.style=Paint.Style.FILL;paint.textSize=3f
 c.drawText("${if(part==0)"A" else "B"}  R${row+1} C${col+1}  100%  /  ${gap} mm",10f,7f,paint)
 c.save();c.clipRect(9.8f,9.8f,10.2f+layout.usableW,10.2f+layout.usableH);c.translate(10f-col*layout.stepX,10f-row*layout.stepY)
 paint.style=Paint.Style.STROKE;paint.strokeWidth=.25f;c.drawPath(path,paint);c.restore();paint.strokeWidth=.15f;paint.pathEffect=DashPathEffect(floatArrayOf(1f,2f),0f);c.drawRect(10f,10f,10f+layout.usableW,10f+layout.usableH,paint);paint.pathEffect=null;doc.finishPage(page)
 }}
 val page=doc.startPage(PdfDocument.PageInfo.Builder((layout.paperW*pt).roundToInt(),(layout.paperH*pt).roundToInt(),++index).create())
 page.canvas.scale(pt,pt);paint.style=Paint.Style.STROKE;page.canvas.drawRect(20f,20f,70f,70f,paint);paint.style=Paint.Style.FILL;page.canvas.drawText("50 x 50 mm / 100%",20f,78f,paint);doc.finishPage(page)
 doc.writeTo(out);out.flush()
 }finally{doc.close()}
}
