package org.hezistudio.storage

import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.roundToInt
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
    BlackOliver(
        Color(31, 39, 27),
        Color(158, 145, 186),
        Color(77, 57, 88),
        Color(123, 75, 148),
        Color(192, 214, 223)
    ),
    Dark(
        Color(6, 24, 38),
        Color(47, 39, 63),
        Color(54, 133, 181),
        Color(4, 113, 166),
        Color(31, 170, 230)
    ),
    Light(
        Color(255, 226, 209),
        Color(225, 240, 196),
        Color(107, 171, 144),
        Color(85, 145, 127),
        Color(94, 76, 90)
    ),
    Cute(
        Color(229, 138, 206),
        Color(234, 166, 215),
        Color(196, 85, 168),
        Color(169, 76, 99),
        Color(96, 53, 46)
    )
}

fun drawSignIn(user: User, avatar:BufferedImage):BufferedImage{
    val userInfo = getSignInfo(user)
    val baseWidth = 1100
    val baseHeight = 640
    val avatarX = baseWidth-baseHeight
    val floatZero = 0f
    val base = BufferedImage(baseWidth,baseHeight,BufferedImage.TYPE_INT_ARGB)
    val g2d = base.createGraphics()
    val theme = ThemeColor.values().random()
    val colorA = theme.main
    val colorB = colorAlphaZ(colorA)
    val gradient = GradientPaint(avatarX.toFloat(),floatZero,colorA,baseWidth.toFloat(),floatZero,colorB)
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT)
    g2d.paint = colorA
    g2d.fillRect(0,0,avatarX,baseHeight)
    g2d.paint = gradient
    g2d.drawImage(avatar,avatarX,0,avatar.width,avatar.height,null)
    g2d.fillRect(avatarX,0,avatar.width,avatar.height)
    drawUserNick(g2d,userInfo.nick,theme.fontColor)
    drawID(g2d,userInfo.qq,theme.fontColor)
    drawNickIcon(g2d,theme.second)
    drawMoney(g2d,userInfo.money,theme.fontColor)
    drawEllipse(g2d,userInfo.workPay,80,400,theme,"累计收益")
    drawEllipse(g2d,userInfo.workTime,230,400,theme,"累计打工")
    drawEllipse(g2d,userInfo.hentai,380,400,theme,"涩涩次数")
    g2d.dispose()
    return base
}
fun drawUserNick(g2d: Graphics2D, userNick:String, color:Color){
    g2d.paint = color
    g2d.font = Font("幼圆", Font.PLAIN,50)
    g2d.drawString(userNick,110,70)
}

fun drawNickIcon(g2d: Graphics2D,color: Color){
    g2d.paint = color
    g2d.fillRoundRect(52,21,45,84,10,10)
}
fun drawID(g2d: Graphics2D,id:Long,color: Color){
    g2d.paint = color
    g2d.font = Font("幼圆", Font.BOLD,18)
    g2d.drawString("ID:${id}",110,100)
}

fun drawMoney(g2d:Graphics2D,money:Long,color: Color){
    g2d.paint = color
    g2d.font = Font("幼圆", Font.PLAIN,30)
    g2d.drawString("积分：",52,200)
    g2d.font = Font("幼圆", Font.PLAIN,45)
    g2d.drawString("$money",100,250)
}

fun drawEllipse(g2d: Graphics2D,value:Long,centerPosX:Int,
                        centerPosY: Int,themeColor: ThemeColor,title:String){

    val innerRadius = 50
    val outerRadius = 60
    val innerCircle = Ellipse2D.Float(
        (centerPosX-innerRadius).toFloat(),
        (centerPosY-innerRadius).toFloat(),
        2f * innerRadius,
        2f * innerRadius
    )
    val outerCircle = Ellipse2D.Float(
        (centerPosX-outerRadius).toFloat(),
        (centerPosY-outerRadius).toFloat(),
        2f * outerRadius,
        2f * outerRadius
    )
    var max = 1L
    while (true){
        if (max(max,value) <= value){
            max *= 10
        }else{
            break
        }
    }
    val arc = (value.toDouble()/max.toDouble()*360).roundToInt()
    g2d.font = g2d.font.deriveFont(30f)
    g2d.font = g2d.font.deriveFont(Font.PLAIN)
    g2d.paint = themeColor.fourth
    g2d.fillArc(centerPosX-outerRadius,centerPosY-outerRadius,2*outerRadius,2*outerRadius,-90,arc)
    g2d.paint = themeColor.main
    g2d.fill(innerCircle)
    g2d.paint = themeColor.third
    g2d.draw(innerCircle)
    g2d.draw(outerCircle)
    var fontSize = 50f
    g2d.font = g2d.font.deriveFont(fontSize)
    while (true){
        val metrics = g2d.getFontMetrics(g2d.font.deriveFont(fontSize))
        val textWidth = metrics.stringWidth(value.toString())
//        println("fontSize:${fontSize}--textWidth:${textWidth}--${innerRadius*2}")
        if (textWidth>innerRadius*2){
            fontSize-=1f
            if (fontSize<=1){
                break
            }
            continue
        }
        break
    }
    var fm = g2d.getFontMetrics(g2d.font.deriveFont(fontSize))
    var tw = fm.stringWidth(value.toString())/2
    var th = fm.ascent/2
    g2d.paint = themeColor.fontColor
    g2d.font = g2d.font.deriveFont(fontSize)
    g2d.drawString(value.toString(),centerPosX-tw,centerPosY+th)
    g2d.font = g2d.font.deriveFont(30f)
    g2d.font = g2d.font.deriveFont(Font.BOLD)
    fm = g2d.fontMetrics
    tw = fm.stringWidth(title)/2
    th = fm.ascent
    g2d.drawString(title,centerPosX-tw,centerPosY+outerRadius+th)


}


fun colorAlphaZ(origin:Color):Color{
    return Color(origin.red,origin.green,origin.blue,0)
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
        work.moneyCounter,
        user.hentai
    )
}