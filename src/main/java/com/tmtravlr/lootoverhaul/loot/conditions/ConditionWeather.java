package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
 * Will pass if this is the weather at the loot's position.
 * 
 * Example Usage: (only applies if in rainy, snowy, or stormy weather)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:weather",
 *  		"weather": [
 *  			"RAIN",
 *  			"SNOW",
 *  			"THUNDER"
 *  		]
 *  	}
 *  ]
 *  Or you don't need an array for just one value: (only clear weather)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:weather",
 *  		"weather": "CLEAR"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionWeather implements LootCondition {

	private List<WeatherType> weatherTypes;

    public ConditionWeather(List<WeatherType> weatherToSet) {
        this.weatherTypes = weatherToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		boolean rain = context.getWorld().isRaining();
		boolean snow = false;
		boolean overcast = false;
		
		if (pos != null) {
			Biome biome =context.getWorld().getBiome(pos);
			rain = context.getWorld().isRainingAt(pos);
			snow = context.getWorld().isRaining() && context.getWorld().canSnowAt(pos, false);
			overcast = context.getWorld().isRaining() && !biome.canRain();
		}
		
		boolean thunder = context.getWorld().isThundering();
		boolean clear = !rain && !snow && !overcast && !thunder;
				
		for(WeatherType weather : weatherTypes) {
			if((weather == WeatherType.CLEAR && clear) || 
					(weather == WeatherType.OVERCAST && overcast) ||
					(weather == WeatherType.RAIN && rain) ||
					(weather == WeatherType.SNOW && snow) ||
					(weather == WeatherType.THUNDER && thunder)) {
				
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
        	
        	LootHelper.serializeStringArray(value.weatherTypes.stream().map(WeatherType::name).collect(Collectors.toList()).toArray(new String[0]), json, "weather");
        }

        public ConditionWeather deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	String[] weatherStrings = LootHelper.deserializeStringArray(json, "weather");
        	List<WeatherType> weatherTypes = new ArrayList<WeatherType>();
        	
        	Arrays.stream(weatherStrings).forEach(name -> {
        		try {
        			weatherTypes.add(WeatherType.valueOf(name.toUpperCase()));
        		} catch (IllegalArgumentException e) {
        			throw new JsonSyntaxException("Weather type '"+name+"' not recognized; it must be CLEAR, OVERCAST, RAIN, SNOW, or THUNDER.");
        		}
        	});

            return new ConditionWeather(weatherTypes);
        }
        
        private void checkNames(String[] weatherStrings) {
        	
        }
    }
	
	private enum WeatherType {
		CLEAR,
		OVERCAST,
		RAIN,
		SNOW,
		THUNDER
	}

}
