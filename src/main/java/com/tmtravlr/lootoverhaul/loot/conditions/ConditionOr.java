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
 * Will pass of any of it's conditions pass
 * 
 * Example Usage: (will pass if in a 'watery' biome or it's raining/storming)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:or",
 *  		"conditions": [
 *  			{
 *  				"condition": "lootoverhaul:in_biome_type",
 *  				"biome_types": [
 *  					"WATER"
 *  				]
 *  			},
 *  			{
 *  				"condition": "lootoverhaul:weather",
 *  				"weather": [
 *  					"rain",
 *  					"storm"
 *  				]
 *  			}
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionOr implements LootCondition {

	private final LootCondition[] conditions;

    public ConditionOr(LootCondition[] conditionList) {
        this.conditions = conditionList;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = null;
		
		for(LootCondition condition : conditions) {
			if(condition.testCondition(rand, context)) {
				return true;
			}
		}
		
		return false;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionOr> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "or"), ConditionOr.class);
        }

        public void serialize(JsonObject json, ConditionOr value, JsonSerializationContext context) {
        	
            JsonArray conditionList = new JsonArray();
            
            for (LootCondition condition : value.conditions)
            {
            	JsonObject conditionObject = new JsonObject();
            	LootConditionManager.getSerializerFor(condition).serialize(conditionObject, condition, context);
            	conditionList.add(conditionObject);
            }

            json.add("conditions", conditionList);
        }

        public ConditionOr deserialize(JsonObject json, JsonDeserializationContext context) {
        	
            JsonArray conditionArray = JsonUtils.getJsonArray(json, "conditions");
            LootCondition[] conditionList = new LootCondition[conditionArray.size()];
            
            for(int i = 0; i < conditionArray.size(); i++) {
            	if(conditionArray.get(i).isJsonObject() && conditionArray.get(i).getAsJsonObject().get("condition").isJsonPrimitive() && conditionArray.get(i).getAsJsonObject().get("condition").getAsJsonPrimitive().isString()) {
            		
            		conditionList[i] = LootConditionManager.getSerializerForName(new ResourceLocation(conditionArray.get(i).getAsJsonObject().get("condition").getAsString())).deserialize(conditionArray.get(i).getAsJsonObject(), context);
            	}
            }

            return new ConditionOr(conditionList);
        }
    }

}
