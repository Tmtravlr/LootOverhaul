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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the humidity is within this range.
 * You can use both min and max, or leave one out.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome_humidity"
 *  		"humidity": {
 *  			"min": 0.7
 *  			"max": 1.0
 *  		}
 *  	}
 *  ]
 *  
 *  Some base vanilla biome humidities, for reference:
 *  Desert: 0.0
 *  Extreme Hills: 0.3
 *  Plains: 0.4
 *  Ocean: 0.5
 *  Forest: 0.8
 *  Jungle: 0.9
 *  Mushroom Island: 1.0
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionBiomeHumidity implements LootCondition {

	private RangeFloat humidity;
	
	public ConditionBiomeHumidity(RangeFloat range) {
		this.humidity = range;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if(pos != null) {
			Biome biome = context.getWorld().getBiome(pos);
			
			return humidity.isInRange(biome.getRainfall());
		}
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionBiomeHumidity> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "biome_humidity"), ConditionBiomeHumidity.class);
        }

        public void serialize(JsonObject json, ConditionBiomeHumidity value, JsonSerializationContext context) {
        	
        	value.humidity.serialize(json, "humidity");
        }

        public ConditionBiomeHumidity deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionBiomeHumidity(LootHelper.deserializeRangeFloat(json, "humidity"));
        }
	}

}
