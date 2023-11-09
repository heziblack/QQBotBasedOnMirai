package org.hezistudio

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info

object MyPluginMain : KotlinPlugin(
        JvmPluginDescription(
                id = "org.hezistudio.groupmanager",
                name = "hezi群管插件",
                version = "0.1.0",
        ) {
            author("Hezi")
        }
) {


    override fun onEnable() {
        MyListener.initFunc(dataFolderPath,configFolderPath,logger)
        logger.info("初始化监听成功")
        globalEventChannel().registerListenerHost(MyListener)
        logger.info("监听注册成功")

        logger.info("插件加载完成")
    }
}