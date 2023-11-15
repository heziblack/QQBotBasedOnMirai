package org.hezistudio.storage

class SetuBean(
    val error:String?,
    val data:Array<PicData>
){
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