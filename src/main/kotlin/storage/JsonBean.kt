package org.hezistudio.storage

class SetuBean(
    val error:String?,
    val data:Array<PicData>
)
{
    inner class PicData(
        val pid:Long,
        val p:Int,
        val uid:Long,
        val title:String,
        val author:String,
        val r18:Boolean = false,
        val width:Int,
        val height:Int,
        val tags:Array<String>,
        val ext:String,
        val aiType:Int,
        val uploadDate:Long,
        val urls:Map<String,String>
    ){
        override fun toString(): String {
            val s =  StringBuilder("{\n")
                .append("""
                    "pid": ${pid},
                    "p": ${p},
                    "uid": ${uid},
                    "title": "${title}",
                    "author": "${author}",
                    "r18": ${r18},
                    "width": ${width},
                    "height": ${height},
                    "tags": [
                    """.trimIndent())
                .append("""
                """.trimIndent())
            for (t in tags){
                s.append("\"${t}\"")
                if (t != tags.last()){
                    s.append(",\n")
                }else{
                    s.append("\n],\n")
                }
            }
            s.append("""
                "ext": "${ext}",
                "aiType": $aiType,
                "uploadDate": $uploadDate,
            """.trimIndent())
            s.append("\"urls\":{")
            for (p in urls){
                s.append("""
                    "${p.key}":"${p.value}",
                """.trimIndent())
                s.append("\n")
            }
            s.append("}\n}")
            return s.toString()
        }
    }

    override fun toString(): String {
        val s = StringBuilder("{\n").append("""
            "error":"${error}",
            "data":[
        """.trimIndent())
        for (d in data){
            s.append(d)
            s.append(",\n")
        }
        s.append("]")
        return s.toString()
    }


}

class GroupWhitelist(
    val groups:ArrayList<GroupInfo>
)
{
    class GroupInfo(
        val group:Long,
        val level:Int,
        val main:Boolean = false,
        val test:Boolean = false
    )

    companion object{
        private val defGroups = arrayListOf(795327860L,116143851L,190772405L)
        fun create():GroupWhitelist{
            val info = arrayListOf<GroupInfo>()
            for (g in defGroups){
                info.add(GroupInfo(g,1))
            }
            return GroupWhitelist(info)
        }
    }

    val groupList:ArrayList<Long>
        get() {
            val l = arrayListOf<Long>()
            for (d in groups){
                l.add(d.group)
            }
            return l
        }

    fun add(id:Long,level:Int){
        groups.add(GroupInfo(id,level))
    }

    fun remove(id:Long){
        for(g in groups){
            if (g.group == id){
                groups.remove(g)
                break
            }
        }
    }

    fun copy(origin:GroupWhitelist){
        this.groups.clear()
        for (o in origin.groups){
            this.groups.add(o)
        }
    }

}