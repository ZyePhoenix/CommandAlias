package de.elliepotato.commandalias.backend.prerun.impl

import de.elliepotato.commandalias.backend.AliasCommand
import de.elliepotato.commandalias.backend.prerun.RunCondition
import org.bukkit.entity.Player

/**
 * Created by Ellie on 16/02/2020 for CommandAlias.
 *
 *    Copyright 2020 Ellie
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
class RunConditionHealth : RunCondition {

    override fun getId(): String {
        return "health"
    }

    override fun meetsConditions(alias: AliasCommand, args: List<String>, player: Player): Boolean {
        player.sendMessage(player.health.toString())
        return when (val any = alias.runConditions[getId()]) {
            is Int, Double -> player.health == any
            else -> return true
        }
    }

}