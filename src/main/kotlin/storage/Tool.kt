package org.hezistudio.storage

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import org.hezistudio.storage.DatabaseHelper as dbh

private val colorList = arrayListOf<Color>(
    Color.WHITE, Color.ORANGE
)
enum class ThemeColor(
    val main:Color,
    val second:Color,
    val third:Color,
    val fourth:Color,
    val fontColor:Color,
)
{

}

fun drawSignIn(user: User, avatar:BufferedImage):BufferedImage{
    val userInfo = getSignInfo(user)
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
    g2d.paint = Color.BLACK
//    val sb = StringBuilder(userInfo.nick)
//        .append("\t积分:${userInfo.money}\t")
//        .append("打工时长:${userInfo.workTime}\t")
//        .append("累计打工收益:${userInfo.workPay}\t")
//    if (userInfo.isSignIn){
//        sb.append("今日已签到")
//    }else{
//        sb.append("今日未签到")
//    }
//    g2d.drawString(sb.toString(),20,20)
    drawUserNick(g2d,userInfo.nick)
    g2d.dispose()
    return base
}
private fun colorAlphaZ(origin:Color):Color{
    return Color(origin.red,origin.green,origin.blue,0)
}

private fun drawUserNick(g2d:Graphics2D,userNick:String){
    g2d.font = Font("",Font.PLAIN,34)
    g2d.drawString(userNick,50,50)
}

private fun getSignInfo(user: User):UserSignForDrawInfo{
    val signIn = dbh.getUserSignIn(user)?:dbh.createUserSignIn(user)
    val work = dbh.createUserWork(user)
    return UserSignForDrawInfo(
        user.qq,
        user.nick,
        user.money,
        signIn.lastDate == dbh.currentDateTime.date,
        work.timer,
        work.moneyCounter
    )
}