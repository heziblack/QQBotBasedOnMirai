package org.hezistudio

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.utils.MiraiLogger
import org.hezistudio.storage.cmdDeal
import org.hezistudio.storage.groupList
import org.hezistudio.storage.syncCmdList
import org.hezistudio.storage.userWorkStatueCheck

object MyListener:ListenerHost{
    private val logger: MiraiLogger = MyPluginMain.logger
    @EventHandler
    suspend fun groupMessageHandler(e:GroupMessageEvent){
        if (!whitelistCheck(e.group.id)) return
        if (!userWorkStatueCheck(e.sender.id)) return
        val cmdResult = cmdDeal(e)
        when (cmdResult){
            false->{
                e.group.sendMessage("指令出错，请联系管理员")
            }
            null->{return}
            true->{return}
        }
    }
    @EventHandler
    suspend fun syncMessage(e:GroupMessageSyncEvent){
        try{
            for (cmd in syncCmdList) {
                if (cmd.acceptable(e)) {
                    logger.info("执行${cmd.name}指令")
                    cmd.action(e)
                    logger.info("执行完毕")
                    return
                }
            }
        }catch (exc:Exception){
            logger.info("执行出错")
            logger.error(exc)
            e.group.sendMessage("坏、坏掉了")
        }
    }
    private fun whitelistCheck(gn:Long):Boolean{
        return gn in groupList.groupList
    }



}