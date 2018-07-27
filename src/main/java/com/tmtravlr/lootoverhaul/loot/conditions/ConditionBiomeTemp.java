package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if biome temperature at this location is in this range.
 * 
 * Example Usage: (only applies if in a beach, ocean, or deep ocean biome)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:temperature",
 *  		"temperature": {
 *  			"min": 1.0,
 *  			"max": 2.0
 *  		}
 *  	}
 *  ]
 *  Or you can leave out one of the min or max: 
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:temperature",
 *  		"temperature": {
 *  			"max": 0.15
 *  		}
 *  	}
 *  ]
 *  
 *  Some base vanilla biome temperatures (they lower as you go higher), for reference:
 *  Ocean: 0.5
 *  Plains: 0.8
 *  Jungle: 0.95
 *  Desert: 2.0
 *  Extreme Hills: 0.2 (starts snowing at 0.15)
 *  Ice Plains: 0.0
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionBiomeTemp implements LootCondition {

	private RangeFloat temperature;

    public ConditionBiomeTemp(RangeFloat range) {
        this.temperature = range;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if (pos != null) {
			return this.temperature.isInRange(context.getWorld().getBiome(pos).getTemperature(pos));
		}
		
		return false;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionBiomeTemp> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "temperature"), ConditionBiomeTemp.class);
        }

        public void serialize(JsonObject json, ConditionBiomeTemp value, JsonSerializationContext context) {
        	
        	value.temperature.serialize(json, "temperature");
        }

        public ConditionBiomeTemp deserialize(JsonObject json, JsonDeserializationContext context) {
        	
            return new ConditionBiomeTemp(LootHelper.deserializeRangeFloat(json, "temperature"));
        }
    }

}
