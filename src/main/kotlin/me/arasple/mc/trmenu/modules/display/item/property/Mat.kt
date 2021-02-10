package me.arasple.mc.trmenu.modules.display.item.property

import io.izzel.taboolib.Version
import io.izzel.taboolib.internal.apache.lang3.math.NumberUtils
import io.izzel.taboolib.util.Strings
import io.izzel.taboolib.util.item.ItemBuilder
import io.th0rgal.oraxen.items.OraxenItems
import me.arasple.mc.trmenu.modules.conf.property.Nodes
import me.arasple.mc.trmenu.modules.display.item.Item
import me.arasple.mc.trmenu.modules.function.ItemRepository
import me.arasple.mc.trmenu.modules.function.hook.HookInstance
import me.arasple.mc.trmenu.modules.function.script.Scripts
import me.arasple.mc.trmenu.util.Msger
import me.arasple.mc.trmenu.util.Skulls
import me.arasple.mc.trmenu.util.Utils
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import kotlin.math.min

/**
 * @author Arasple
 * @date 2020/7/27 16:59
 */
data class Mat(val raw: String, val value: String, val type: Pair<Nodes, String>, val dynamic: Boolean) {

    var staticItemBuilder: ItemBuilder? = null
    var staticItem: ItemStack? = null

    init {
        if (type.first == Nodes.MAT_TEXTURED_SKULL) {
            Skulls.getTextureSkull(type.second)
        }
    }

    fun createItem(player: Player): ItemStack {
        val value = if (dynamic) Msger.replace(player, value) else value
        val typeValue = if (dynamic) Msger.replace(player, type.second) else type.second

        return when (type.first) {
            Nodes.MAT_REPOSITORY -> ItemRepository.getItem(typeValue) ?: Item.EMPTY_ITEM
            Nodes.MAT_HEAD -> Skulls.getPlayerHead(typeValue)
            Nodes.MAT_TEXTURED_SKULL -> Skulls.getTextureSkull(typeValue)
            Nodes.MAT_HEAD_DATABASE -> HookInstance.getHeadDatabase().getHead(typeValue)
            Nodes.MAT_ORAXEN -> OraxenItems.getItemById(typeValue).build()
            Nodes.MAT_HEAD_SKINSRESTORER -> {
                val texture = HookInstance.getSkinsRestorer().getSkinTextureValue(typeValue)
                if (texture == null) Skulls.getPlayerHead(typeValue)
                else Skulls.getTextureSkull(texture)
            }
            Nodes.MAT_SCRIPT -> Scripts.script(player, type.second, true).asItemStack() ?: Item.EMPTY_ITEM
            Nodes.MAT_JSON -> Item.fromJson(typeValue)
            else -> {
                if (staticItem != null) {
                    return staticItem!!
                }
                val builder = if (dynamic) serializeItemBuilder(value) else staticItemBuilder.let {
                    if (it == null) staticItemBuilder = serializeItemBuilder(value)
                    return@let staticItemBuilder!!
                }
                when (type.first) {
                    Nodes.MAT_DATA_VALUE -> builder.damage(NumberUtils.toInt(typeValue, 0))
                    Nodes.MAT_DYE_LEATHER -> builder.color(serializeColor(typeValue))
                    else -> {
                        // TODO BANNER
                    }
                }
                val itemStack = builder.build()
                if (!dynamic) staticItem = itemStack
                if (type.first == Nodes.MAT_MODEL_DATA && Version.isAfter(Version.v1_14)) {
                    itemStack.itemMeta.let { meta ->
                        meta?.setCustomModelData(NumberUtils.toInt(typeValue, 0)).also { itemStack.itemMeta = meta }
                    }
                }
                return itemStack
            }
        }
    }

    override fun toString() = raw

    companion object {

        fun serializeColor(color: String): Color {
            val rgb = color.split(",").toTypedArray()
            if (rgb.size == 3) {
                val r = min(rgb[0].toIntOrNull() ?: 0, 255)
                val g = min(rgb[1].toIntOrNull() ?: 0, 255)
                val b = min(rgb[2].toIntOrNull() ?: 0, 255)
                return Color.fromRGB(r, g, b)
            }
            return Color.BLACK
        }

        fun serializeItemBuilder(value: String): ItemBuilder {
            val split = value.split(":")
            var builder = ItemBuilder(Material.BARRIER)
            val data = if (split.size >= 2) NumberUtils.toInt(split[1], 0) else 0
            try {
                if (NumberUtils.isCreatable(split[0])) {
                    val id = NumberUtils.toInt(split[0], -1)
                    if (id >= 0) builder.material(id)
                    if (data > 0) builder.damage(data)
                } else {
                    val name = split[0].replace("( )|-".toRegex(), "_").toUpperCase()
                    val materials = Item.fromMaterials(name)
                    if (materials != null) {
                        val pared = materials.parseItem()
                        builder = if (data > 0) ItemBuilder(pared).damage(data)
                        else ItemBuilder(pared)
                    } else builder.material(Material.valueOf(split[0]))
                }
            } catch (e: Throwable) {
                Msger.printErrors("MATERIAL", value)
            }
            return builder
        }

        fun createMat(item: ItemStack): String {
            val meta = item.itemMeta
            val material = toMaterialFormateed(item)
            // Skull & Head
            @Suppress("DEPRECATION")
            if (meta is SkullMeta) {
                if (HookInstance.getHeadDatabase().isHooked()) {
                    try {
                        val id = HookInstance.getHeadDatabase().getId(item)
                        if (!Strings.isBlank(id)) return "<hdb:$id>"
                    } catch (ignored: NullPointerException) { }
                }
                val owner = meta.owner
                return if (owner != null) "<head:${owner}>"
                else "<skull:${Skulls.getSkullTexture(item)}>"
            }
            // ModelData
            if (Version.isAfter(Version.v1_14) && meta != null && meta.hasCustomModelData()) {
                return "$material<model-data:${meta.customModelData}>"
            }
            // Dye Leather TODO
            if (meta is LeatherArmorMeta) {
                return "$material<dye:${meta.color}>"
            }
            // Banner TODO
            return material
        }

        fun createMat(raw: String): Mat {
            val result = Nodes.read(raw)
            val left = result.first
            val nodes = result.second.entries.firstOrNull()
            var type = nodes?.key
            var typeValue = nodes?.value
            if (Utils.isJson(left) && left.contains('"')) {
                type = Nodes.MAT_JSON
                typeValue = left
            }
            val types = if (type != null) Pair(type, typeValue ?: "") else Pair(Nodes.MAT_ORIGINAL, result.first)
            return Mat(raw, result.first, types, Msger.containsPlaceholders(raw) || type == Nodes.MAT_VARIABLE)
        }

        private fun toMaterialFormateed(item: ItemStack): String {
            return item.type.name.toLowerCase().replace("_", " ")
        }

    }

}