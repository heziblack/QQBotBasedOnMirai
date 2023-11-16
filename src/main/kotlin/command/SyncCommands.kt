package org.hezistudio.command

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import org.hezistudio.storage.DatabaseHelper
import org.hezistudio.storage.groupWhitelist

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
//        val tw = e.group.members.random()
//        tw.nameCard = "!"+tw.nameCardOrNick
        e.group.sendMessage("当前未加载任何测试模块")
    }

}

object AddOrRemoveService:SyncCommand{
    override val name: String = "添加或移除群服务"
    override val description: String = "将收到指令的群纳入或移出指令执行范围"
    private val cmds:ArrayList<String> = arrayListOf("添加服务","移除服务")

    override fun acceptable(e: GroupMessageSyncEvent): Boolean {
        if(e.message.size != 2) return false
        if (e.message[1] !is PlainText) return false
        if ((e.message[1] as PlainText).content !in cmds) return false
        return true
    }

    override suspend fun action(e: GroupMessageSyncEvent) {
        val cmd = e.message[1].content
        val gid = e.group.id
        when(cmd){
            cmds[0]->{
                if (gid in groupWhitelist){
                    e.group.sendMessage("本群已在服务范围中")
                }else{
                    groupWhitelist.add(gid)
                    e.group.sendMessage("添加成功")
                }
            }
            cmds[1]->{
                if (gid in groupWhitelist){
                    groupWhitelist.remove(gid)
                    e.group.sendMessage("移除成功")
                }else{
                    e.group.sendMessage("本群不在服务范围中")
                }
            }
        }
    }

}