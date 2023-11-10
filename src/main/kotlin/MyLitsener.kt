package org.hezistudio

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMemberEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import org.hezistudio.storage.DatabaseHelper
import java.nio.file.Path

object MyListener:ListenerHost{
    val configPath: Path = MyPluginMain.configFolderPath
    val dataPath: Path = MyPluginMain.dataFolderPath
    val logger: MiraiLogger = MyPluginMain.logger

    @EventHandler
    suspend fun test(e:GroupMessageEvent){
        if (e.message.content=="测试"){
            DatabaseHelper.registerUser(e.sender.id,e.group.id,e.sender.nick)
            val r = DatabaseHelper.findUser(e.sender.id)
            if (r!=null){
                e.group.sendMessage("${r.nick}:${r.id} in ${r.firstRegisterGroup}")
            }else{
                e.group.sendMessage("something wrong...")
            }

        }
    }



}