package org.hezistudio.command


import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hezistudio.MyPluginMain
import org.hezistudio.storage.SetuBean
import org.hezistudio.storage.User
import org.hezistudio.storage.UserSignIn
import org.hezistudio.storage.proxyPort
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.roundToInt
import org.hezistudio.storage.DatabaseHelper as dbh


object CmdSignIn:Command{
    override val name: String = "签到"
    override val description: String = "完成每日签到，获取签到奖励哦，只接受群聊签到"
    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent){ return false }
        if (e.message.size != 2) return false
        return e.message[1].content == "签到"
    }

    override suspend fun action(e: MessageEvent) {
        if (e !is GroupMessageEvent) { return }
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
object CmdHentaiPic:Command{
    override val name: String = "kknd"
    override val description: String = "普通色图功能"
    private const val SETU_PRICE:Int = 70
    private const val URL_LOLICON = "https://api.lolicon.app/setu/v2?size=original&size=regular"
    private const val YHAPI = "http://api.yujn.cn/api/yht.php?type=image"
    private val clientNormal = OkHttpClient.Builder()
        .connectTimeout(50000,TimeUnit.MILLISECONDS)
        .readTimeout(50000,TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
//        .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyPort)))
        .build()

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
            if (user.money < SETU_PRICE) {
                e.group.sendMessage("抱歉，您的积分不足")
                return
            }
//            加入url选择
            val urls = YHAPI
            val bi = getImageProxy(urls)
            if (bi != null) {
                sendImage(e.group, bi)
                dbh.addMoney(user, -SETU_PRICE.toLong())
                e.group.sendMessage("扣除积分${SETU_PRICE}点")
            } else {
                MyPluginMain.logger.error("未加载到图片")
                sendLostMsg(e.group)
            }
//            if (urls == null) {
//                MyPluginMain.logger.error("url 丢失")
//                sendLostMsg(e.group)
//            } else {
//                val u = urls["regular"]
//                if (u == null) {
//                    MyPluginMain.logger.error("no regular size")
//                    sendLostMsg(e.group)
//                } else {
//                    val bi = getImageProxy(u)
//                    if (bi != null) {
//                        sendImage(e.group, bi)
//                        dbh.addMoney(user, -SETU_PRICE.toLong())
//                        e.group.sendMessage("扣除积分${SETU_PRICE}点")
//                    } else {
//                        MyPluginMain.logger.error("未加载到图片")
//                        sendLostMsg(e.group)
//                    }
//                }
//            }
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
    private fun getLoliconUrls():Map<String,String>?{
        val request = buildRequest(URL_LOLICON)
        val response = try{
            clientNormal.newCall(request).execute()
        }catch (e:Exception){
            MyPluginMain.logger.error(e)
            MyPluginMain.logger.error("lolicon请求失败")
            return null
        }
        return if (response.isSuccessful){
            val ipt = response.body!!.charStream()
            val a = ipt.readText()
            val gson = Gson()
            val obj = gson.fromJson(a, SetuBean::class.java)
            obj.data[0].urls
        }else{
            null
        }
    }

    private fun getSetuUrlRebuild():String{
        TODO()
    }

    private fun buildRequest(url:String):Request{
        return Request.Builder().url(url)
            .method("GET",null)
            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0")
            .build()
    }
    private fun getImageProxy(url:String):BufferedImage?{
        val request = buildRequest(url)
//        val response = if (proxyPort==0){
//            clientNormal.newCall(request).execute()
//        }else{
//        clientProxy.newCall(request).execute()
//        }
        val response = clientNormal.newCall(request).execute()
//        clientProxy.newCall(request).execute()
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
                var count:Long = n.toLong()
//                println("read buffer size:$n")
                while(n!=-1){
                    opt.write(buffer,0,n)
                    n = ipt.read(buffer)
                    count+=n
//                    println("read buffer size:$n")
                }
                MyPluginMain.logger.info("图像大小：${count}Byte")
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
object CmdWorkForMoney:Command{
    override val name: String = "打工"
    override val description: String = "打工换取积分"
    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        val msg = e.message
        if (msg.size != 2) return false
        val text = e.message[1].content
        return text == "打工"
    }

    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val g = e.group
        if (g.id == 190772405L){
//            g.sendMessage("功能还在开发中")
            val hour = (1..8).random()
            val user = dbh.getUser(e.sender.id, e.group.id, e.sender.nick)
            dbh.userGoWorking(user,hour)
            val workInfo = dbh.userWork(user.qq)!!
            val userNew = dbh.getUser(user.qq,user.firstRegisterGroup,user.nick)
            val salary = (hour*(0.8*hour + 10.0)).roundToInt()
            e.group.sendMessage("随机打工${hour}小时，报酬${salary}积分\n" +
                    "打工累计时间${workInfo.timer}小时，累计收益${workInfo.moneyCounter}积分\n" +
                    "接下来${hour}小时内将不再响应您的指令，现有积分${userNew.money}")
        }else{
            g.sendMessage("您不能打工哦，要在群聊190772405才能打工")
        }

    }

}