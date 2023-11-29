package org.hezistudio.storage

import java.awt.*
import org.hezistudio.storage.DatabaseHelper as dbh

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

fun getSignInfo(user: User):UserSignForDrawInfo{
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

fun getSignInfo():UserSignForDrawInfo{
    return UserSignForDrawInfo(
        (0L..3000L).random(),
        arrayOf("盒子","涩中恶鬼","怎么办我爱你").random(),
        (0L..3000L).random(),
        false,
        (0L..3000L).random(),
        (0L..3000L).random(),
        (0L..3000L).random(),
    )
}