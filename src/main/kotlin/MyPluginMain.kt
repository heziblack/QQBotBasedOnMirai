package org.hezistudio

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import org.hezistudio.storage.DatabaseHelper

object MyPluginMain : KotlinPlugin(
        JvmPluginDescription(
                id = "org.hezistudio.groupManager",
                name = "Hezi群管",
                version = "0.1.1",
        ) {
            author("Hezi")
        }
) {
    override fun onEnable() {
//        MyListener.initMe(configFolderPath, dataFolderPath, logger)
        globalEventChannel().registerListenerHost(MyListener)
        DatabaseHelper
    }
}