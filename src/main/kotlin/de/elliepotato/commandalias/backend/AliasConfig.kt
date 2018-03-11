package de.elliepotato.commandalias.backend

import com.google.common.collect.Maps
import de.elliepotato.commandalias.CommandAlias
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.scanner.ScannerException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level

/**
 * Created by Ellie on 23/07/2017 for CommandAlias.
 *
 *    Copyright 2017 Ellie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
class AliasConfig(val core: CommandAlias, dir: File) {

    private val file: File = File(dir, "config.yml")
    private var cfg: YamlConfiguration

    init {

        var first = false

        if (!file.exists()) {
            if (!file.createNewFile()) throw IOException("Failed to create config.yml!")
            first = true
        }

        cfg = YamlConfiguration.loadConfiguration(file)
        if (cfg.getConfigurationSection("commands") == null) first = true

        if (first) {
            cfg.set("prefix", "&7[&aCommandAlias&7] &c")
            cfg.set("noPermission", "{prefix}No permission!")
            cfg.set("commands.gamemode.aliases", Arrays.asList("gm", "gamemodepls"))
            cfg.set("commands.gamemode.enabled", true)
            cfg.set("commands.gamemode.permission", "")
            cfg.set("commands.list.aliases", Collections.singletonList("peepsonline"))
            cfg.set("commands.list.enabled", true)
            cfg.set("commands.-msg-wiki.aliases", Collections.singletonList("You can find our wiki at: www.wikipedia.com!"))
            cfg.set("commands.-msg-wiki.enabled", true)
            save(cfg)
        }

        /* Since 1.1 */
        if (cfg.get("prefix") == null) {
            cfg.set("prefix", "&7[&aCommandAlias&7] &c")
            save(cfg)
        }

        /* Since 1.2 */
        if (cfg.get("noPermission") == null) {
            cfg.set("noPermission", "{prefix}No permission!")
            save(cfg)
        }

    }

    /***
     *
     * Get a new instance of commands
     *
     * @return a list of commands fed from the configuration file
     * @throws IllegalStateException If a configuration value of a alias is null
     * @throws ScannerException By the YAML parser if the config is invalid.
     * @throws InvalidConfigurationException By the YAML parser if the config is invalid.
     * @throws NullPointerException If the configuration section "commands" doesn't exist
     */
    fun getNewCommands(): HashMap<String, AliasCommand> {
        val cmds: HashMap<String, AliasCommand> = Maps.newHashMap()
        try {
            cfg.getConfigurationSection("commands").getKeys(false).forEach(Consumer { t ->
                var label: String = t
                val enabled = cfg.getBoolean("commands.$t.enabled")

                val permission = cfg.getString("commands.$t.permission")
                val aliases = cfg.getStringList("commands.$t.aliases")
                val type: CommandType = CommandType.values().firstOrNull { label.startsWith(it.prefix) }
                        ?: CommandType.CMD
                if (type != CommandType.CMD) label = label.split(type.prefix)[1]
                try {
                    val command = AliasCommand(label, enabled, permission, aliases, type)
                    cmds.put(label.toLowerCase(), command)
                } catch (e: IllegalStateException) {
                    core.log("The config is improperly defined! Cannot load alias $label.", Level.SEVERE)
                    core.error = "Failed to set alias instance (${e.message})"
                    e.printStackTrace()
                }
            })
        } catch (e: ScannerException) {
            core.log("The config is improperly defined! Please refer to http://www.yamllint.com/", Level.SEVERE)
            core.error = "Bad config"
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            core.log("The config is improperly defined! Please refer to http://www.yamllint.com/", Level.SEVERE)
            core.error = "Bad config"
            e.printStackTrace()
        } catch (e: NullPointerException) {
            core.log("The config is improperly defined! Please refer to http://www.yamllint.com/", Level.SEVERE)
            core.error = "Configuration section 'commands' doesn't exist" // what
            e.printStackTrace()
        }
        /* THANK YOU KOTLIN FOR THIS LOVELY DOUBLE CATCH :)) */
        return cmds
    }

    fun getPrefix(): String = cfg.getString("prefix")

    fun getNoPerm(): String = cfg.getString("noPermission").replace("{prefix}", getPrefix())

    fun save(config: YamlConfiguration) {
        config.save(file)
    }

    fun reload() {
        cfg = YamlConfiguration.loadConfiguration(file)
    }

    /***
     * @return Pair<HashMap<String, AliasCommand> = The new hashmap, boolean (has the map been modified?)
     */
    fun toggleAlias(commands: HashMap<String, AliasCommand>, label: String): Pair<HashMap<String, AliasCommand>, Boolean> {
        val alias: AliasCommand = commands[label.toLowerCase()] ?: return Pair(commands, false)

        cfg.set("commands.${alias.serialiseLabel().toLowerCase()}.enabled", !alias.enabled)
        save(cfg)
        alias.enabled = !alias.enabled

        return Pair(commands, true)
    }

}