package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.ConfigLoader;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.fml.common.FMLLog;

/**
 * Will pass of all if its conditions pass
 * 
 * Example Usage: (will pass if in a 'sandy' biome and it's clear out)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:and",
 *  		"conditions": [
 *  			{
 *  				"condition": "lootoverhaul:in_biome_type",
 *  				"biome_types": [
 *  					"SANDY"
 *  				]
 *  			},
 *  			{
 *  				"condition": "lootoverhaul:weather",
 *  				"weather": "CLEAR"
 *  			}
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since March 2018
 */
public class ConditionAnd implements LootCondition {

	private final LootCondition[] conditions;

    public ConditionAnd(LootCondition[] conditionList) {
        this.conditions = conditionList;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		for (LootCondition condition : conditions) {
			if (!condition.testCondition(rand, context)) {
				return false;
			}
		}
		
		return true;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionAnd> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "and"), ConditionAnd.class);
        }

        public void serialize(JsonObject json, ConditionAnd value, JsonSerializationContext context) {
        	
            JsonArray conditionList = new JsonArray();
            
            for (LootCondition condition : value.conditions)
            {
            	JsonObject conditionObject = new JsonObject();
            	LootConditionManager.getSerializerFor(condition).serialize(conditionObject, condition, context);
            	conditionList.add(conditionObject);
            }

            json.add("conditions", conditionList);
        }

        public ConditionAnd deserialize(JsonObject json, JsonDeserializationContext context) {
        	
            JsonArray conditionArray = JsonUtils.getJsonArray(json, "conditions");
            LootCondition[] conditionList = new LootCondition[conditionArray.size()];
            
            for(int i = 0; i < conditionArray.size(); i++) {
            	if(conditionArray.get(i).isJsonObject() && conditionArray.get(i).getAsJsonObject().get("condition").isJsonPrimitive() && conditionArray.get(i).getAsJsonObject().get("condition").getAsJsonPrimitive().isString()) {
            		
            		conditionList[i] = LootConditionManager.getSerializerForName(new ResourceLocation(conditionArray.get(i).getAsJsonObject().get("condition").getAsString())).deserialize(conditionArray.get(i).getAsJsonObject(), context);
            	}
            }

            return new ConditionAnd(conditionList);
        }
    }

}
