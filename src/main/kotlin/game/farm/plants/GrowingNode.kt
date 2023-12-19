package org.hezistudio.game.farm.plants

import java.time.Duration

/**植物成长阶段节点*/
class GrowingNode(
    /**阶段名*/
    val nodeName:String,
    /**阶段持续时间-“PT1h32m”*/
    val durationTimeStr:String,
    /**最低水分*/
    val water:Double,
    /**最低营养*/
    val nutrition:Double
)
{
    init {
        try {
            Duration.parse(durationTimeStr)
        }catch (e:Exception){
            throw e
        }
    }

    /**阶段持续时间*/
    val duration:Duration
        get() = Duration.parse(durationTimeStr)
}