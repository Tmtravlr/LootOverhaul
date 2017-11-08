package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the entity killed/looted/etc. has this type.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"entity_type": [
 *  			"minecraft:zombie",
 *  			"minecraft:skeleton"
 *  		]
 *  	}
 *  ]
 *  Or with only one name:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"entity_type": "minecraft:chest_minecart"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionEntityType implements LootCondition {
	
	private String[] entityTypes;
	
	public ConditionEntityType(String[] entityTypesToSet) {
		this.entityTypes = entityTypesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		Entity entity = null;
		
		if(context.getLootedEntity() != null) {
			entity = context.getLootedEntity();
		}
		
		if(entity != null) {
			for(String entityTypeName : entityTypes) {
				ResourceLocation entityType = new ResourceLocation(entityTypeName);
				if(entityType.equals(EntityList.getKey(entity))) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionEntityType> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "entity_type"), ConditionEntityType.class);
        }

        public void serialize(JsonObject json, ConditionEntityType value, JsonSerializationContext context) {
        	LootHelper.serializeStringArray(value.entityTypes, json, "entity_type");
        }

        public ConditionEntityType deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionEntityType(LootHelper.deserializeStringArray(json, "entity_type"));
        }
    }

}
