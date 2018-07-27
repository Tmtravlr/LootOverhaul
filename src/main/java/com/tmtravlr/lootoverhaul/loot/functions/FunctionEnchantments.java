package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.ArrayList;
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

/**
 * Adds enchantments to the item. More of a convenience, since
 * modded enchantment ids change between worlds, and enchantments
 * on items still use numerical ids for some reason.
 * 
 * Example Usage:
 * 
 * Adds silk touch
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:enchantments",
 *  		"enchantment": {
 *  			"name": "minecraft:silk_touch"
 *  		}
 *  	}
 * ]
 *  
 * Also can take a list of enchantments to choose randomly between.
 * Will add 2 of sharpness, smite, or bane of arthropods enchantment with level 1-3:
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:enchantments",
 *  		"enchantment": [
 *  			{
 *  				"name": "minecraft:sharpness",
 *          		"level": {
 *          			"min": 1,
 *          			"max": 3
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:smite",
 *          		"level": {
 *          			"min": 1,
 *          			"max": 3
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:bane_of_arthropods",
 *          		"level": {
 *          			"min": 1,
 *          			"max": 3
 *          		}
 *  			}
 *          ],
 *          "number_to_generate": 2
 *  	}
 * ]
 * 
 * If replace is set, instead of generating a new enchantment list,
 * it will try replacing the given string with an enchantment tag.
 * Example: An enchantment book with silk touch, mending, or efficiency
 * 
 * "functions": [
 *  	{
 * 			"function": "set_nbt",
 * 			"tag": "{StoredEnchantments:\"#GeneratedEnchantments\"}"
 * 		},
 * 		{
 *  		"function": "lootoverhaul:enchantments",
 *  		"enchantment": [
 *  			{
 *  				"name": "minecraft:silk_touch"
 *  			},
 *  			{
 *  				"name": "minecraft:mending"
 *  			},
 *  			{
 *  				"name": "minecraft:efficiency",
 *          		"level": {
 *          			"min": 1,
 *          			"max": 5
 *          		}
 *  			}
 *          ],
 *          "replace": "\"#GeneratedEnchantments\""
 *  	}
 * ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since March 2018
 */
public class FunctionEnchantments extends LootFunction {

	private List<EnchantmentDetails> details;
	private String replace;
	private RandomValueRange numberToGenerate;
	
	public FunctionEnchantments(LootCondition[] conditionsIn, List<EnchantmentDetails> enchantmentDetails, String replace, RandomValueRange numberToGenerate) {
		super(conditionsIn);
		this.details = enchantmentDetails;
		this.replace = replace;
		this.numberToGenerate = numberToGenerate;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		int numToRemove = details.size() - MathHelper.clamp(numberToGenerate.generateInt(rand), 1, details.size());
		
		List<EnchantmentDetails> enchantmentsToGenerate = new ArrayList<>(details);
		
		for (int i = 0; i < numToRemove; i++) {
			enchantmentsToGenerate.remove(rand.nextInt(enchantmentsToGenerate.size()));
		}
		
		Map<Enchantment, Integer> enchantmentMap = enchantmentsToGenerate.stream()
				.filter(details -> Enchantment.getEnchantmentByLocation(details.name) != null)
				.collect(Collectors.toMap(details -> Enchantment.getEnchantmentByLocation(details.name), details -> details.level.generateInt(rand)));
		
		if (this.replace.isEmpty()) {
			Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.getEnchantments(stack);
			stackEnchantments.putAll(enchantmentMap);
			EnchantmentHelper.setEnchantments(stackEnchantments, stack);
		} else if (stack.hasTagCompound()) {
			String enchantmnetsString = "[";
			StringJoiner enchantmentJoiner = new StringJoiner(",");
			enchantmentMap.forEach( (ench, level) -> {
				enchantmentJoiner.add("{id:" + Enchantment.getEnchantmentID(ench) + ", lvl:" + level + "}");
			});
			enchantmnetsString += enchantmentJoiner.toString() + "]";
			
    		String tagString = stack.getTagCompound().toString();
    		
    		if (!this.replace.equals(enchantmnetsString)) {
				tagString = tagString.replace(this.replace, enchantmnetsString);
    		}
			
			try {
				stack.setTagCompound(JsonToNBT.getTagFromJson(tagString));
			} catch (NBTException e) {
				LootOverhaul.logger.warn("Unable to generate loot enchantments.", e);
			}
		}

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionEnchantments> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "enchantments"), FunctionEnchantments.class);
        }

        public void serialize(JsonObject json, FunctionEnchantments value, JsonSerializationContext serializationContext) {
        	if (value.details.size() == 1) {
        		json.add("enchantment", value.details.get(0).serialize(serializationContext));
        	} else {
        		JsonArray jsonArray = new JsonArray();
        		value.details.forEach(detail -> jsonArray.add(detail.serialize(serializationContext)));
        		json.add("enchantment", jsonArray);
        	}
        	
    		json.add("number_to_generate", serializationContext.serialize(value.numberToGenerate));
    		if (!value.replace.isEmpty()) {
    			json.addProperty("replace", value.replace);
    		}
        }

        public FunctionEnchantments deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
        	
        	List<EnchantmentDetails> enchantments = new ArrayList<>();
        	RandomValueRange numberToGenerate;
        	String replace = JsonUtils.getString(json, "replace", "");
        	
        	if (JsonUtils.isJsonArray(json, "enchantment")) {
        		JsonArray jsonArray = JsonUtils.getJsonArray(json, "enchantment");
        		jsonArray.forEach(element -> enchantments.add(EnchantmentDetails.deserialize(JsonUtils.getJsonObject(element, "member of enchantment"), deserializationContext, conditionsIn)));
        	
	        	if (enchantments.size() == 0) {
	        		throw new JsonSyntaxException("At least one enchantment is required");
	        	}
        	} else {
        		enchantments.add(EnchantmentDetails.deserialize(JsonUtils.getJsonObject(json, "enchantment"), deserializationContext, conditionsIn));
        	}
        	
        	if (json.has("number_to_generate")) {
        		numberToGenerate = (RandomValueRange)JsonUtils.deserializeClass(json, "number_to_generate", deserializationContext, RandomValueRange.class);
        	} else {
        		numberToGenerate = new RandomValueRange(1);
        	}
        	
            return new FunctionEnchantments(conditionsIn, enchantments, replace, numberToGenerate);
        }
    }
	
	private static class EnchantmentDetails {
		private String name;
		private RandomValueRange level;
		
		private EnchantmentDetails(String enchantmentName, RandomValueRange enchantmentLevel) {
			this.name = enchantmentName;
			this.level = enchantmentLevel;
		}
		
		private JsonObject serialize(JsonSerializationContext serializationContext) {
			JsonObject json = new JsonObject();
			json.addProperty("name", this.name);
    		json.add("level", serializationContext.serialize(this.level));
    		return json;
		}
		
		private static EnchantmentDetails deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
			String name = JsonUtils.getString(json, "name");
        	RandomValueRange level;
        	
        	if (json.has("level")) {
        		level = (RandomValueRange)JsonUtils.deserializeClass(json, "level", deserializationContext, RandomValueRange.class);
        	} else {
        		level = new RandomValueRange(1);
        	}
        	
        	return new EnchantmentDetails(name, level);
		}
	}
}
