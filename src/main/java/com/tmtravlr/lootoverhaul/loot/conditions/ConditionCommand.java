package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.commands.CommandSenderGeneric;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if a command runs successfully
 * 
 * Example Usage: (assuming you have a scoreboard objective called deaths)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:command",
 *  		"command": "testfor @a[r=16,score_deaths_min=100]"
 *  	}
 *  ]
 *  Or you could give it a list of commands: (tests for a player at least level 30 or a creeper nearby)
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:command",
 *  		"command": [
 *  			"testfor @a[lm=30,r=32]",
 *  			"testfor @e[type=minecraft:creeper,r=32]"
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionCommand implements LootCondition {

	String[] commands;
	
	public ConditionCommand(String[] commandsToSet) {
		commands = commandsToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		CommandSenderGeneric sender = new CommandSenderGeneric("@", context.getWorld(), pos == null ? BlockPos.ORIGIN : pos);
		if(context.getWorld().getMinecraftServer() != null) {
			for(String command : commands) {
				int success = context.getWorld().getMinecraftServer().commandManager.executeCommand(sender, command);
				if(success > 0) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionCommand> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "command"), ConditionCommand.class);
        }

        public void serialize(JsonObject json, ConditionCommand value, JsonSerializationContext context) {
        	
        	LootHelper.serializeStringArray(value.commands, json, "command");
        }

        public ConditionCommand deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionCommand(LootHelper.deserializeStringArray(json, "command"));
        }
    }

}
