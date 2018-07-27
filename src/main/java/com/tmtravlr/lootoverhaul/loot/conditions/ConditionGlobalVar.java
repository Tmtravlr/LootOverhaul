package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;
import com.tmtravlr.lootoverhaul.utilities.SavedData;
import com.tmtravlr.lootoverhaul.utilities.SavedData.StoredVar;
import com.tmtravlr.lootoverhaul.utilities.SavedData.StoredVarFloat;
import com.tmtravlr.lootoverhaul.utilities.SavedData.StoredVarInt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the stored global variable is one of these values. Could be 
 * a single value, a list, or a min/max range.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:global_variable",
 *  		"name": "some_variable",
 *  		"type": "string",
 *  		"value": [
 *  			"First Possible Value",
 *  			"Another Possible Value"
 *  		]
 *  	}
 *  ]
 * Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:global_variable",
 *  		"name": "times_looted",
 *  		"type": "integer",
 *  		"value": 4
 *  	}
 *  ]
 * Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:global_variable",
 *  		"name": "zombies_killed",
 *  		"type": "integer",
 *  		"value": {
 *  			"min": 6,
 *  			"max": 12
 *  		}
 *  	}
 *  ]
 * Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:global_variable",
 *  		"name": "player_has_killed_wither",
 *  		"type": "boolean",
 *  		"value": true
 *  	}
 *  ]
 * Or: Check if variable is set
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:global_variable",
 *  		"name": "player_has_killed_wither"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionGlobalVar implements LootCondition {

	private String name;
	private String type;
    private NBTBase[] valueList;
    private RangeFloat valueRange;
	
	public ConditionGlobalVar(String nameToSet, String typeToSet, NBTBase[] valuesToSet, RangeFloat rangeToSet) {
		this.name = nameToSet;
		this.type = typeToSet;
		this.valueList = valuesToSet;
		this.valueRange = rangeToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		SavedData globalSavedData = SavedData.getSavedData(context.getWorld().getMinecraftServer().getEntityWorld());
		StoredVar var;
		
		if (this.type.isEmpty()) {
			var = globalSavedData.getGlobalVar(this.name);
			return var != null;
		} else if (this.valueRange == null && this.valueList == null) {
			var = globalSavedData.getGlobalVar(this.name);
			return var != null && var.getType() == this.type;
		} else {
			var = globalSavedData.getGlobalVarByType(this.name, this.type);
		}
		
		if(this.valueRange != null) {
			if(var instanceof StoredVarFloat) {
				return valueRange.isInRange(((StoredVarFloat)var).value);
			} else if(var instanceof StoredVarInt) {
				return valueRange.isInRange((float) ((StoredVarInt)var).value);
			}
		}
		
		if(this.valueList != null) {
			for(NBTBase value : this.valueList) {
				if(value.equals(var.toNBT())) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionGlobalVar> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "global_variable"), ConditionGlobalVar.class);
        }

        public void serialize(JsonObject json, ConditionGlobalVar value, JsonSerializationContext context) {
        	json.add("name", new JsonPrimitive(value.name));
        	json.add("type", new JsonPrimitive(value.type));
        	
        	if(value.valueList.length == 1) {
        		LootHelper.serializeNBT(json, "value", value.valueList[0]);
        	}
        	
        	JsonArray array = new JsonArray();
        	
        	for(NBTBase base : value.valueList) {
        		JsonObject innerJson = new JsonObject();
        		LootHelper.serializeNBT(innerJson, "value", base);
        		array.add(innerJson);
        	}
        	
        	json.add("value", array);
        }

        public ConditionGlobalVar deserialize(JsonObject json, JsonDeserializationContext context) {
        	NBTBase[] array = null;
        	RangeFloat range = null;
        	
        	String type = JsonUtils.getString(json, "type", "");
        	
        	if (!type.isEmpty() && json.has("value")) {
	        	if ((type.equalsIgnoreCase("float") || type.equalsIgnoreCase("integer")) && json.has("value") && json.get("value").isJsonObject()) {
	        		range = LootHelper.deserializeRangeFloat(json, "value");
	        	} else {
	        		array = LootHelper.deserializeNBTArray(json, "value");
	        	}
        	}
        	
        	return new ConditionGlobalVar(JsonUtils.getString(json, "name"), type, array, range);
        }
    }

}
