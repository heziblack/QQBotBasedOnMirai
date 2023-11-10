package org.hezistudio.storage

import kotlinx.datetime.LocalDate
import org.hezistudio.storage.Users.default
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
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
    val lastDate:Column<LocalDate> = date("date")
    val consecutiveDays:Column<Int> = integer("consecutive_days")
}

class UserSignIn(id: EntityID<Long>):LongEntity(id){
    companion object:LongEntityClass<UserSignIn>(SignIns)
    var qq by SignIns.qq
    var localDate by SignIns.lastDate
    var consecutiveDays by SignIns.consecutiveDays
}

