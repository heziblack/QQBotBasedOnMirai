package org.hezistudio.game.farm.plants

import com.google.gson.GsonBuilder

class PlantModule(
    val plantName:String,
    val description:String,
    val seedName:String,
    val seedDescription:String?=null,
    val fruitName:String,
    val fruitDescription:String?=null,
    val nodes:ArrayList<GrowingNode>
)
{
    fun toJson():String{
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }
}

