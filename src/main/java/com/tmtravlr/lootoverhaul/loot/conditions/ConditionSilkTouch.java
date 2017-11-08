package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Explanation
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:silk_touch_enchant"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionSilkTouch implements LootCondition {

	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		return (context instanceof LootContextExtended) && ((LootContextExtended)context).isSilkTouch();
	}

	public static class Serializer extends LootCondition.Serializer<ConditionSilkTouch> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "silk_touch_enchant"), ConditionSilkTouch.class);
        }

        public void serialize(JsonObject json, ConditionSilkTouch value, JsonSerializationContext context) {
        }

        public ConditionSilkTouch deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionSilkTouch();
        }
    }

}
