package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.ConfigLoader;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.fml.common.FMLLog;

/**
 * Will pass if all its conditions are false
 * 
 * Example Usage: (only applies if not in a deep ocean biome)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:not",
 *  		"conditions": [
 *  			{
 *  				"condition": "lootoverhaul:in_biome",
 *  				"biomes": [
 *  					"minecraft:deep_ocean"
 *  				]
 *  			}
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionNot implements LootCondition {

	private final LootCondition[] conditions;

    public ConditionNot(LootCondition[] conditionList) {
        this.conditions = conditionList;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = null;
		boolean pass = true;
		
		for(LootCondition condition : conditions) {
			if(!condition.testCondition(rand, context)) {
				pass = false;
			}
		}
		
		return !pass;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionNot> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "not"), ConditionNot.class);
        }

        public void serialize(JsonObject json, ConditionNot value, JsonSerializationContext context) {
        	
            JsonArray conditionList = new JsonArray();
            
            for (LootCondition condition : value.conditions) {
            	JsonObject conditionObject = new JsonObject();
            	LootConditionManager.getSerializerFor(condition).serialize(conditionObject, condition, context);
            	conditionList.add(conditionObject);
            }

            json.add("conditions", conditionList);
        }

        public ConditionNot deserialize(JsonObject json, JsonDeserializationContext context) {
        	
            JsonArray conditionArray = JsonUtils.getJsonArray(json, "conditions");
            LootCondition[] conditionList = new LootCondition[conditionArray.size()];
            
            for(int i = 0; i < conditionArray.size(); i++) {
            	if(conditionArray.get(i).isJsonObject() && conditionArray.get(i).getAsJsonObject().get("condition").isJsonPrimitive() && conditionArray.get(i).getAsJsonObject().get("condition").getAsJsonPrimitive().isString()) {
            		
            		conditionList[i] = LootConditionManager.getSerializerForName(new ResourceLocation(conditionArray.get(i).getAsJsonObject().get("condition").getAsString())).deserialize(conditionArray.get(i).getAsJsonObject(), context);
            	}
            }

            return new ConditionNot(conditionList);
        }
    }

}
