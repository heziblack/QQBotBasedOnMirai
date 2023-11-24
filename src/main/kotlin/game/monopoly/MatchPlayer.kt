package org.hezistudio.game.monopoly

/**大富翁对局中的玩家实体*/
class MatchPlayer(
    val playerID:Long,
)
{
    /**该玩家指向的下一个玩家*/
    var next:MatchPlayer? = this
    /**玩家在游戏中的状态*/
    var status:PlayerStatus = PlayerStatus.InGame

}