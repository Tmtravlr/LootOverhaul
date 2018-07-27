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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Checks the light level, either for the sky, blocks (like torches), or combined (highest of the two)
 * 
 * Example Usage: In high sunlight
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:light_level",
 *  		"type": "SKY",
 *  		"level": {
 *  			"min": 10
 *  		}
 *  	}
 *  ]
 * 
 * Example Usage: In low torchlight
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:light_level",
 *  		"type": "BLOCK",
 *  		"level": {
 *  			"max": 5
 *  		}
 *  	}
 *  ]
 * 
 * Example Usage: In pitch darkness
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:light_level",
 *  		"type": "COMBINED",
 *  		"level": 0
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionLightLevel implements LootCondition {
	
	private LightType type;
	private RangeFloat level;

	public ConditionLightLevel(RangeFloat level, LightType type) {
		this.level = level;
		this.type = type;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if (pos != null) {
			float skyLight = context.getWorld().getLightFor(EnumSkyBlock.SKY, pos);
			float blockLight = context.getWorld().getLightFor(EnumSkyBlock.BLOCK, pos);
			
			if (type == LightType.SKY) {
				return level.isInRange(skyLight);
			} else if (type == LightType.BLOCK) {
				return level.isInRange(blockLight);
			} else {
				return level.isInRange(Math.max(skyLight, blockLight));
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionLightLevel> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "light_level"), ConditionLightLevel.class);
        }

        public void serialize(JsonObject json, ConditionLightLevel value, JsonSerializationContext context) {
        }

        public ConditionLightLevel deserialize(JsonObject json, JsonDeserializationContext context) {
        	RangeFloat level = LootHelper.deserializeRangeFloat(json, "level");
        	LightType type = LightType.COMBINED;
        	
        	if (json.has("type")) {
        		try {
        			type = LightType.valueOf(JsonUtils.getString(json, "type").toUpperCase());
        		} catch (IllegalArgumentException e) {
        			throw new JsonSyntaxException("Unknown light type. Try using SKY, BLOCK, or COMBINED.", e);
        		}
        	}
        	
        	return new ConditionLightLevel(level, type);
        }
    }

	private enum LightType {
		SKY,
		BLOCK,
		COMBINED
	}
	
}
