package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the current dimension's 'actual' height is in this height range.
 * 
 * 
 * Example Usage: (looks for 'shorter' dimensions like the Nether, which is 128 blocks tall)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:dimension_height"
 *  		"height": {
 *  			"min": 64
 *  			"max": 128
 *  		}
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionDimensionHeight implements LootCondition {

	private RangeFloat height;
	
	public ConditionDimensionHeight(RangeFloat range) {
		this.height = range;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		return height.isInRange((float)context.getWorld().provider.getActualHeight());
	}

	public static class Serializer extends LootCondition.Serializer<ConditionDimensionHeight> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "dimension_height"), ConditionDimensionHeight.class);
        }

        public void serialize(JsonObject json, ConditionDimensionHeight value, JsonSerializationContext context) {
        	if(value.height.min != null && value.height.max != null && value.height.min.equals(value.height.max)) {
        		json.add("height", new JsonPrimitive(value.height.min));
        	}
        	else {
        		value.height.serialize(json, "height");
        	}
        }

        public ConditionDimensionHeight deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	if(json.has("height") && JsonUtils.isNumber(json.get("height"))) {
        		return new ConditionDimensionHeight(new RangeFloat(JsonUtils.getFloat(json, "height"), JsonUtils.getFloat(json, "height")));
        	}
        	
        	return new ConditionDimensionHeight(LootHelper.deserializeRangeFloat(json, "height"));
        }
    }

}
