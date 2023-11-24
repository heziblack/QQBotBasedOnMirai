package org.hezistudio.game.monopoly

enum class MatchStatus(code:Int){
    Free(0),
    Readying(1),
    Playing(2),
}

enum class PlayerStatus(){
    InGame(),
    Quit(),
    Collapse(),
    Freezing(),
}