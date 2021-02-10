package me.arasple.mc.trmenu.modules.command

import io.izzel.taboolib.module.command.base.*
import me.arasple.mc.trmenu.modules.command.sub.*


/**
 * @author Arasple
 * @date 2020/5/30 14:11
 */
@BaseCommand(name = "trmenu", aliases = ["tmenu", "menu"], permission = "trmenu.access")
class CommandHandler : BaseMainCommand() {

    @SubCommand(permission = "trmenu.command.reload", description = "Reload menus")
    val reload: BaseSubCommand = CommandReload()

    @SubCommand(permission = "trmenu.command.open", description = "Open a menu for player")
    val open: BaseSubCommand = CommandOpen()

    @SubCommand(permission = "trmenu.command.list", description = "List loaded menus")
    val list: BaseSubCommand = CommandListMenu()

    @SubCommand(permission = "trmenu.command.action", description = "Run actions for test")
    val action: BaseSubCommand = CommandAction()

    @SubCommand(permission = "trmenu.command.item", description = "Manipulate items")
    val item: BaseSubCommand = CommandItem()

    @SubCommand(permission = "trmenu.command.itemRepository", description = "Store and access itemStacks")
    val itemRepo: BaseSubCommand = CommandItemRepository()

    @SubCommand(permission = "trmenu.command.template", description = "Create menus quickly", type = CommandType.PLAYER)
    val template: BaseSubCommand = CommandTemplate()

    @SubCommand(permission = "trmenu.command.sounds", description = "Preview sounds", type = CommandType.PLAYER)
    var sounds: BaseSubCommand = CommandSoundsPreview()

    @SubCommand(permission = "trmenu.command.migrate", description = "Migrate menus for other plugins")
    val migrate: BaseSubCommand = CommandMigrate()

    @SubCommand(permission = "trmenu.command.mirror", description = "View performance monitoring", type = CommandType.PLAYER)
    val mirror: BaseSubCommand = CommandMirror()

    @SubCommand(permission = "trmenu.command.dump", description = "Upload environmental information for bug report")
    val dump: BaseSubCommand = CommandDump()

    @SubCommand(permission = "trmenu.command.debug", description = "Enable debug mode for player or print debug info to console")
    val debug: BaseSubCommand = CommandDebug()

    @SubCommand(permission = "trmenu.command.download", description = "Download and save file from URL")
    val download: BaseSubCommand = CommandDownload()

}