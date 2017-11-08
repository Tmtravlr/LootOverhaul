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
 * Will pass if the dimension has a sky like the overworld and end.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension_has_sky"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionDimensionHasSky implements LootCondition {

	public ConditionDimensionHasSky() {
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		return context.getWorld().provider.hasSkyLight();
	}

	public static class Serializer extends LootCondition.Serializer<ConditionDimensionHasSky> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "dimension_has_sky"), ConditionDimensionHasSky.class);
        }

        public void serialize(JsonObject json, ConditionDimensionHasSky value, JsonSerializationContext context) {
        }

        public ConditionDimensionHasSky deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionDimensionHasSky();
        }
    }

}
