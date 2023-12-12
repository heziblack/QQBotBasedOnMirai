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
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hezistudio.MyPluginMain
import org.hezistudio.storage.*
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.hezistudio.storage.DatabaseHelper as dbh

class CmdCommonUtils{
    suspend fun sendImage(g: Group, img: BufferedImage){
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

    suspend fun getImageFromUrl(url: String): BufferedImage? {
        try {
            val urlConnection = withContext(Dispatchers.IO){ URL(url).openConnection() as HttpURLConnection }
            urlConnection.connectTimeout = 30000 // 设置连接超时时间为30秒
            urlConnection.inputStream.use { inputStream ->
                val byteOutputStream = ByteArrayOutputStream()
                inputStream.copyTo(byteOutputStream)
                val bytes = byteOutputStream.toByteArray()
                val byteIn = ByteArrayInputStream(bytes)
                return ImageIO.read(byteIn)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun resizeAvatar(originalImage:BufferedImage):BufferedImage{
        // 创建一个新的BufferedImage对象，目标尺寸为640x640
        val resizedImage = BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB)
        // 创建一个Graphics2D对象来处理图像缩放
        val g: Graphics2D = resizedImage.createGraphics()
        // 设置抗锯齿和质量设置
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        // 缩放原始图像至新图像
        g.drawImage(originalImage, 0, 0, 640, 640, null)
        // 清理并关闭Graphics2D对象
        g.dispose()
        return resizedImage
    }

}
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
        val tool = CmdCommonUtils()
        var userIcon = tool.getImageFromUrl(e.sender.avatarUrl)?: withContext(Dispatchers.IO) {
            ImageIO.read(File(MyPluginMain.dataFolder, "DefaultAvatar.jpg"))
        }
        if (userIcon.width != 640){
            userIcon = tool.resizeAvatar(userIcon)
        }
        if (lastSignIn==null || lastSignIn.lastDate != newSignIn.lastDate){
            dbh.addMoney(user,awards.toLong())
            val userInfo = getSignInfo(dbh.getUser(user))
            val img = SignInPanel(userInfo,userIcon).create()
            tool.sendImage(e.group,img)
        }else{
            e.group.sendMessage("你已经签过到了, 今日获得签到积分${awards}点, 当前有${user.money}点")
            return
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
        .build()
    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        val m = e.message
        if (m.size != 2) return false
        if (m[1] !is PlainText) return false
        if (m[1].content == name){
            return true
        }else if (m[1].content == "涩涩"){
            return true
        }
        return false
    }
    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val user = dbh.getUser(e.sender.id,e.group.id,e.sender.nick)
        val p = if (e.group.id == 190772405L){
            7
        }else{
            SETU_PRICE
        }
        try{
            if (user.money < p) {
                e.group.sendMessage("抱歉，您的积分不足")
                return
            }
            e.group.sendMessage("加载中，请耐心等待")
            val urls = randomUrl()
            if (urls == ""){
                sendLostMsg(e.group)
                return
            }
            val bi = getImageProxy(urls)
            if (bi != null) {
                var ls: Int
                val imageFolder = File(MyPluginMain.dataFolder,"pixivImages").also {
                    if (!(it.exists() && it.isDirectory)){
                        Files.createDirectories(it.toPath())
                    }
                    ls = it.list()?.size ?: 0
                }
                val imageFile = File(imageFolder,"${ls+1}.jpg")
                withContext(Dispatchers.IO) {
                    ImageIO.write(ImageIO.read(bi),"JPG",imageFile)
                    bi.close()
                }
                if (dbh.getUser(user).money>=p){
                    withContext(Dispatchers.IO) {
                        val er = imageFile.toExternalResource()
                        e.group.sendMessage(er.uploadAsImage(e.group))
                        er.close()
                    }
                    dbh.hentaiCounter(user)
                    dbh.addMoney(user, -p.toLong())
                    e.group.sendMessage("扣除积分${p}点")
                }else{
                    e.group.sendMessage("积分不足加载失败")
                }
            } else {
                MyPluginMain.logger.error("未加载到图片")
                sendLostMsg(e.group)
            }
        }catch (exc:Exception){
            e.group.sendMessage("出错啦！")
            MyPluginMain.logger.error(exc)
            throw Exception(exc)
        }
    }
    private suspend fun sendLostMsg(g:Group){
        g.sendMessage("链接丢失，跑到异次元啦！")
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
    private fun randomUrl():String{
        return when((0..1).random()){
            1 -> getLoliconUrl()
            else -> {
                YHAPI
            }
        }
    }
    private fun getLoliconUrl():String{
        val urls = getLoliconUrls()
        val targetUrl = urls?.get("regular")
        return targetUrl?:""
    }
    private fun buildRequest(url:String):Request{
        return Request.Builder().url(url)
            .method("GET",null)
            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0")
            .build()
    }
    private suspend fun getImageProxy(url:String):InputStream?{
        val connect = withContext(Dispatchers.IO){
            URL(url).openConnection().also {
                it.connect()
            } as HttpURLConnection
        }
        if (connect.responseCode in
            (HttpURLConnection.HTTP_MULT_CHOICE..HttpURLConnection.HTTP_BAD_REQUEST)){
            var second = withContext(Dispatchers.IO) {
                URL(connect.getHeaderField("Location")).openConnection().also {
                    it.connect()
                } as HttpURLConnection
            }
            for (i in (1..10)) {
                if (second.responseCode in
                    (HttpURLConnection.HTTP_MULT_CHOICE..HttpURLConnection.HTTP_BAD_REQUEST)
                ) {
                    second = withContext(Dispatchers.IO) {
                        URL(connect.getHeaderField("Location")).openConnection().also {
                            it.connect()
                        } as HttpURLConnection
                    }
                }else{
                    break
                }
            }
            return if (second.responseCode==200){
                withContext(Dispatchers.IO){ second.inputStream }
            }else{
                null
            }
        }else{
            return withContext(Dispatchers.IO){ connect.inputStream }
        }
    }
}
object CmdWorkForMoney:Command{
    override val name: String = "打工"
    override val description: String = "打工换取积分"
    private val regex = Regex("打工 ([1-9]|1[0-9]|2[0-4])")
    private val regex2 = Regex("打工")
    private val mistake = arrayListOf(
        Pair("由于你的疏忽，导致商品损坏，店主从工资中扣除损失金额。",0.8),
        Pair("做家教时，学生家长不满意教学质量，扣减了部分课酬。",0.7),
        Pair("在餐厅打工时，你误操作导致顾客食物浪费，老板因此扣减工资。",0.3),
        Pair("兼职促销员未能达到销售目标，公司按照协议扣除相应提成。",0.35),
        Pair("打印店兼职因操作失误打印了大量废纸，雇主要求承担成本并扣发相应工资。",0.2),
        Pair("实习时在工作中犯了错误，丢失重要文件或泄露机密信息，导致雇主遭受损失，进而影响实习工资。",0.9),
        Pair("打工无故缺勤或迟到早退，根据规定被扣除相应的工钱。",0.15),
        Pair("发传单没有完成分配的任务量，雇主以此为由扣减报酬。",0.15),
        Pair("作为快递员丢件或者送件延误，造成客户投诉，公司扣除工资。",0.35),
        Pair("在酒店当保洁员未打扫干净房间，遭到客人投诉，酒店经理决定扣除一部分薪资作为惩罚。",0.20),
        Pair("在健身房做兼职教练，因为学员满意度低而被减少工资。",0.26),
        Pair("商场售货员盘点时发现货物数量不符，认为是你的失职，于是从你的工资中扣除差额。",0.85),
        Pair("作为网站管理员发布有误的信息，导致网站信誉受损，被扣减工资。",0.60),
        Pair("书店打工整理图书时不小心撕毁了封面，书店老板从中扣除了一部分工资来弥补损失。",0.15),
        Pair("宠物店员工照看动物不当，导致宠物受伤，被扣除部分薪水以支付医疗费用。",0.21),
        Pair("作为图书馆助理在归还书籍时放错了位置，需要花费额外时间寻找，图书馆主管决定扣除部分小时工资。",0.15),
        Pair("兼职厨师烹饪的食物不符合卫生标准，被卫生检查部门罚款，老板因此扣你工资",0.75),
        Pair("文案撰写人提交的文章内容存在抄袭行为，被雇主发现后扣除全部或部分稿酬。",0.85),
        Pair("社交媒体营销实习生,发布的帖子违反了平台规定，导致账号被封禁，企业主因此扣除了你的工资。",0.86),
        Pair("活动策划兼职者未能按时组织好活动，导致参与者不满，主办方从其工资中扣除一定比例。",0.5),
        Pair("在超市做收银员时，因操作失误导致商品价格计算错误，超市主管从中扣除一部分工资以弥补差额。",0.3),
    )
    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        val msg = e.message
        if (msg.size != 2) return false
        val text = e.message[1].content
        return if(regex.matches(text)){
            true
        }else{
            regex2.matches(text)
        }
    }

    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val g = e.group
        val msg = e.message[1].content
        val isRandom = regex.matches(msg)
        val sb = StringBuilder()
        if (!isRandom){
            sb.append("随机")
        }
        val hour = if (isRandom) {
            msg.split(" ")[1].toInt()
        }else{
            (1..24).random()
        }
        val user = dbh.getUser(e.sender.id, e.group.id, e.sender.nick)
        dbh.userGoWorking(user,hour)
        val workInfo = dbh.userWork(user.qq)!!
        val userNew = dbh.getUser(user.qq,user.firstRegisterGroup,user.nick)
        val salary = (hour*(0.8*hour + 10.0)).roundToInt()
        sb.append("打工${hour}小时，报酬${salary}\n")
        sb.append("打工累计时间${workInfo.timer}小时，累计收益${workInfo.moneyCounter}积分\n")
        sb.append("接下来${hour}小时内将不再响应您的指令，现有积分${userNew.money}")
        g.sendMessage(sb.toString())
        if (hour>=9 && (1..99).random()>30){
            val m = mistake.random()
            val pay = (salary*m.second*10 + (-9..9).random()).roundToLong()
            g.sendMessage(m.first+"\n扣除积分${pay}")
            dbh.addMoney(user,-pay)
        }
    }

}
object CmdCardSearch:Command{
    private val API:String = "https://ygocdb.com/api/v0/?search="
    private val PIC_URL = "https://cdn.233.momobako.com/ygopro/pics/"
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

object CmdMaxim:Command{
    override val name: String = "一言"
    override val description: String = "获取随机每日一言"

