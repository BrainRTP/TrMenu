package me.arasple.mc.trmenu.modules.function.hook

import io.izzel.taboolib.internal.apache.lang3.math.NumberUtils
import io.izzel.taboolib.module.inject.THook
import me.arasple.mc.trmenu.TrMenu
import me.arasple.mc.trmenu.api.Extends.getArguments
import me.arasple.mc.trmenu.api.Extends.getMenuSession
import me.arasple.mc.trmenu.api.Extends.getMeta
import me.arasple.mc.trmenu.modules.display.Menu
import me.arasple.mc.trmenu.modules.function.script.Scripts
import me.arasple.mc.trmenu.util.Utils
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * @author Arasple
 * @date 2020/4/4 13:43
 */
object HookPlaceholderAPI {

    fun replace(player: OfflinePlayer, content: String): String = PlaceholderAPI.setPlaceholders(player, PlaceholderAPI.setBracketPlaceholders(player, content))

    fun replace(player: OfflinePlayer, content: List<String>): List<String> = PlaceholderAPI.setPlaceholders(player, PlaceholderAPI.setBracketPlaceholders(player, content))

    private fun processRequest(player: OfflinePlayer, content: String): String {
        if (player !is Player) return ""
        val params = content.split("_")

        return when (params[0].toLowerCase()) {
            "menus" -> Menu.getMenus().size.toString()
            "args" -> arguments(player, params)
            "meta" -> meta(player, params)
            "data" -> data(player, params)
            "menu" -> menu(player, params)
            "emptyslot" -> freeSlot(player, params)
            "js" -> if (params.size > 1) Scripts.script(player, params[1], true).asString() else ""
            else -> ""
        }
    }

    private fun data(player: Player, params: List<String>): String {
        val data = player.getMeta("{data:${params[1]}}").toString()
        if (data == "null" && params.size > 2) return params[2]
        return data
    }

    private fun meta(player: Player, params: List<String>): String {
        val meta = player.getMeta("{meta:${params[1]}}").toString()
        if (meta == "null" && params.size > 2) return params[2]
        return meta
    }

    private fun arguments(player: Player, params: List<String>): String {
        val arguments = player.getArguments()
        if (params.size > 1) {
            return buildString {
                Utils.asIntRange(params[1]).forEach { it ->
                    arguments.getOrNull(it)?.let {
                        append(it)
                        append(" ")
                    }
                }
            }.removeSuffix(" ")
        }
        return "null"
    }

    private fun menu(player: Player, params: List<String>): String {
        val session = player.getMenuSession()
        return if (!session.isNull()) {
            when (params[1]) {
                "page" -> session.page.toString()
                "pages" -> session.menu?.layout?.layouts?.size.toString()
                "next" -> (session.page + 1).toString()
                "title" -> session.menu!!.settings.title.currentTitle(player)
                else -> ""
            }
        } else ""
    }

    private fun freeSlot(player: Player, params: List<String>): String {
        val session = player.getMenuSession()
        val index = NumberUtils.toInt(params.getOrElse(1) { "0" }, 0)
        val range = Utils.asIntRange(params.getOrElse(2) { "0-90" })
        return (session.menu?.getEmptySlots(player, session.page)?.filter { range.contains(it) }?.get(index) ?: -1).toString()
    }


    @THook
    class Inject : PlaceholderExpansion() {

        override fun getIdentifier() = "trmenu"

        override fun getVersion() = TrMenu.plugin.description.version

        override fun getAuthor() = "Arasple"

        override fun persist() = true

        override fun onRequest(player: OfflinePlayer?, params: String) = player?.let { processRequest(it, params) }


    }

}