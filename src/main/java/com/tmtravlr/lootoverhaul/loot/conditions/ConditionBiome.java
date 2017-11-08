package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if in one of these biomes. Must be the biome string id,
 * NOT the biome name that appears in F3.
 * For a list of biomes, see config/Loot Overhaul/Useful Info/Biomes.txt
 * 
 * Example Usage: (only applies if in a beach or ocean biome)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome",
 *  		"biome": [
 *  			"minecraft:beaches",
 *  			"minecraft:stone_beach",
 *  			"minecraft:cold_beach",
 *  			"minecraft:ocean",
 *  			"minecraft:deep_ocean",
 *  			"minecraft:frozen_ocean"
 *  		]
 *  	}
 *  ]
 *  Or use 'biome' with a single biome: (only applies in a forest biome)
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome",
 *  		"biome": "minecraft:forest"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionBiome implements LootCondition {

	private final Biome[] biomes;

    public ConditionBiome(Biome[] biomesToSet) {
        this.biomes = biomesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if(pos != null) {
			for(Biome biome: biomes) {
				if(context.getWorld().getBiome(pos) == biome) {
					return true;
				}
			}
		}
		
		return false;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionBiome> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "biome"), ConditionBiome.class);
        }

        public void serialize(JsonObject json, ConditionBiome value, JsonSerializationContext context) {
        	
        	if(value.biomes.length == 1) {
        		json.add("biome", new JsonPrimitive(Biome.REGISTRY.getNameForObject(value.biomes[0]).toString()));
        	}
        	else {
        	
	            JsonArray biomeList = new JsonArray();

	            for (Biome biome : value.biomes) {
	                biomeList.add(new JsonPrimitive(Biome.REGISTRY.getNameForObject(biome).toString()));
	            }
	
	            json.add("biome", biomeList);
        	}
        }

        public ConditionBiome deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	if(JsonUtils.isString(json, "biome")) {
        		return new ConditionBiome(new Biome[]{getBiomeFromString(JsonUtils.getString(json, "biome"))});
        	}
        	
            JsonArray biomeArray = JsonUtils.getJsonArray(json, "biome");
            Biome[] biomesToSet = new Biome[biomeArray.size()];
            
            for(int i = 0; i < biomeArray.size(); i++) {
            	if(biomeArray.get(i).isJsonPrimitive() && biomeArray.get(i).getAsJsonPrimitive().isString()) {
            		
	            	biomesToSet[i] = getBiomeFromString(biomeArray.get(i).getAsString());
            	}
            	else {
                    throw new JsonSyntaxException("Expected biome array element to be a String");
                }
            }

            return new ConditionBiome(biomesToSet);
        }
        
        private Biome getBiomeFromString(String biomeName) {
        	Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeName));
        	
        	if(biome == null) {
        		throw new JsonSyntaxException("Can't find a biome with name: " + biomeName);
        	}
        	
        	return biome;
        }
    }

}
