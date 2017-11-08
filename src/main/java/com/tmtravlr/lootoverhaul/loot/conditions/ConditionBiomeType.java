package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;
import java.util.Set;

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
import net.minecraftforge.common.BiomeDictionary;

/**
 * Will pass if in one of these biome types, as defined in {@link BiomeDictionary.Type}.
 * For a list of what biomes have what biome types, see config/Loot Overhaul/Useful Info/Biomes.txt
 * 
 * Example Usage: (only applies if in a biome considered spooky, dead, or nether)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome_type",
 *  		"biome_type": [
 *  			"SPOOKY",
 *  			"DEAD",
 *  			"NETHER"
 *  		]
 *  	}
 *  ]
 *  Or use 'biome_type' with a single biome type: (applies in any sandy biomes (like deserts))
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:biome_type",
 *  		"biome_type": "SANDY"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionBiomeType implements LootCondition {

	private final BiomeDictionary.Type[] biomeTypes;

    public ConditionBiomeType(BiomeDictionary.Type[] biomesToSet) {
        this.biomeTypes = biomesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if(pos != null) {
			Biome biome = context.getWorld().getBiome(pos);
			Set<BiomeDictionary.Type> currentTypes = BiomeDictionary.getTypes(biome);
			for(BiomeDictionary.Type biomeType: biomeTypes) {
				for(BiomeDictionary.Type currentType : currentTypes) {
					if(currentType == biomeType) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

    public static class Serializer extends LootCondition.Serializer<ConditionBiomeType> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "biome_type"), ConditionBiomeType.class);
        }

        public void serialize(JsonObject json, ConditionBiomeType value, JsonSerializationContext context) {
        	
        	if(value.biomeTypes.length == 1) {
        		json.add("biome_type", new JsonPrimitive(value.biomeTypes[0].getName()));
        	}
        	else {
        	
	            JsonArray biomeList = new JsonArray();

	            for (BiomeDictionary.Type biomeType : value.biomeTypes) {
	                biomeList.add(new JsonPrimitive(biomeType.getName()));
	            }
	
	            json.add("biome_type", biomeList);
        	}
        }

        public ConditionBiomeType deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	if(JsonUtils.isString(json, "biome_type")) {
        		return new ConditionBiomeType(new BiomeDictionary.Type[]{getBiomeTypeFromString(JsonUtils.getString(json, "biome_type"))});
        	}
        	
            JsonArray biomeArray = JsonUtils.getJsonArray(json, "biome_type");
            BiomeDictionary.Type[] biomesToSet = new BiomeDictionary.Type[biomeArray.size()];
            
            for(int i = 0; i < biomeArray.size(); i++) {
            	if(biomeArray.get(i).isJsonPrimitive() && biomeArray.get(i).getAsJsonPrimitive().isString()) {
            		
	            	biomesToSet[i] = getBiomeTypeFromString(biomeArray.get(i).getAsString());
            	}
            	else {
                    throw new JsonSyntaxException("Expected biome type array element to be a String");
                }
            }

            return new ConditionBiomeType(biomesToSet);
        }
        
        private BiomeDictionary.Type getBiomeTypeFromString(String biomeTypeName) {
        	BiomeDictionary.Type biomeType = BiomeDictionary.Type.getType(biomeTypeName);
        	
        	return biomeType;
        }
    }

}
