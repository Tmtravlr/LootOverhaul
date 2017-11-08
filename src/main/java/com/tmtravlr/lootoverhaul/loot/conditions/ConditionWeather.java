package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if this is the current world's weather. Must be 'clear',
 * 'rain', or 'thunder'. Rain also means snow if it's snowing (raining
 * with a temperature below 0.15).
 * 
 * Example Usage: (only applies if in rainy, snowy, or stormy weather)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:weather",
 *  		"weather": [
 *  			"rain",
 *  			"thunder"
 *  		]
 *  	}
 *  ]
 *  Or you don't need an array for just one value: (only clear weather)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:weather",
 *  		"weather": "clear"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionWeather implements LootCondition {

	private String[] weatherTypes;

    public ConditionWeather(String[] weatherToSet) {
        this.weatherTypes = weatherToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		Biome biome = pos != null ? context.getWorld().getBiome(pos) : null;
		boolean rain = context.getWorld().isRaining() && (biome == null || biome.getEnableSnow() || biome.canRain());
		boolean thunder = context.getWorld().isThundering();
				
		for(String weather : weatherTypes) {
			if((weather.equalsIgnoreCase("rain") && rain) || (weather.equalsIgnoreCase("thunder") && thunder) || (weather.equalsIgnoreCase("clear") && !rain && !thunder)) {
				
				return true;
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionWeather> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "weather"), ConditionWeather.class);
        }

        public void serialize(JsonObject json, ConditionWeather value, JsonSerializationContext context) {
        	
        	LootHelper.serializeStringArray(value.weatherTypes, json, "weather");
        }

        public ConditionWeather deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	String[] weatherStrings = LootHelper.deserializeStringArray(json, "weather");
        	checkNames(weatherStrings);

            return new ConditionWeather(weatherStrings);
        }
        
        private void checkNames(String[] weatherStrings) {
        	for(String weatherString : weatherStrings) {
	        	if(!weatherString.equalsIgnoreCase("clear") && !weatherString.equalsIgnoreCase("rain") && !weatherString.equalsIgnoreCase("thunder")) {
	        		throw new JsonSyntaxException("Weather type '"+weatherString+"' not recognized; it must be 'clear', 'rain', or 'thunder'.");
	        	}
        	
        	}
        }
    }

}
