package org.hezistudio.game.farm.plants

/**植物成长阶段节点*/
abstract class GrowingTreeNode(
    /**阶段名*/
    val name:String,
) {
    val children:ArrayList<GrowingTreeNode> = arrayListOf()
    /**给定一些条件参数，使植物进入下一生长阶段,若已经是最终成长阶段
     * 则返回空值*/
    abstract fun getNext(): GrowingTreeNode?
}