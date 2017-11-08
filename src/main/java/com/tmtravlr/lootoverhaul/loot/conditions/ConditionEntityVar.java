package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;
import com.tmtravlr.lootoverhaul.misc.SavedData;
import com.tmtravlr.lootoverhaul.misc.SavedData.StoredVar;
import com.tmtravlr.lootoverhaul.misc.SavedData.StoredVarFloat;
import com.tmtravlr.lootoverhaul.misc.SavedData.StoredVarInt;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the stored entity variable for the target entity is one of these values.
 * Could be a single value, a list, or a min/max range.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_variable",
 *  		"name": "some_variable",
 *  		"target": "this",
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
 *  		"condition": "lootoverhaul:entity_variable",
 *  		"name": "times_looted",
 *  		"target": "this",
 *  		"type": "integer",
 *  		"value": 4
 *  	}
 *  ]
 * Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_variable",
 *  		"name": "zombies_killed",
 *  		"target": "killer",
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
 *  		"condition": "lootoverhaul:entity_variable",
 *  		"name": "player_has_killed_wither",
 *  		"target": "killer_player",
 *  		"type": "boolean",
 *  		"value": true
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionEntityVar implements LootCondition {

	private String name;
	private String type;
    private NBTBase[] valueList;
    private RangeFloat valueRange;
    private ExtendedEntityTarget target;
	
	public ConditionEntityVar(String nameToSet, String typeToSet, NBTBase[] valuesToSet, RangeFloat rangeToSet, ExtendedEntityTarget targetToSet) {
		this.name = nameToSet;
		this.type = typeToSet;
		this.valueList = valuesToSet;
		this.valueRange = rangeToSet;
		this.target = targetToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		Entity entity = LootHelper.getEntityFromContext(context, this.target);
		
		if (entity == null) {
			return false;
		}
		
		StoredVar var = SavedData.getEntityVar(entity, this.name, this.type);
		
		if(this.valueRange != null) {
			if(var instanceof StoredVarFloat) {
				return this.valueRange.isInRange(((StoredVarFloat)var).value);
			} else if(var instanceof StoredVarInt) {
				return this.valueRange.isInRange((float) ((StoredVarInt)var).value);
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

	public static class Serializer extends LootCondition.Serializer<ConditionEntityVar> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "entity_variable"), ConditionEntityVar.class);
        }

        public void serialize(JsonObject json, ConditionEntityVar value, JsonSerializationContext context) {
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
        	LootHelper.serializeExtendedEntityTarget(json, "target", value.target);
        }

        public ConditionEntityVar deserialize(JsonObject json, JsonDeserializationContext context) {
        	NBTBase[] array = null;
        	RangeFloat range = null;
        	
        	String type = JsonUtils.getString(json, "type");
        	
        	if((type.equalsIgnoreCase("float") || type.equalsIgnoreCase("integer")) && json.has("value") && json.get("value").isJsonObject()) {
        		range = LootHelper.deserializeRangeFloat(json, "value");
        	}
        	else {
        		array = LootHelper.deserializeNBTArray(json, "value");
        	}
        	
        	ExtendedEntityTarget target = LootHelper.deserializeExtendedEntityTarget(json, "target", ExtendedEntityTarget.THIS);
        	
        	return new ConditionEntityVar(JsonUtils.getString(json, "name"), type, array, range, target);
        }
    }

}
