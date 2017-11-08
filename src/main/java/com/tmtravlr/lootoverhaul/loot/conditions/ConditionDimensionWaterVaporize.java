package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if water vaporizes in this dimension (like the Nether)
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension_water_vaporizes"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionDimensionWaterVaporize implements LootCondition {

	public ConditionDimensionWaterVaporize() {
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		return context.getWorld().provider.doesWaterVaporize();
	}

	public static class Serializer extends LootCondition.Serializer<ConditionDimensionWaterVaporize> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "dimension_water_vaporizes"), ConditionDimensionWaterVaporize.class);
        }

        public void serialize(JsonObject json, ConditionDimensionWaterVaporize value, JsonSerializationContext context) {
        }

        public ConditionDimensionWaterVaporize deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionDimensionWaterVaporize();
        }
    }

}
