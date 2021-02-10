package me.arasple.mc.trmenu.api.action.impl.item

import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.util.item.Items
import me.arasple.mc.trmenu.api.action.base.Action
import me.arasple.mc.trmenu.modules.data.Metas
import me.arasple.mc.trmenu.modules.function.item.ItemIdentifierHandler
import me.arasple.mc.trmenu.util.Utils
import org.bukkit.entity.Player

/**
 * @author Arasple
 * @date 2020/3/30 19:50
 */
class ActionGiveItem : Action("(give|add)(-)?item(s)?") {

    override fun onExecute(player: Player) {
        val item = getContent(player)
        if (Utils.isJson(item)) {
            CronusUtils.addItem(player, Items.fromJson(item))
        } else {
            ItemIdentifierHandler.read(item).identifiers.forEach {
                it.buildItem(player)?.let { item ->
                    CronusUtils.addItem(player, item)
                }
            }
        }
        Metas.updateInventoryContents(player, true)
    }

}