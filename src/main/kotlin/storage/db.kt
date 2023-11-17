package org.hezistudio.storage

//import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
//import java.time.LocalDate
import java.util.Date

object Users: IntIdTable(){
    val qq:Column<Long> = long("qq")
    val nick:Column<String> = varchar("nick",32)
    val firstRegisterGroup:Column<Long> = long("first_register_group")
    val money:Column<Long> = long("money").default(0L)
}

class User(id:EntityID<Int>):IntEntity(id){
    companion object:IntEntityClass<User>(Users)
    var qq by Users.qq
    var nick by Users.nick
    var firstRegisterGroup by Users.firstRegisterGroup
    var money by Users.money
}

object SignIns:LongIdTable(){
    val qq:Column<Long> = long("qq")
    val lastDate:Column<LocalDate> = date("last_sign_in")
    val consecutiveDays:Column<Int> = integer("consecutive_days")
}

class UserSignIn(id: EntityID<Long>):LongEntity(id){
    companion object:LongEntityClass<UserSignIn>(SignIns)
    var qq by SignIns.qq
    var lastDate by SignIns.lastDate
    var consecutiveDays by SignIns.consecutiveDays
}

object UserWorks:LongIdTable(){
    val qq:Column<Long> = long("qq")
    val workStamp:Column<LocalDateTime> = datetime("work_Stamp")
    val moneyCounter:Column<Long> = long("money_counter")
    val timer:Column<Long> = long("timer")
}

class Work(id: EntityID<Long>):LongEntity(id){
    companion object:LongEntityClass<Work>(UserWorks)
    var qq by UserWorks.qq
    var workStamp by UserWorks.workStamp
    var moneyCounter by UserWorks.moneyCounter
    var timer by UserWorks.timer
}

