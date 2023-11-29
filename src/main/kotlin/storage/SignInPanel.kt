package org.hezistudio.storage

import java.awt.image.BufferedImage
import java.awt.*
import java.awt.geom.Ellipse2D
import kotlin.math.max
import kotlin.math.roundToInt

class SignInPanel(var userSignForDrawInfo: UserSignForDrawInfo,
                  var avatar: BufferedImage) {
    private val baseWidth = 1100
    private val baseHeight = 640
    private val avatarX = baseWidth-baseHeight
    private val floatZero = 0f
    private val base:BufferedImage = BufferedImage(baseWidth,baseHeight,BufferedImage.TYPE_INT_ARGB)
    private val theme = ThemeColor.values().random()
    private val g2d: Graphics2D = base.createGraphics()
    fun create():BufferedImage{
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT)
        drawBase(g2d)
        drawUserNick(g2d)
        drawID(g2d)
        drawNickIcon()
        drawMoney(g2d)
        drawEllipse(g2d,userSignForDrawInfo.workPay,80,400,"累计收益")
        drawEllipse(g2d,userSignForDrawInfo.workTime,230,400,"累计打工")
        drawEllipse(g2d,userSignForDrawInfo.hentai,380,400,"涩涩次数")
        return base
    }

    private fun drawNickIcon(){
        g2d.paint = theme.second
        g2d.fillRoundRect(52,21,45,84,10,10)
    }

    private fun drawBase(g2d:Graphics2D){
        val colorA = theme.main
        val colorB = colorAlphaZ(colorA)
        val gradient = GradientPaint(avatarX.toFloat(),floatZero,colorA,baseWidth.toFloat(),floatZero,colorB)
        g2d.paint = colorA
        g2d.fillRect(0,0,avatarX,baseHeight)
        g2d.paint = gradient
        g2d.drawImage(avatar,avatarX,0,avatar.width,avatar.height,null)
        g2d.fillRect(avatarX,0,avatar.width,avatar.height)
    }

    private fun drawUserNick(g2d: Graphics2D){
        g2d.paint = theme.fontColor
        g2d.font = Font("幼圆", Font.PLAIN,50)
        g2d.drawString(userSignForDrawInfo.nick,110,70)
    }

    private fun drawID(g2d: Graphics2D){
        g2d.paint = theme.fontColor
        g2d.font = Font("幼圆", Font.BOLD,18)
        g2d.drawString("ID:${userSignForDrawInfo.qq}",110,100)
    }

    private fun drawMoney(g2d:Graphics2D){
        g2d.paint = theme.fontColor
        g2d.font = Font("幼圆", Font.PLAIN,30)
        g2d.drawString("积分：",52,200)
        g2d.font = Font("幼圆", Font.PLAIN,45)
        g2d.drawString("${userSignForDrawInfo.money}",100,250)
    }

    private fun drawEllipse(g2d: Graphics2D, value:Long, centerPosX:Int,
                            centerPosY: Int, title:String){
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
        g2d.paint = theme.fourth
        g2d.fillArc(centerPosX-outerRadius,centerPosY-outerRadius,2*outerRadius,2*outerRadius,-90,arc)
        g2d.paint = theme.main
        g2d.fill(innerCircle)
        g2d.paint = theme.third
        g2d.draw(innerCircle)
        g2d.draw(outerCircle)
        // 绘制环形图的环内数字
        var fontSize = 50f
        g2d.font = g2d.font.deriveFont(fontSize)
        while (true){
            val metrics = g2d.getFontMetrics(g2d.font.deriveFont(fontSize))
            val textWidth = metrics.stringWidth(value.toString())
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
        g2d.paint = theme.fontColor
        g2d.font = g2d.font.deriveFont(fontSize)
        g2d.drawString(value.toString(),centerPosX-tw,centerPosY+th)
        // 绘制环形表的标题
        g2d.font = g2d.font.deriveFont(30f)
        g2d.font = g2d.font.deriveFont(Font.BOLD)
        fm = g2d.fontMetrics
        tw = fm.stringWidth(title)/2
        th = fm.ascent
        g2d.drawString(title,centerPosX-tw,centerPosY+outerRadius+th+15)
    }

    private fun colorAlphaZ(origin:Color):Color{
        return Color(origin.red,origin.green,origin.blue,0)
    }

}