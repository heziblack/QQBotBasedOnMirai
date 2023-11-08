package org.hezistudio

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMemberEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content

object MyListener:ListenerHost{
    @EventHandler
    suspend fun test(e:GroupMessageEvent){
        if (e.message.content=="签到"){
            e.group.sendMessage("签个屁")
        }
    }
}