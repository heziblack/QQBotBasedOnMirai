package org.hezistudio.game.farm.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

/**农场表结构*/
object Farms:LongIdTable(){
    val qq: Column<Long> = long("qq")
    val exp: Column<Long> = long("exp")
}
