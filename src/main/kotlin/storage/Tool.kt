package org.hezistudio.storage

import java.awt.Color
import java.awt.GradientPaint
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage

fun drawSignIn(user: User,signIn: UserSignIn,work: Work,avatar:BufferedImage):BufferedImage{
    val base = BufferedImage(500,300,BufferedImage.TYPE_INT_ARGB)
    val g2d = base.createGraphics()
    val colorA = Color(255,0,0,100)
    val colorB = Color(255,0,0,0)
    val gradient = GradientPaint(0f,0f,colorA,100f,100f,colorB)
    g2d.paint = gradient
    g2d.drawImage(avatar,0,0,avatar.width,avatar.height,null)
    g2d.dispose()
    return base
}