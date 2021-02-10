package me.arasple.mc.trmenu.modules.function.item.impl

import me.arasple.mc.trmenu.modules.function.item.base.MatchItemIdentifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author Arasple
 * @date 2020/3/13 22:06
 */
class MatchItemName : MatchItemIdentifier("(display)?(-)?name(s)?") {

    override fun match(player: Player, itemStack: ItemStack): Boolean = itemStack.itemMeta.let {
        if (it != null && it.hasDisplayName()) {
            val displayName = it.displayName
            return displayName.equals(getContent(player), true)
        }
        return false
    }

    override fun apply(player: Player, itemStack: ItemStack): ItemStack {
        val itemMeta = itemStack.itemMeta ?: return itemStack
        itemMeta.setDisplayName(getContent(player))
        itemStack.itemMeta = itemMeta
        return itemStack
    }

}