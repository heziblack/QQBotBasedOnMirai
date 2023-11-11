package org.hezistudio.command


import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import org.hezistudio.storage.User
import org.hezistudio.storage.UserSignIn
import org.hezistudio.storage.DatabaseHelper as dbh

interface Command{
    val name:String
    val description:String
//    val acceptTypes:ArrayList<JavaType>
    fun acceptable(e: MessageEvent):Boolean
    suspend fun action(e:MessageEvent)
}

object CmdSignIn:Command{
    override val name: String = "签到"
    override val description: String = "完成每日签到，获取签到奖励哦，只接受群聊签到"
    override fun acceptable(e: MessageEvent): Boolean {
        return if (e is GroupMessageEvent){
            println(e.message.content)
            e.message[1].content == "签到"
        }else false
    }

    override suspend fun action(e: MessageEvent) {
        if (e !is GroupMessageEvent) {
//            MyPluginMain.logger.info(e.javaClass.toString())
            return
        }
        val user = dbh.getUser(e.sender.id, e.group.id, e.sender.nick)
        val lastSignIn = dbh.getUserSignIn(user)
        val newSignIn = newSignIn(user,lastSignIn)
        if (lastSignIn==null){
            e.group.sendMessage("这是你第一天签到")
        }else if (lastSignIn.lastDate == newSignIn.lastDate){
            e.group.sendMessage("你已经签过到了")
        }else{
            e.group.sendMessage("签到成功，你已连续签到${newSignIn.consecutiveDays}天")
        }
    }

    private fun newSignIn(user: User, userSignIn: UserSignIn?):UserSignIn{
        return if (userSignIn!=null){
            dbh.updateUserSignIn(user)
        }else{
            dbh.createUserSignIn(user)
        }
    }

}