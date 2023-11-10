package org.hezistudio.command

import net.mamoe.mirai.event.events.GroupMemberEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import org.hezistudio.storage.DatabaseHelper as dbh

interface Command{
    val name:String
    val description:String
    fun acceptable(e: MessageEvent):Boolean
    suspend fun action(e:MessageEvent)
}

object CmdSignIn:Command{
    override val name: String = "签到"
    override val description: String = "完成每日签到，获取签到奖励哦"
    override fun acceptable(e: MessageEvent): Boolean {
        return if (e is GroupMemberEvent){
            e.message.content == name
        }else false
    }

    override suspend fun action(e: MessageEvent) {
        if (e !is GroupMemberEvent) throw Exception("消息类型错误：需要GroupMessageEvent，收到${e.javaClass}")
        val user = dbh.getUser(e.sender.id, e.group.id, e.sender.nick)
        e.group.sendMessage("尊敬的${user.nick}, 功能正在完善中敬请期待")
    }

}