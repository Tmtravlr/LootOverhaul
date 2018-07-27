package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if this position can see the sky. There's an optional boolean 'direct', 
 * which if true means the position must see the sky directly. Otherwise,
 * transparent blocks can be in between (like glass). Defaults to false.
 * 
 * Example Usage: (only applies if there are no blocks between the loot position and the sky)
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:can_see_sky",
 *  		"direct": true
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionSeeSky implements LootCondition {

	boolean direct = false;

    public ConditionSeeSky(boolean isDirect) {
        this.direct = isDirect;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		BlockPos pos = LootHelper.getPosFromContext(context);
		
		if (pos != null && ((!direct && context.getWorld().canSeeSky(pos)) || context.getWorld().getPrecipitationHeight(pos).getY() <= pos.getY())) {
			return true;
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionSeeSky> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "can_see_sky"), ConditionSeeSky.class);
        }

        public void serialize(JsonObject json, ConditionSeeSky value, JsonSerializationContext context) {
        	
        	json.add("direct", new JsonPrimitive(value.direct));
        }

        public ConditionSeeSky deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	return new ConditionSeeSky(JsonUtils.getBoolean(json, "direct", false));
            
        }
    }

}