    override fun acceptable(e: MessageEvent): Boolean {
        if (e !is GroupMessageEvent) return false
        if (e.message.size != 2) return false
        if (e.message[1] !is PlainText) return false
        if ((e.message[1] as PlainText).content != name) return false
//        if (e.group.id != 190772405L) return false
        return true
    }

    override suspend fun action(e: MessageEvent) {
        e as GroupMessageEvent
        val sb = StringBuilder()
        val a = getMaximDirect()
        if (a == null){
            MyPluginMain.logger.error("一言：获取失败")
            return
        }
        sb.append(a.trimMargin().replace("<br>",""))
        e.group.sendMessage(sb.toString())
    }
    private suspend fun getMaximDirect():String?{
        val api01 = "http://api.yujn.cn/api/sjyy.php"
        val api02 = "http://api.yujn.cn/api/sc1.php"
        val api03 = "http://api.yujn.cn/api/kfc.php"
        val api04 = "http://api.yujn.cn/api/yan.php"
        val al = arrayListOf(api01,api02,api03,api04)
        return try{
            val randomIndex = (1..al.size).random()
            MyPluginMain.logger.info("api${randomIndex}:${al[randomIndex-1]}")
            val maximUrl = al[randomIndex-1]
            val cnt = withContext(Dispatchers.IO) {
                (URL(maximUrl).openConnection() as HttpURLConnection).also {
                    it.connect()
                }
            }
            if (cnt.responseCode != 200) {
                println("连接失败")
                throw Exception()
            }
            val string = withContext(Dispatchers.IO) { cnt.inputStream.bufferedReader().readText() }
            string
        }catch (e:Exception){
            null
        }
    }

    private suspend fun getMaxim():MaximVer10?{
        try{
            val maximUrl = "https://api.yujn.cn/api/yiyan.php"
            val cnt = withContext(Dispatchers.IO) {
                (URL(maximUrl).openConnection() as HttpURLConnection).also {
                    it.connect()
                }
            }
            if (cnt.responseCode != 200) {
                println("连接失败")
                return null
            }
            val string = withContext(Dispatchers.IO) { cnt.inputStream.bufferedReader().readText() }
            return Gson().fromJson<MaximVer10>(string, MaximVer10::class.java)
        }catch (e:Exception){
            return null
        }
    }

}



