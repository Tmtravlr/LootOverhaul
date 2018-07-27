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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the game is in this difficulty. Known values are 'peaceful',
 * 'easy', 'normal', 'hard', and 'hardcore'. 
 * You can also check the additional difficulty based on area (the 'regional difficulty'
 * https://minecraft.gamepedia.com/Difficulty#Regional_difficulty). For the regional
 * difficulty, you can use both min and max, or leave one out.
 * 
 * Example Usage: (applies if the difficulty is peaceful)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:difficulty",
 *  		"difficulty": "PEACEFUL"
 *  	}
 *  ]
 *  Or: (applies if the difficulty is greater than easy)
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:difficulty",
 *  		"difficulty": [
 *  			"normal",
 *  			"hard",
 *  			"hardcore"
 *  		]
 *  	}
 *  ]
 *  Or: (checks for regional difficulty of at least 2)
 *  "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:difficulty",
 *  		"regional": {
 *  			"min": 2.0
 *  		}
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionDifficulty implements LootCondition {
	
	private String[] difficulties;
	private RangeFloat additional;

	public ConditionDifficulty(String[] difficultiesToSet, RangeFloat additionalRange) {
		this.difficulties = difficultiesToSet;
		this.additional = additionalRange;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		String globalDifficulty = context.getWorld().getDifficulty().name().toLowerCase();
		
		float currentAdditional = 0.0f;
		
		if (pos != null) {
			currentAdditional = context.getWorld().getDifficultyForLocation(pos).getAdditionalDifficulty();
		}
		
		for (String difficulty : difficulties) {
			if (context.getWorld().getWorldInfo().isHardcoreModeEnabled() && difficulty.equalsIgnoreCase("hardcore")) {
				return true;
			}
			if (globalDifficulty.equalsIgnoreCase(difficulty)) {
				return true;
			}
		}
		
		if (additional.min != null || additional.max != null) {
			return additional.isInRange(currentAdditional);
		}
		
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionDifficulty> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "difficulty"), ConditionDifficulty.class);
        }

        public void serialize(JsonObject json, ConditionDifficulty value, JsonSerializationContext context) {
        	if (value.difficulties.length > 0) {
        		LootHelper.serializeStringArray(value.difficulties, json, "difficulty");
        	}
        	
        	if (value.additional.min != null && value.additional.max != null) {
        		value.additional.serialize(json, "regional");
        	}
        }

        public ConditionDifficulty deserialize(JsonObject json, JsonDeserializationContext context) {
        	if (!JsonUtils.hasField(json, "difficulty") && !JsonUtils.hasField(json, "regional")) {
        		throw new JsonSyntaxException("Expected one or both fields 'difficulty' and 'regional'");
        	}
        	
        	String[] difficulties = new String[0];
        	RangeFloat regional = new RangeFloat(null, null);
        	
        	if (JsonUtils.hasField(json, "difficulty")) {
        		difficulties = LootHelper.deserializeStringArray(json, "difficulty");
        	}
        	if (JsonUtils.hasField(json, "regional")) {
        		regional = LootHelper.deserializeRangeFloat(json, "regional");
        	}
        	
        	return new ConditionDifficulty(difficulties, regional);
        }
    }

}
