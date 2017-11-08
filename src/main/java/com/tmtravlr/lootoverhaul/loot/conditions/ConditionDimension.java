package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if inside one of these dimensions.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension",
 *  		"dimension": [
 *  			"overworld",
 *  			"the_end"
 *  		]
 *  	}
 *  ]
 *  Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension",
 *  		"dimension": "the_nether"
 *  	}
 *  ]
 *  And it is also compatible with dimension ids:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension",
 *  		"dimension_id": [
 *  			0,
 *  			1
 *  		]
 *  	}
 *  ]
 *  Or:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension",
 *  		"dimension_id": -1
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionDimension implements LootCondition {

	private String[] dimensions;
	private int[] dimensionIds;
	
	public ConditionDimension(String[] dimsToSet, int[] dimIdsToSet) {
		dimensions = dimsToSet;
		dimensionIds = dimIdsToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		if(dimensions != null) {
			for(String name: dimensions) {
				if(name.equalsIgnoreCase(context.getWorld().provider.getDimensionType().getName())) {
					return true;
				}
			}
		}
		
		if(dimensionIds != null) {
			for(int id : dimensionIds) {
				if(id == context.getWorld().provider.getDimension()) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionDimension> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "dimension"), ConditionDimension.class);
        }

        public void serialize(JsonObject json, ConditionDimension value, JsonSerializationContext context) {
        	LootHelper.serializeStringArray(value.dimensions, json, "dimension");
        	LootHelper.serializeIntArray(value.dimensionIds, json, "dimension_id");
        }

        public ConditionDimension deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	if(!JsonUtils.hasField(json, "dimension") && !JsonUtils.hasField(json, "dimension_id")) {
        		throw new JsonSyntaxException("Expected one or both fields 'dimension' and 'dimension_id'");
        	}
        	
        	String[] dimensions = new String[0];
        	int[] dimensionIds = new int[0];
        	
        	if(JsonUtils.hasField(json, "dimension")) {
        		dimensions = LootHelper.deserializeStringArray(json, "dimension");
        	}
        	
        	if(JsonUtils.hasField(json, "dimension_id")) {
        		dimensionIds = LootHelper.deserializeIntArray(json, "dimension_id");
        	}
        	
        	return new ConditionDimension(dimensions, dimensionIds);
        }
    }

}
