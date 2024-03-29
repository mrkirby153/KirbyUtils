package me.mrkirby153.kcutils.structure

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import me.mrkirby153.kcutils.Chat
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.Locale

@CommandAlias("structure")
class CommandStructure : BaseCommand() {

    private val placedStructures = mutableMapOf<Int, Structure>()

    var currentId = 1


    @Subcommand("create")
    fun create(
        sender: CommandSender, origin: Location, pt1: Location, pt2: Location,
        outFile: String, @Default("") ignoredMaterials: String
    ) {
        val ignored = mutableListOf<Material>()

        ignoredMaterials.split(",").forEach {
            try {
                ignored.add(Material.valueOf(it.uppercase(Locale.getDefault())))
            } catch (e: IllegalArgumentException) {
                // Ignore
            }
        }
        Structure.makeStructure(origin, pt1, pt2, ignored.toTypedArray()).save(outFile)
        sender.sendMessage(
            Chat.message("Structure", "Structure created successfully!")
        )
    }

    @Subcommand("place")
    fun place(sender: CommandSender, origin: Location, file: String) {
        val id = currentId++
        placedStructures[id] = Structure(
            YamlConfiguration.loadConfiguration(File(file))
        ).apply {
            placeAll(origin)
        }
        sender.sendMessage(
            Chat.message(
                "Structure",
                "Structure {id} placed at {x} {y} {z}",
                "x" to origin.blockX,
                "y" to origin.blockY,
                "z" to origin.blockZ, "id" to id
            )
        )
    }

    @Subcommand("remove")
    fun remove(sender: CommandSender, id: Int) {
        if (placedStructures.containsKey(id)) {
            placedStructures.remove(id)?.restore()
            sender.sendMessage(
                Chat.message("Structure", "Removed structure {id}", "id" to id)
            )
        } else {
            sender.sendMessage(Chat.error("That structure isn't placed!"))
        }
    }

}