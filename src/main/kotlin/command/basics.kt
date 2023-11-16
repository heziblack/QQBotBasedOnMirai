package org.hezistudio.command

import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.event.events.MessageEvent

interface Command{
    val name:String
    val description:String
    //    val acceptTypes:ArrayList<JavaType>
    fun acceptable(e: MessageEvent):Boolean
    suspend fun action(e: MessageEvent)
}

interface SyncCommand{
    val name:String
    val description:String
    //    val acceptTypes:ArrayList<JavaType>
    fun acceptable(e: GroupMessageSyncEvent):Boolean
    suspend fun action(e: GroupMessageSyncEvent)
}