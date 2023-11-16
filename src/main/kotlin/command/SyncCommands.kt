package org.hezistudio.command

import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import org.hezistudio.storage.DatabaseHelper

object AwardByHost:SyncCommand{
    override val name: String = "奖励"
    override val description: String = "奖励玩家积分"
    private val regex = Regex("""[1-9][0-9]{0,5}""")
    override fun acceptable(e: GroupMessageSyncEvent): Boolean {
        // GroupMessageSyncEvent
        if (e.message.size != 4) return false
        if (e.message[1] !is PlainText) return false
        if (e.message[1].content != "奖励") return false
        if (e.message[2] !is At) return false
        if (e.message[3] !is PlainText) return false
        return regex.matches(e.message[3].content.trimStart())
    }

    override suspend fun action(e: GroupMessageSyncEvent) {
        val msg = e.message
        val target = (msg[2] as At).target
        val tEntity = e.group.members[target]
        val user = DatabaseHelper.getUser(tEntity?.id?:target,e.group.id,tEntity?.nick?:"用户${target}")
        val awardsNumber = msg[3].content.trimStart().toLong()

        if (awardsNumber<=0) throw Exception("必须是正整数")
        DatabaseHelper.addMoney(user,awardsNumber)
        val newUser = DatabaseHelper.getUser(user.qq,user.firstRegisterGroup,user.nick)
        e.group.sendMessage("成功给[${newUser.nick}]增加[${awardsNumber}]点积分, ta的当前积分:${newUser.money}")
    }
}
object Test:SyncCommand{
    override val name: String = "测试"
    override val description: String = "自用测试模块"

    override fun acceptable(e: GroupMessageSyncEvent): Boolean {
        return (e.message.size == 2 && e.message[1].content == "cs")
    }

    override suspend fun action(e: GroupMessageSyncEvent) {
        e.group.sendMessage("测试模块未加载任何内容")
    }

}