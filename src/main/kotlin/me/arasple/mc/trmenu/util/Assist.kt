package me.arasple.mc.trmenu.util

import io.izzel.taboolib.internal.apache.lang3.math.NumberUtils
import io.izzel.taboolib.kotlin.Randoms
import io.izzel.taboolib.module.db.local.LocalPlayer
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import me.arasple.mc.trmenu.api.Extends.getArguments
import me.arasple.mc.trmenu.api.Extends.getMeta
import me.arasple.mc.trmenu.api.action.Actions
import me.arasple.mc.trmenu.modules.function.WebData
import me.arasple.mc.trmenu.modules.function.hook.HookInstance
import me.arasple.mc.trmenu.modules.function.hook.impl.HookLuckPerms
import me.arasple.mc.trmenu.modules.function.item.ItemIdentifierHandler
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory


/**
 * @author Arasple
 * @date 2020/7/21 20:57
 */
class Assist {

    companion object {

        val INSTANCE = Assist()

    }

    /**
     * Actions Execute & Control
     */

    fun runAction(player: Player, vararg actions: String) {
        actions.filter { it.isNotBlank() }.forEach { Actions.runCachedAction(player, it) }
    }

    /**
     * Bukkit
     */
    fun isPlayerOperator(player: String): Boolean {
        return getPlayer(player).let { return@let it != null && it.isOp }
    }

    fun isPlayerOnline(player: String): Boolean {
        return getPlayer(player).let { return@let it != null && it.isOnline }
    }

    fun isPlayerWhitelisted(player: String): Boolean {
        return getOfflinePlayer(player)?.isWhitelisted ?: Bukkit.getWhitelistedPlayers()
            .any { it.name.equals(player, true) }
    }

    fun addWhitelist(player: String): Boolean {
        return getOfflinePlayer(player)?.let {
            Bukkit.getWhitelistedPlayers().add(it)
            true
        } ?: false
    }

    fun removeWhitelist(player: String): Boolean {
        return Bukkit.getWhitelistedPlayers().removeIf { it.name.equals(player, true) }
    }

    fun getPlayer(player: String): Player? {
        return Bukkit.getPlayerExact(player)
    }

    fun getOfflinePlayer(player: String): OfflinePlayer? {
        return Bukkit.getOfflinePlayers().firstOrNull { it.name.equals(player, true) }
    }

