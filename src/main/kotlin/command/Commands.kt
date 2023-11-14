package org.hezistudio.command


import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import org.hezistudio.storage.User
import org.hezistudio.storage.UserSignIn
import kotlin.math.pow
import kotlin.math.roundToInt
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
        val awards = signInAwards(newSignIn.consecutiveDays)
        val update = user.money + awards
        if (lastSignIn==null){
            e.group.sendMessage("这是你第一天签到, 获得积分${awards}点")
            dbh.addMoney(user,awards.toLong())
        }else if (lastSignIn.lastDate == newSignIn.lastDate){
            e.group.sendMessage("你已经签过到了, 今日获得签到积分${awards}点, 当前有${user.money}点")
        }else{
            e.group.sendMessage("签到成功, 你已连续签到${newSignIn.consecutiveDays}天, 获得积分${awards}点, 当前有${update}点")
            dbh.addMoney(user,awards.toLong())
        }
    }

    private fun newSignIn(user: User, userSignIn: UserSignIn?):UserSignIn{
        return if (userSignIn!=null){
            dbh.updateUserSignIn(user)
        }else{
            dbh.createUserSignIn(user)
        }
    }

    private fun signInAwards(days:Int):Int{
        val d = days.toDouble()
        return when{
            days < 100 ->{
                val head = ((d+10)/40)
                (((3.0).pow(head) * 8 ) - 8).roundToInt()

            }
            days >= 100 ->{
                val bl = (d-99).pow(0.5) +156
                bl.roundToInt()
            }
            else ->{
                0
            }
        }
    }

}

object CmdBackpack:Command{
    override val name: String
        get() = TODO("Not yet implemented")
    override val description: String
        get() = TODO("Not yet implemented")

    override fun acceptable(e: MessageEvent): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun action(e: MessageEvent) {
        TODO("Not yet implemented")
    }

}