package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the looter was using a silk touch tool.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:silk_touch"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionSilkTouch implements LootCondition {

	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		return LootHelper.getHasSilkTouchFromContext(context);
	}

	public static class Serializer extends LootCondition.Serializer<ConditionSilkTouch> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "silk_touch"), ConditionSilkTouch.class);
        }

        public void serialize(JsonObject json, ConditionSilkTouch value, JsonSerializationContext context) {
        }

        public ConditionSilkTouch deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionSilkTouch();
        }
    }

}
