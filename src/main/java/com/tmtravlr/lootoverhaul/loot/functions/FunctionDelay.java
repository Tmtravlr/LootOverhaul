package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.items.ItemLoot;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

/**
 * Delays a drop by the given number of ticks
 * 
 * Example Usage:
 * 
 * Will delay the drop between 1 and 5 seconds (20 and 100 ticks)
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:delay",
 *  		"delay": {
 *				"min": 20,
 *				"max": 100
 *			},
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class FunctionDelay extends LootFunction {
	
	private RandomValueRange delay;
    
    public FunctionDelay(LootCondition[] conditions, RandomValueRange delay) {
    	super(conditions);
        this.delay = delay;
    }

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (!(stack.getItem() instanceof ItemLoot)) {
			stack = ItemLoot.createStackFromItem(stack);
		}
		
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.getTagCompound().setInteger("Delay", this.delay.generateInt(rand));

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionDelay> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "delay"), FunctionDelay.class);
        }

        public void serialize(JsonObject json, FunctionDelay value, JsonSerializationContext serializationContext) {
            json.add("delay", serializationContext.serialize(value.delay));
        }

        public FunctionDelay deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
            return new FunctionDelay(conditionsIn, (RandomValueRange)JsonUtils.deserializeClass(json, "delay", deserializationContext, RandomValueRange.class));
        }
    }

}
