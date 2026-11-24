package fi.vladi.makeit
import android.graphics.Bitmap
import android.graphics.Color
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

internal fun smartSelection(source:Bitmap,left:Float,top:Float,right:Float,bottom:Float,tolerance:Int):Bitmap {
 if(!OpenCVLoader.initLocal()) return isolatePattern(source,left,top,right,bottom,tolerance)
 val rgba=Mat();val rgb=Mat();val mask=Mat();val bg=Mat();val fg=Mat();val binary=Mat();val labels=Mat();val stats=Mat();val centers=Mat();val largest=Mat();val hierarchy=Mat()
 val contours=ArrayList<MatOfPoint>()
 try {
 Utils.bitmapToMat(source,rgba)
 val scale=minOf(1.0,640.0/maxOf(rgba.cols(),rgba.rows()))
 Imgproc.resize(rgba,rgba,Size(rgba.cols()*scale,rgba.rows()*scale))
 Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB)
 val w=rgb.cols();val h=rgb.rows()
 if(w<8||h<8)return source
 val x=(left*w).toInt().coerceIn(1,w-3);val y=(top*h).toInt().coerceIn(1,h-3)
 val x2=(right*w).toInt().coerceIn(x+1,w-1);val y2=(bottom*h).toInt().coerceIn(y+1,h-1)
 Imgproc.grabCut(rgb,mask,Rect(x,y,x2-x,y2-y),bg,fg,5,Imgproc.GC_INIT_WITH_RECT)
 Core.bitwise_and(mask,Scalar(1.0),binary)
 val count=Imgproc.connectedComponentsWithStats(binary,labels,stats,centers)
 if(count<=1)return isolatePattern(source,left,top,right,bottom,tolerance)
 var best=1
 for(i in 2 until count)if(stats.get(i,Imgproc.CC_STAT_AREA)[0]>stats.get(best,Imgproc.CC_STAT_AREA)[0])best=i
 Core.compare(labels,Scalar(best.toDouble()),largest,Core.CMP_EQ)
 Imgproc.findContours(largest.clone(),contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE)
 // Fill tiny enclosed artifacts; retain substantial enclosed holes.
 for(i in contours.indices){
 val parent=hierarchy.get(0,i)?.get(3) ?: -1.0
 if(parent>=0&&Imgproc.contourArea(contours[i])<w*h*0.001)Imgproc.drawContours(largest,contours,i,Scalar(255.0),-1)
 }
 Imgproc.resize(largest,largest,Size(source.width.toDouble(),source.height.toDouble()),0.0,0.0,Imgproc.INTER_NEAREST)
 val alpha=ByteArray(source.width*source.height);largest.get(0,0,alpha)
 val pixels=IntArray(alpha.size);source.getPixels(pixels,0,source.width,0,0,source.width,source.height)
 for(i in pixels.indices)if(alpha[i].toInt()==0)pixels[i]=Color.TRANSPARENT
 return Bitmap.createBitmap(pixels,source.width,source.height,Bitmap.Config.ARGB_8888)
 }catch(e:Exception){return isolatePattern(source,left,top,right,bottom,tolerance)}
 finally{contours.forEach{it.release()};listOf(rgba,rgb,mask,bg,fg,binary,labels,stats,centers,largest,hierarchy).forEach{it.release()}}
}
