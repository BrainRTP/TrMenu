package me.arasple.mc.trmenu.modules.function.script

import io.izzel.taboolib.internal.apache.lang3.math.NumberUtils
import io.izzel.taboolib.util.Strings
import io.izzel.taboolib.util.lite.Sounds
import me.arasple.mc.trmenu.api.Extends.getMenuSession
import me.arasple.mc.trmenu.api.event.MenuCloseEvent
import me.arasple.mc.trmenu.modules.function.script.expression.Expressions
import me.arasple.mc.trmenu.util.Assist
import me.arasple.mc.trmenu.util.Msger
import me.arasple.mc.trmenu.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function
import java.util.regex.Pattern
import javax.script.*

/**
 * @author Arasple
 * @date 2020/3/24 10:50
 */
object Scripts {

    private val bindings = SimpleBindings()
    private val engine = ScriptEngineManager(null).getEngineByName("nashorn")
    private val compiledScripts = mutableMapOf<String, CompiledScript>()
    private val booleanMatch = "(?i)(yes|true)".toRegex()

    const val function = "rwp"
    val argumentPattern: Pattern = Pattern.compile("['\"]?([\$]?\\{(.*?)})['\"]?")
    val placeholderPattern: Pattern = Pattern.compile("['\"]?[$]?(%.*?%)['\"]?")
    val bracketPlaceholderPattern: Pattern = Pattern.compile("[{]([^{}]+)[}]")

    init {
        bindings["bukkitServer"] = Bukkit.getServer()
        bindings["TrUtils"] = Assist.INSTANCE
        bindings["utils"] = Assist.INSTANCE
    }

    fun expression(player: Player, expression: String): Result {
        val parsed = Expressions.parse(expression)
        return script(player, parsed.first, bindings, parsed.second, true)
    }

    fun script(player: Player, script: String, cache: Boolean): Result {
        return script(player, script, bindings, true, cache)
    }

    fun script(player: Player, script: String, translate: Boolean, cache: Boolean): Result {
        return script(player, script, bindings, translate, cache)
    }

    fun script(player: Player, script: String, bindings: SimpleBindings, translate: Boolean, cache: Boolean): Result {
        return script(player, script, bindings, false, translate, cache)
    }

    fun script(
        player: Player,
        script: String,
        bindings: SimpleBindings,
        silent: Boolean,
        translate: Boolean,
        cache: Boolean
    ): Result {
        return eval(player, script, compile(script, translate, cache), bindings, silent)
    }

    private fun compile(content: String, translate: Boolean, cache: Boolean): CompiledScript? {
        return try {
            if (cache) {
                compiledScripts.computeIfAbsent(content) {
                    val script = if (translate) translate(content) else content
                    Msger.debug("PRE-COMPILE-SCRIPT", content, script)
                    (engine as Compilable).compile(script)
                }
            } else (engine as Compilable).compile(content)
        } catch (e: Throwable) {
            null
        }
    }

    private fun eval(
        player: Player,
        rawScript: String,
        script: CompiledScript?,
        bindings: SimpleBindings,
        silent: Boolean
    ): Result = try {
        val content = SimpleScriptContext()
        content.setBindings(SimpleBindings(bindings).let {
            it["player"] = player
            return@let it
        }, ScriptContext.ENGINE_SCOPE)
        content.setAttribute(function, Function<String, String> {
            Msger.replace(player, it)
        }, ScriptContext.ENGINE_SCOPE)
        Result(script!!.eval(content)).let {
            return@let it
        }
    } catch (e: Throwable) {
        if (!silent) Msger.printErrors("SCRIPT", e, rawScript)
        Tasks.delay(10) { cancel(player) }
        Result()
    }

    private fun cancel(player: Player) {
        player.getMenuSession().menu?.let {
            it.close(player, 0, MenuCloseEvent.Reason.ERROR, true, silent = false)
            Sounds.BLOCK_ANVIL_BREAK.play(player)
        }
    }

    fun translate(string: String): String {
        val buffer = StringBuffer(string.length)
        val content = placeholderPattern.matcher(string).let {
            while (it.find()) {
                val find = it.group(1)
                val group = Strings.replaceWithOrder(find, *getArgs(find))
                it.appendReplacement(buffer, "$function('${escape(group)}')")
            }
            it.appendTail(buffer).toString()
        }
        val buffer2 = StringBuffer(content.length)
        return argumentPattern.matcher(content).let {
            while (it.find()) {
                val group = it.group(2)
                val find = it.group(1)
                val isFunction = find.startsWith("$")
                if (!isFunction && !group.startsWith("meta") && !group.startsWith("data") && !group.startsWith("input") && !NumberUtils.isParsable(
                        group
                    ) && group != "reason"
                ) {
                    continue
                }
                it.appendReplacement(buffer2, "$function('${escape(find)}')")
            }
            it.appendTail(buffer2)
        }.toString()
    }

    private fun getArgs(content: String): Array<String> {
        val replaces = mutableListOf<String>()
        val bracker = bracketPlaceholderPattern.matcher(content)
        var size = -1
        while (bracker.find()) size =
            size.coerceAtLeast(NumberUtils.toInt(bracker.group().removeSurrounding("{", "}"), -1))
        for (i in 0..size) replaces.add("%trmenu_args_$i%")
        return replaces.toTypedArray()
    }

    private fun escape(string: String): String = escapeMath(
        string.replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("(", "\\(")
            .replace(")", "\\)").replace("$", "\\$")
    )

    private fun escapeMath(string: String): String = string.replace("+", "\\+").replace("*", "\\*")

    class Result(private val result: Any?, private val throwable: Throwable?) {

        constructor(result: Any?) : this(result, null)
        constructor() : this(null, null)

        fun isSucceed() = throwable == null && result != null

        fun asBoolean() = if (result is Boolean) result else result.toString().matches(booleanMatch)

        fun asString() = result.toString()

        fun asItemStack() = if (result is ItemStack) result else null

        fun asCollection() = if (result is Collection<*>) result else null

    }

}