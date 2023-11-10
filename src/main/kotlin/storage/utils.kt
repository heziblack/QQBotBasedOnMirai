package org.hezistudio.storage

import org.hezistudio.MyPluginMain
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

object DatabaseHelper{
    private val dbFile: File = File(MyPluginMain.dataFolder,"database.db3")
    val db by lazy {
        Database.connect("jdbc:sqlite:${dbFile}")
    }
    init {
        testDeleteDBFile() // 重置数据库
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        createDB()
    }

    private fun createDB(){
        transaction(db) {
            SchemaUtils.create(Users)
        }
    }

    private fun testDeleteDBFile(){
        if (dbFile.exists()){
            dbFile.delete()
        }
    }

    fun registerUser(qq:Long,group:Long,nick:String):Boolean{
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

    fun findUser(qq:Long):User?{
        return transaction {
            User.find{ Users.qq eq qq }.firstOrNull()
        }
    }



}