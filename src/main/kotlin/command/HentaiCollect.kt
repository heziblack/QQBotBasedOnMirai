package org.hezistudio.command

import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.message.data.content
import org.hezistudio.MyPluginMain
import java.io.File

object HentaiCollect:SyncCommand {
    override val name: String = "收集情况"
    override val description: String= "关于群友们使用色图功能的统计"

    override fun acceptable(e: GroupMessageSyncEvent): Boolean {
        return e.message.size == 2 && e.message[1].content == "收集情况"
    }

    override suspend fun action(e: GroupMessageSyncEvent) {
        val imgFolder = File(MyPluginMain.dataFolder,"pixivImages")
        if (!imgFolder.exists()) {
            e.group.sendMessage("文件夹不存在")
            return
        }else if (!imgFolder.isDirectory){
            e.group.sendMessage("不是文件夹")
            return
        }
        MyPluginMain.logger.info("图片路径:${imgFolder.absolutePath}   存在:${imgFolder.exists()}  文件夹:${imgFolder.isDirectory}  文件:${imgFolder.isFile}")
        val fl = imgFolder.list()
        val count = fl?.size
        e.group.sendMessage("共收集了${count}张图片")
    }
}