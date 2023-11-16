package org.hezistudio.storage

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
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
            SchemaUtils.create(Users, SignIns)
        }
    }

    /**在测试阶段必须使用的方法*/
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

}

val cmdList:ArrayList<Command> = arrayListOf(
    CmdSignIn, CmdBackpack, CmdHentaiPic
)
val syncCmdList:ArrayList<SyncCommand> = arrayListOf(
    AwardByHost, Test
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
        pluginMe.logger.info("执行出错")
        return false
    }
    return null
}
/**功能白名单*/
val groupWhitelist:ArrayList<Long> = arrayListOf(795327860L,116143851L,190772405L)


