package org.hezistudio

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMemberEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import java.nio.file.Path

object MyListener:ListenerHost{
    var dataFolder: Path? = null
    var configFolder: Path? = null
    var logger:MiraiLogger? = null
    @EventHandler
    suspend fun test(e:GroupMessageEvent){
        if (e.message.content=="路径"){
            e.group.sendMessage(dataFolder.toString()+"\n"+ configFolder.toString())
        }
    }
    /**初始化操作, 保存插件的必要线索*/
    public fun initFunc(dp:Path,cp:Path,lgr:MiraiLogger){
        dataFolder = dp
        configFolder = cp
        logger = lgr
    }

}