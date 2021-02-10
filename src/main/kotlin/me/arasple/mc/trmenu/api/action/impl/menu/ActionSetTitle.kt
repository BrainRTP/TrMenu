package me.arasple.mc.trmenu.api.action.impl.menu

import me.arasple.mc.trmenu.api.action.base.Action
import me.arasple.mc.trmenu.util.Tasks
import org.bukkit.entity.Player

/**
 * @author Arasple
 * @date 2020/3/8 21:53
 */
class ActionSetTitle : Action("set(-)?title") {

    override fun onExecute(player: Player) {
        Tasks.delay(3) {
            val session = getSession(player)
            if (!session.isNull()) {
                val menu = session.menu ?: return@delay
                val layout = session.layout ?: return@delay

                layout.displayInventory(player, getContent(player))
                menu.refreshIconItems(player, session)
            }
        }
    }

}