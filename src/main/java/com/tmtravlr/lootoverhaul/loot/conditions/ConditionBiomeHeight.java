package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the biome base height or variation are within this range.
 * You can use both min and max for the base and variaion, or leave one out.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome_height"
 *  		"base": {
 *  			"min": 0.4
 *  			"max": 1.5
 *  		}
 *  		"variation": {
 *  			"max": 0.1
 *  		}
 *  	}
 *  ]
 *  
 *  Some base vanilla biome heights, for reference:
 *  Deep Ocean: Base: -1.8, Variation: 0.1
 *  Ocean: Base: -1.0, Variation: 0.1
 *  Jungle: Base: 0.1, Variation: 0.2
 *  Plains: Base: 0.125, Variation: 0.05
 *  Extreme Hills: Base: 1.0, Variation: 0.5
 *  Savanna Plateau: Base: 1.5, Variation: 0.025
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionBiomeHeight implements LootCondition {
	
	RangeFloat base, variation;

	public ConditionBiomeHeight(RangeFloat baseToSet, RangeFloat varToSet) {
		this.base = baseToSet;
		this.variation = varToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if(pos != null) {
			Biome biome = context.getWorld().getBiome(pos);
			
			if(!base.isInRange(biome.getBaseHeight())) {
				return false;
			}
			
			if(!variation.isInRange(biome.getHeightVariation())) {
				return false;
			}
		}
		
		return true;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionBiomeHeight> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "biome_height"), ConditionBiomeHeight.class);
        }

        public void serialize(JsonObject json, ConditionBiomeHeight value, JsonSerializationContext context) {
        	
        	value.base.serialize(json, "base");
        	value.variation.serialize(json, "variation");
        }

        public ConditionBiomeHeight deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	if(!JsonUtils.hasField(json, "base") && !JsonUtils.hasField(json, "variation")) {
        		throw new JsonSyntaxException("Expected one or both fields 'base' and 'variation'");
        	}
        	
        	RangeFloat base = new RangeFloat(null, null);
        	RangeFloat variation = new RangeFloat(null, null);
        	
        	if(JsonUtils.hasField(json, "base")) {
        		base = LootHelper.deserializeRangeFloat(json, "base");
        	}
        	if(JsonUtils.hasField(json, "variation")) {
        		variation = LootHelper.deserializeRangeFloat(json, "variation");
        	}
        	
        	return new ConditionBiomeHeight(base, variation);
        }
    }

}
