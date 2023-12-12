package org.hezistudio.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDateTime
import net.mamoe.mirai.event.events.MessageEvent
import org.hezistudio.MyPluginMain
import org.hezistudio.command.*
import org.hezistudio.MyPluginMain as pluginMe
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


object DatabaseHelper{
    private val dbFile: File = File(pluginMe.dataFolder,"database.db3")
    private val db by lazy {
        Database.connect("jdbc:sqlite:${dbFile}")
    }
    init {
        testDeleteDBFile() // 备份数据库
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        createDB()
    }

    private fun createDB(){
        transaction(db) {
            SchemaUtils.create(Users, SignIns, UserWorks)
            SchemaUtils.createMissingTablesAndColumns(Users, SignIns, UserWorks)
        }
    }

    /**在测试阶段必须使用的方法, 每次启动插件时备份之前的数据库数据*/
    private fun testDeleteDBFile(){
        if (dbFile.exists() && dbFile.isFile){
            val ds = java.time.LocalDateTime.now()
            val dds = """${ds.year}-${ds.month.value}-${ds.dayOfMonth}-${ds.hour}-${ds.minute}-${ds.second}"""
            val newFile = File(pluginMe.dataFolder, "backup${dds}.db3")
            if(dbFile.copyTo(newFile,true)==newFile){
                MyPluginMain.logger.info("成功启动时备份数据库")
            }
//            if(!dbFile.renameTo(newFile)) MyPluginMain.logger.info("备份数据库文件失败")
//            if(!dbFile.createNewFile()) MyPluginMain.logger.info("创建新数据库文件失败")
        }
    }

    private fun registerUser(qq:Long,group:Long,nick:String):Boolean{
        return transaction(db) {
            if(User.find { Users.qq eq qq }.empty()) {
                User.new {
                    this.qq = qq
                    firstRegisterGroup = group
                    this.nick = nick
                }
            }else{
                null
            }
        } != null
    }

    private fun findUser(qq:Long):User?{
        return transaction {
            User.find{ Users.qq eq qq }.firstOrNull()
        }
    }

    fun getUser(qq: Long,group: Long,nick: String):User{
        val u = findUser(qq)
        return if (u!=null) u else{
            registerUser(qq, group, nick)
            findUser(qq)!!
        }
    }

    fun getUser(user: User):User{
        return findUser(user.qq)!!
    }

    fun addMoney(user: User, add:Long){
        transaction(db){ user.money += add }
    }

    val currentDateTime:LocalDateTime
        get() {
            val ldt = java.time.LocalDateTime.now()
            return LocalDateTime(
                ldt.year,
                ldt.month,
                ldt.dayOfMonth,
                ldt.hour,
                ldt.minute,
                ldt.second,
                ldt.nano
            )
        }

    fun getUserSignIn(user: User):UserSignIn?{
        return transaction(db) {
            UserSignIn.find { SignIns.qq eq user.qq }.firstOrNull()
        }
    }

    fun createUserSignIn(user:User):UserSignIn{
        return transaction(db) {
            UserSignIn.new {
                qq = user.qq
                lastDate = currentDateTime.date
                consecutiveDays = 1
            }
        }
    }

    fun updateUserSignIn(user: User):UserSignIn{
        return transaction(db) {
            val s = UserSignIn.find { SignIns.qq eq user.qq }.first()
            val cDate = currentDateTime.date
            if (s.lastDate.plus(1,DateTimeUnit.DAY)==cDate){
                s.lastDate = cDate
                s.consecutiveDays += 1
            }else if (s.lastDate != cDate){
                s.lastDate = cDate
            }
            s
        }
    }
    /**查找qq号的打工记录，没有则返回空*/
    fun userWork(id:Long):Work?{
        return transaction(db){ Work.find { UserWorks.qq eq id }.firstOrNull() }
    }

    fun createUserWork(user:User):Work{
        return transaction(db) {
            userWork(user.qq)?:Work.new {
                qq = user.qq
                workStamp = currentDateTime
            }
        }
    }

    fun hentaiCounter(user: User){
        transaction {
            user.hentai++
        }
    }

    fun userGoWorking(user: User,hour:Int){
        return transaction(db){
            val workInfo = userWork(user.qq)?:Work.new {
                qq = user.qq
                workStamp = currentDateTime
            }
            val tsJava = currentDateTime.toJavaLocalDateTime().plusHours(hour.toLong())
            LocalDateTime.parse(tsJava.toString())
            val salary = (hour*(0.8*hour + 10.0)).roundToInt()
            workInfo.workStamp = LocalDateTime.parse(tsJava.toString())
            workInfo.timer += hour
            workInfo.moneyCounter += salary
            user.money += salary
        }

    }

}

val cmdList:ArrayList<Command> = arrayListOf(
    CmdSignIn, CmdBackpack, CmdHentaiPic, CmdWorkForMoney
)
val syncCmdList:ArrayList<SyncCommand> = arrayListOf(
    AwardByHost, Test, AddOrRemoveService,HentaiCollect
)

suspend fun cmdDeal(e:MessageEvent):Boolean?{
    try{
        for (cmd in cmdList) {
            if (cmd.acceptable(e)) {
                pluginMe.logger.info("执行${cmd.name}指令")
                cmd.action(e)
                pluginMe.logger.info("执行完毕")
                return true
            }
        }
    }catch (exc:Exception){
        pluginMe.logger.error(exc)
        pluginMe.logger.error("执行出错")
        return false
    }
    return null
}
/**功能白名单*/
val groupList:GroupWhitelist = GroupWhitelist.create()
val groupListFile:File = File(MyPluginMain.configFolder,"whitelist.json")

fun userWorkStatueCheck(id: Long):Boolean{
    val uw = DatabaseHelper.userWork(id)
    return if (uw == null){
        true
    }else{
        DatabaseHelper.currentDateTime > uw.workStamp
    }
}
fun loadGroupWhitelist(){
    val gson = GsonBuilder().setPrettyPrinting().create()
    if (groupListFile.exists() && groupListFile.isFile){
        val fromFile = groupListFile.reader().use {
            gson.fromJson(it, GroupWhitelist::class.java)
        }
        groupList.copy(fromFile)
    }else{
        groupListFile.createNewFile()
        val t = GroupWhitelist.create()
        val s = gson.toJson(t)
        groupListFile.writeText(s)
    }
}
fun saveGroupWhitelist(){
    val gson = Gson()
    val s = gson.toJson(groupList)
    groupListFile.writeText(s)
}
/**从配置中读取的端口号，默认为0*/
var proxyPort:Int = 0

fun proxySetting():Int{
    val f = File(MyPluginMain.configFolder,"port.txt")
    return if (f.exists() && f.isFile){
        val a = f.readText()
        try{
            a.toInt().absoluteValue
        }catch (exc:Exception){
            0
        }
    }else if (!f.exists()){
        f.createNewFile()
        f.writeText("0")
        0
    }else{
        0
    }
}

