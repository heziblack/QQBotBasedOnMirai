package org.hezistudio.storage

import java.awt.Color
import java.awt.GradientPaint
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

private val colorList = arrayListOf<Color>(
    Color.WHITE, Color.ORANGE
)

fun drawSignIn(user: User, avatar:BufferedImage):BufferedImage{
    val baseWidth = 1100
    val baseHeight = 640
    val avatarX = baseWidth-baseHeight
    val floatZero = 0f
    val base = BufferedImage(baseWidth,baseHeight,BufferedImage.TYPE_INT_ARGB)
    val g2d = base.createGraphics()
    val colorA = colorList.random()
    val colorB = colorAlphaZ(colorA)
    val gradient = GradientPaint(avatarX.toFloat(),floatZero,colorA,baseWidth.toFloat(),floatZero,colorB)
    g2d.paint = colorA
    g2d.fillRect(0,0,avatarX,baseHeight)
    g2d.paint = gradient
    g2d.drawImage(avatar,avatarX,0,avatar.width,avatar.height,null)
    g2d.fillRect(avatarX,0,avatar.width,avatar.height)
    g2d.dispose()
    return base
}
private fun colorAlphaZ(origin:Color):Color{
    return Color(origin.red,origin.green,origin.blue,0)
}
fun getImageFromUrl(url: String): BufferedImage? {
    try {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connectTimeout = 30000 // 设置连接超时时间为30秒
        urlConnection.inputStream.use { inputStream ->
            val byteOutputStream = ByteArrayOutputStream()
            inputStream.copyTo(byteOutputStream)
            val bytes = byteOutputStream.toByteArray()
            val byteIn = ByteArrayInputStream(bytes)
            return ImageIO.read(byteIn)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

