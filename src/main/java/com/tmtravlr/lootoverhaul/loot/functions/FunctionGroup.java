package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

/**
 * Runs a group of functions, useful with conditions like random
 * chance (so all are chosen together). If you give it a number or
 * range called number_to_apply, it will randomly choose that many
 * functions to apply from the list.
 * 
 * Example Usage:
 * 
 * Runs two functions
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:function_group",
 *  		"functions": [
 *  			{
 *  				"function": "minecraft:furnace_smelt"
 *  			},
 *  			{
 *  				"function": "minecraft:set_count",
 *  				"count": 10
 *  			}
 *  		]
 *  	}
 * ]
 * 
 * Will choose one or two functions to run
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:function_group",
 *  		"number_to_apply": {
 *  			"min": 1,
 *  			"max": 2
 * 			},
 *  		"functions": [
 *  			{
 *  				"function": "lootoverhaul:set_nbt",
 *  				"tag": "{display:{Lore:\"Really Awesome Weapon\"}}"
 *  			},
 *  			{
 *  				"function": "minecraft:enchant_randomly"
 *  			},
 *  			{
 *  				"function": "minecraft:set_damage",
 *  				"damage": 0.3
 *  			}
 *  		]
 *  	}
 * ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since July 2018
 */
public class FunctionGroup extends LootFunction {

	private List<LootFunction> functions;
	private RandomValueRange numberToApply;
	
	public FunctionGroup(LootCondition[] conditionsIn, List<LootFunction> functions, RandomValueRange numberToApply) {
		super(conditionsIn);
		this.functions = functions;
		this.numberToApply = numberToApply;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		int numToApply = functions.size();
		
		if (this.numberToApply != null) {
			numToApply = this.numberToApply.generateInt(rand);
		}
		
		numToApply = MathHelper.clamp(numToApply, 0, functions.size());
		int numToRemove = functions.size() - numToApply;
		List<LootFunction> functionsToApply = new LinkedList<>(functions);
		
		//Remove functions, making sure to preserve order (since order matters)
		for (int i = 0; i < numToRemove; i++) {
			int toRemove = rand.nextInt(functionsToApply.size());
			functionsToApply.remove(toRemove);
		}
		
		for (LootFunction function : functionsToApply) {
			stack = function.apply(stack, rand, context);
		}

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionGroup> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "function_group"), FunctionGroup.class);
        }

        public void serialize(JsonObject json, FunctionGroup value, JsonSerializationContext serializationContext) {
        	JsonArray functionArray = new JsonArray();
        	
        	for (LootFunction function : value.functions) {
        		JsonObject functionObject = new JsonObject();
        		LootFunctionManager.getSerializerFor(function).serialize(functionObject, function, serializationContext);
        		functionArray.add(functionObject);
        	}
        	
        	if (value.numberToApply != null) {
        		json.add("number_to_apply", serializationContext.serialize(value.numberToApply));
        	}
        	
        	json.add("functions", functionArray);
        }

        public FunctionGroup deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
        	JsonArray functionArray = JsonUtils.getJsonArray(json, "functions");
        	final List<LootFunction> functions = new ArrayList<>();
        	RandomValueRange numberToApply = null;
        	
        	functionArray.forEach(functionObject -> {
        		functions.add(LootFunctionManager.getSerializerForName(new ResourceLocation(JsonUtils.getString(functionObject.getAsJsonObject(), "function"))).deserialize(functionObject.getAsJsonObject(), deserializationContext, conditionsIn));
        	});
        	
        	if (json.has("number_to_apply")) {
        		numberToApply = (RandomValueRange)JsonUtils.deserializeClass(json, "number_to_apply", deserializationContext, RandomValueRange.class);
        	}
        	
            return new FunctionGroup(conditionsIn, functions, numberToApply);
        }
    }
}
