package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;

/**
 * Increases the drops based on the fortune enchantment level
 * 
 * Example Usage:
 * 
 * Will drop 0-1 more every fortune level.
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:fortune_enchant",
 *  		"count": {
 *				"min": 0,
 *				"max": 1
 *			},
 *  	}
 * ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since September 2017
 */
public class FunctionFortuneEnchant extends LootFunction {
	
	private RandomValueRange count;
    private int limit;
    
    public FunctionFortuneEnchant(LootCondition[] conditions, RandomValueRange countIn, int limitIn) {
    	super(conditions);
        this.count = countIn;
        this.limit = limitIn;
    }

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (context instanceof LootContextExtended) {
			LootContextExtended contextExtended = (LootContextExtended) context;
			Entity entity = contextExtended.getLooter();
	
	        if (entity instanceof EntityLivingBase && context instanceof LootContextExtended) {
	            int fortune = contextExtended.getFortuneModifier();
	
	            if (fortune == 0) {
	                return stack;
	            }
	
	            float extra = (float)fortune * this.count.generateFloat(rand);
	            stack.grow(Math.round(extra));
	
	            if (this.limit != 0 && stack.getCount() > this.limit) {
	                stack.setCount(this.limit);
	            }
	        }
		}

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionFortuneEnchant> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "fortune_enchant"), FunctionFortuneEnchant.class);
        }

        public void serialize(JsonObject object, FunctionFortuneEnchant value, JsonSerializationContext serializationContext) {
            object.add("count", serializationContext.serialize(value.count));

            if (value.limit > 0)
            {
                object.add("limit", serializationContext.serialize(Integer.valueOf(value.limit)));
            }
        }

        public FunctionFortuneEnchant deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
            return new FunctionFortuneEnchant(conditionsIn, (RandomValueRange)JsonUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class), JsonUtils.getInt(object, "limit", 0));
        }
    }

}