    fun getOnlinePlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().sortedBy { it.name }
    }

    fun getRandomPlayer(): Player? {
        return Bukkit.getOnlinePlayers().randomOrNull()
    }

    fun getPlayerInventory(player: String): PlayerInventory? {
        return getPlayer(player)?.inventory
    }

    fun getArmorContents(player: String): Array<ItemStack>? {
        return getPlayerInventory(player)?.armorContents
    }

    fun getItemInHand(player: String) = getItemInHand(player, false)

    fun getItemInHand(player: String, offhand: Boolean): ItemStack? {
        return getPlayerInventory(player)?.let {
            if (offhand) it.itemInOffHand
            else it.itemInMainHand
        }
    }

    /**
     * BungeeCord
     */

    fun connect(player: Player, server: String) {
        Bungees.connect(player, server)
    }

    fun sendBungeeData(player: Player, vararg data: String) {
        Bungees.sendBungeeData(player, *data)
    }

    /**
     * Internal - TrMenu
     */

    fun getPlayerArgs(player: String): Array<String> {
        return getPlayer(player)?.getArguments() ?: arrayOf()
    }

    fun query(url: String): WebData {
        return WebData.query(url)
    }

    /**
     * Internal - TabooLib Utils
     */

    fun getItemBuildr(): ItemBuilder {
        return ItemBuilder(Material.STONE)
    }

    fun getTellraw(): TellrawJson {
        return TellrawJson.create()
    }

    fun getItemName(itemStack: ItemStack): String {
        return Items.getName(itemStack)
    }

    fun hasItem(player: String, identify: String): Boolean {
        return getPlayer(player)?.let { ItemIdentifierHandler.read(identify).hasItem(it) } ?: false
    }

    fun hasItem(player: Player, identify: String): Boolean {
        return ItemIdentifierHandler.read(identify).hasItem(player)
    }

    fun listData(player: Player): MutableList<String>? {
        return LocalPlayer.get(player).getConfigurationSection("TrMenu.Data")?.getKeys(true)?.sorted()?.toMutableList()
    }

    fun setData(player: Player, node: String, value: Any?) {
        LocalPlayer.get(player).set("TrMenu.Data.$node", value)
    }

    fun hasData(player: Player, path: String): Boolean {
        return getData(player, path) != null
    }

    fun getData(player: Player, path: String): Any? {
        return LocalPlayer.get(player).get("TrMenu.Data.$path")
    }

    fun getDataSection(player: Player): ConfigurationSection? {
        return LocalPlayer.get(player).let {
            if (!it.isSet("TrMenu.Data")) it.createSection("TrMenu.Data")
            else it.getConfigurationSection("TrMenu.Data")
        }
    }

    fun hasMeta(player: Player, id: String): Boolean {
        return player.getMeta("{meta:$id}") != null
    }

    /**
     * Hook - PlaceholderAPI
     */

    fun parsePlaceholders(player: OfflinePlayer, string: String): String {
        return PlaceholderAPI.setPlaceholders(player, string)
    }

    fun parseBracketPlaceholders(player: OfflinePlayer, string: String): String {
        return PlaceholderAPI.setBracketPlaceholders(player, string)
    }

    /**
     * Hook - Economy
     */

    fun hasMoney(player: Player, money: String): Boolean {
        return hasMoney(player, NumberUtils.toDouble(money, 0.0))
    }

    fun hasMoney(player: Player, money: Double): Boolean {
        return HookInstance.get(player) >= money
    }

    fun hasPoints(player: Player, points: String): Boolean {
        return hasPoints(player, NumberUtils.toInt(points, 0))
    }

    fun hasPoints(player: Player, points: Int): Boolean {
        return HookInstance.getPlayerPoints().hasPoints(player, points)
    }

    fun evalCronusCondition(player: String, condition: String): Boolean {
        return getPlayer(player)?.let { return@let evalCronusCondition(it, condition) } ?: false
    }

    fun evalCronusCondition(player: Player, condition: String): Boolean {
        return HookInstance.getCronus().parseCondition(condition).check(player)
    }

    fun getPlayerHead(name: String): ItemStack {
        return Skulls.getPlayerHead(name)
    }

    fun getLuckPerms(): HookLuckPerms {
        return HookInstance.get("LuckPerms") as HookLuckPerms
    }


    /**
     * Numbers
     */
    fun chance(number: String): Boolean {
        return Randoms.random(NumberUtils.toDouble(number, 0.0))
    }

    fun randomInteger(low: Int, high: Int): Int {
        return IntRange(low, high).random()
    }

    fun randomDouble(low: Double, high: Double): Double {
        return Randoms.random(low, high)
    }

    fun isNumber(number: String): Boolean {
        return NumberUtils.isParsable(number)
    }

    fun isInt(number: String): Boolean {
        return try {
            number.toInt()
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun toInt(number: String): Int {
        return NumberUtils.toInt(number, -1)
    }

    fun toDouble(number: String): Double {
        return NumberUtils.toDouble(number, -1.0)
    }

    fun isWithin(input: String, low: String, high: String): Boolean {
        return IntRange(low.toInt(), high.toInt()).contains(input.toInt())
    }

    fun isSmaller(input1: String, input2: String): Boolean {
        return NumberUtils.toDouble(input1) < NumberUtils.toDouble(input2)
    }

    fun isSmallerOrEqual(input1: String, input2: String): Boolean {
        return NumberUtils.toDouble(input1) <= NumberUtils.toDouble(input2)
    }

    fun isGreater(input1: String?, input2: String?): Boolean {
        return NumberUtils.toDouble(input1) > NumberUtils.toDouble(input2)
    }

    fun isGreaterOrEqual(input1: String?, input2: String?): Boolean {
        return NumberUtils.toDouble(input1) >= NumberUtils.toDouble(input2)
    }

    /**
     * Miscellaneous
     */

    fun emptyString(length: Int): String {
        return buildString {
            for (i in 0..length) append(" ")
        }
    }

    fun equalsIgnoreCase(sample: String, temp: String): Boolean {
        return sample.equals(temp, true)
    }

    fun sort(list: List<Any>): List<Any> {
        return list.sortedBy { it.toString() }
    }

}