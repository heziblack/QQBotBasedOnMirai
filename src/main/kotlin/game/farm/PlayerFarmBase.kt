package org.hezistudio.game.farm

/**
 * @property id 玩家的唯一识别ID
 * @property community 玩家所属群组，一般是群号
 * */
abstract class PlayerFarmBase(val id:Long,var community:Long) {
    /**玩家农场中的地块项目列表*/
    val farmFields = arrayListOf<FarmField>()
    /**玩家农场中的道具列表, 以键值对形式存取玩家道具和数量，k=道具id，v=道具数量*/
//    val items = arrayListOf<Item>()
    val items:MutableMap<Int,Long> = mutableMapOf()
    /***/

}