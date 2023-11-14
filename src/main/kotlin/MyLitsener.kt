package org.hezistudio

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import org.hezistudio.storage.DatabaseHelper
import org.hezistudio.storage.cmdDeal
import org.hezistudio.storage.groupWhitelist
import java.nio.file.Path

object MyListener:ListenerHost{
    val configPath: Path = MyPluginMain.configFolderPath
    val dataPath: Path = MyPluginMain.dataFolderPath
    val logger: MiraiLogger = MyPluginMain.logger

    @EventHandler
    suspend fun test(e:GroupMessageEvent){
        if (!whitelistCheck(e.group.id)) return
        testMod(e)
        val cmdResult = cmdDeal(e)
        when (cmdResult){
            false->{
                e.group.sendMessage("指令出错，请联系管理员")
            }
            null->{return}
            true->{return}
        }
    }

    private suspend fun testMod(e: GroupMessageEvent){
        if (e.message.content=="测试"){
            DatabaseHelper.registerUser(e.sender.id,e.group.id,e.sender.nick)
            val r = DatabaseHelper.findUser(e.sender.id)
            if (r!=null){
                e.group.sendMessage("${r.nick}:${r.qq} in ${r.firstRegisterGroup}")
            }else{
                e.group.sendMessage("something wrong...")
            }
        }
    }

    private fun whitelistCheck(gn:Long):Boolean{
        return gn in groupWhitelist
    }

}