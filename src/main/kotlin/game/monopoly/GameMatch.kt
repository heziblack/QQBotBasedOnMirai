package org.hezistudio.game.monopoly
/**游戏对局，游戏核心管理的对象*/
class GameMatch(
    val matchID:Long
)
{
    var status:Status = Status.Free

}