package org.hezistudio.game.farm.plants

interface Plant {
    val name:String
    /**计算上次操作到本次操作间隔时间，将植物在此段时间内的演化结果更新*/
    fun evolution()
    /***/
    

}