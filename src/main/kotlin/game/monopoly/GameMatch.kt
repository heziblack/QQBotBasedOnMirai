package org.hezistudio.game.monopoly
/**游戏对局，游戏核心管理的对象*/
class GameMatch(
    val matchID:Long
)
{
    /**对局状态*/
    var status:MatchStatus = MatchStatus.Free
    /**对局中当前行动的玩家*/
    var thePlayer:MatchPlayer? = null


}