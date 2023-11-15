package org.hezistudio.command


import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hezistudio.storage.SetuBean
import org.hezistudio.storage.User
import org.hezistudio.storage.UserSignIn
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
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
//            println(e.message.content)
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
    override val name: String = "背包"
    override val description: String = "查看当前拥有的物品"

    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        if (e.message.size != 2) return false
        if (e.message[1] !is PlainText) return false
        if (e.message[1].content != "背包") return false
        return true
    }

    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val user = dbh.getUser(e.sender.id,e.group.id,e.sender.nick)
        if (user.money==0L){
            e.group.sendMessage("您的背包空空荡荡，什么都没有~")
        }else{
            e.group.sendMessage("您当前有积分${user.money}点")
        }
    }
}


interface SyncCommand{
    val name:String
    val description:String
    //    val acceptTypes:ArrayList<JavaType>
    fun acceptable(e: GroupMessageSyncEvent):Boolean
    suspend fun action(e:GroupMessageSyncEvent)
}
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
        val user = dbh.getUser(tEntity?.id?:target,e.group.id,tEntity?.nick?:"用户${target}")
        val awardsNumber = msg[3].content.trimStart().toLong()

        if (awardsNumber<=0) throw Exception("必须是正整数")
        dbh.addMoney(user,awardsNumber)
        val newUser = dbh.getUser(user.qq,user.firstRegisterGroup,user.nick)
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

object CmdHentaiPic:Command{
    override val name: String = "cancanneed"
    override val description: String = "普通色图功能"
    private val price:Int = 70
    private val lolicon = "https://api.lolicon.app/setu/v2?size=original&size=regular"
    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        val m = e.message
        if (m.size != 2) return false
        if (m[1] is PlainText && m[1].content == name){
            return true
        }
        return false
    }

    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val user = dbh.getUser(e.sender.id,e.group.id,e.sender.nick)
        try{
            if (user.money < price) {
                e.group.sendMessage("抱歉，您的积分不足")
                return
            }
            val urls = try {
                getSetuUrl()
            } catch (exc: Exception) {
                null
            }
            if (urls == null) {
                sendLostMsg(e.group)
            } else {
                val u = urls["regular"]
                if (u == null) {
                    sendLostMsg(e.group)
                } else {
                    val bi = getImage(u)
                    if (bi != null) {
                        sendImage(e.group, bi)
                        dbh.addMoney(user, -price.toLong())
                        e.group.sendMessage("扣除积分${price}点")
                    } else sendLostMsg(e.group)
                }
            }
        }catch (exc:Exception){
            e.group.sendMessage("出错啦！")
            throw Exception(exc)
        }
    }

    private suspend fun sendLostMsg(g:Group){
        g.sendMessage("链接丢失，跑到异次元啦！")
    }

    private suspend fun sendImage(g:Group,img:BufferedImage){
        val opt = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            ImageIO.write(img, "PNG", opt)
        }
        val exRes = ByteArrayInputStream(opt.toByteArray()).use {
            it.toExternalResource()
        }
        g.sendImage(exRes)
        withContext(Dispatchers.IO) {
            exRes.close()
            opt.close()
        }

    }
    private fun getSetuUrl():Map<String,String>?{
        val client = OkHttpClient.Builder()
            .connectTimeout(50000, TimeUnit.MILLISECONDS)
            .readTimeout(50000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
//        .proxy(Proxy(Proxy.Type.SOCKS,InetSocketAddress(7890)))
            .build()
        val request = Request.Builder().url(lolicon)
            .method("GET",null)
            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0")
            .build()
        val response = client.newCall(request).execute()
//    println(response)
        return if (response.isSuccessful){
    //        println(response.body!!.byteStream())
            val ipt = response.body!!.charStream()
            val a = ipt.readText()
    //            println(a)
            val gson = Gson()
            val obj = gson.fromJson<SetuBean>(a, SetuBean::class.java)
            obj.data[0].urls
        }else{
            null
        }
    }

    private fun getImage(url:String):BufferedImage?{
        val client = OkHttpClient.Builder()
            .connectTimeout(50000,TimeUnit.MILLISECONDS)
            .readTimeout(50000,TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
//            .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(7890)))
//        .cache(Cache(File("okCache"),1024*8*1024*30))
            .build()
        val request = Request.Builder().url(url)
            .method("GET",null)
            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0")
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful && response.code == 200){
//            println("responseCode = 200")
            if(response.body!=null){
//                println("Not Empty Body")
//            response.close()
                val ipt = response.body!!.byteStream()
//            val bytes = ipt.readBytes()
//            println("getArray：${bytes.size}")
                val buffer = ByteArray(1024*8)
                val opt = ByteArrayOutputStream()
                var n = ipt.read(buffer)
                println("read buffer size:$n")
                while(n!=-1){
                    opt.write(buffer,0,n)
                    n = ipt.read(buffer)
                    println("read buffer size:$n")
                }
                ipt.close()
                val ba = ByteArrayInputStream(opt.toByteArray()).use{
                    ImageIO.read(it)
                }
                opt.close()
                return ba
            }
        }
        return null
    }

}