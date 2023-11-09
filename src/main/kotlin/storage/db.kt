package org.hezistudio.storage

import org.hezistudio.storage.Users.default
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.IntIdTable

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

