package me.arasple.mc.trmenu.modules.function.script.expression

import io.izzel.taboolib.util.Strings

/**
 * @author Arasple
 * @date 2020/3/1 22:45
 */
enum class Expression(val regex: Regex, var replace: String, var splitBy: String, var translateScript: Boolean) {

    STRING_EQUALS("(equal(s)?|is)", "\"{0}\" == \"{1}\""),

    STRING_EQUALS_IGNORE_CASE("(equal(s)?|is)IgnoreCase(s)?", "utils.equalsIgnoreCase(\"{0}\", \"{1}\")"),

    IS_NUMBER("isNum(ber)?", "utils.isNumber(\"{0}\")"),

    IS_OPERATOR("isOp(erator)?(s)?", "player.isOp()"),

    IS_SNEAKING("isSneak(ing)?", "player.isSneaking()"),

    IS_PLAYER_OPERATOR("isPlayerOp(erator)?(s)?", "utils.isPlayerOperator(\"{0}\")"),

    IS_PLAYER_ONLINE("isOnline", "utils.isPlayerOnline(\"{0}\")"),

    IS_PLAYER_WHITELISTED("isWhitelisted", "utils.isPlayerWhitelisted(\"{0}\")", "~"),

    HAS_PERMISSION("ha(s|ve)(-)?(Perm(ission)?(s)?)", "player.hasPermission(\"{0}\")", "~"),

    HAS_LEVEL("ha(s|ve)(-)?(Level|Lv)(s)?", "player.getLevel() >= {0}"),

    HAS_MONEY("ha(s|ve)(-)?(Money|Eco|Coin)(s)?", "utils.hasMoney(player, {0})", "~"),

    HAS_POINTS("ha(s|ve)(-)?(Point)(s)?", "utils.hasPoints(player, {0})"),

    HAS_ITEM("ha(s|ve)(-)?Item(Stack)?(s)?", "utils.hasItem(player, \"{0}\")", "~", false),

    HAS_META("ha(s|ve)(-)?Meta(data)?(s)?", "utils.hasMeta(player, \"{0}\")", "~"),

    CRONUS_CONDITION("(cq|cronus)Condition", "utils.evalCronusCondition(player, {0})");

    constructor(name: String, replace: String) : this(name, replace, ".")

    constructor(name: String, replace: String, splitBy: String) : this(("(?i)$name.").toRegex(), replace, splitBy, true)

    constructor(name: String, replace: String, splitBy: String, translateScript: Boolean) : this(("(?i)$name.").toRegex(), replace, splitBy, translateScript)

    fun parse(string: String): String = Strings.replaceWithOrder(replace, *getArguments(string))

    private fun getArguments(string: String): Array<String> = string.split(splitBy).toTypedArray()

}