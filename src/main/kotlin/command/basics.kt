package org.hezistudio.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

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