package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

/**
 * Adds custom potion effects to the item (potion, tipped arrow, etc.)
 * More of a convenience, since modded potion ids change between worlds,
 * and custom potions still use numerical ids for some reason.
 * 
 * Example Usage:
 * 
 * Adds an instant health 5 effect
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:custom_potion_effects",
 *  		"potion": {
 *          	"name": "minecraft:instant_health",
 *  			"amplifier": 4
 *  		}
 *  	}
 * ]
 *  
 * Also can take a list of potions to choose randomly between.
 * Will add either a strength or swiftness effect to a potion for 
 * 30 seconds with no particles:
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:custom_potion_effects",
 *  		"potion": [
 *  			{
 *  				"name": "minecraft:strength",
 *          		"duration": 600,
 *          		"hideParticles": true
 *          	},
 *          	{
 *  				"name": "minecraft:speed",
 *          		"duration": 600,
 *          		"hideParticles": true
 *          	}
 *          ]
 *  	}
 * ]
 *  
 * If you add number_to_generate, it will randomly choose that number
 * of potions from the list:
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:custom_potion_effects",
 *  		"potion": [
 *  			{
 *  				"name": "minecraft:wither",
 *         	 		"duration": {
 *          			"min": 1000,
 *          			"max": 3000
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:poison",
 *         	 		"duration": {
 *          			"min": 1000,
 *          			"max": 3000
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:hunger",
 *         	 		"duration": {
 *          			"min": 1000,
 *          			"max": 3000
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:blindness",
 *         	 		"duration": {
 *          			"min": 1000,
 *          			"max": 3000
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:instant_damage",
 *  				"duration": 1,
 *  				"amplifier": {
 *  					"min": 0,
 *  					"max": 3
 *  				}
 *  			}
 *          ],
 *          "number_to_generate": {
 *          	"min": 2,
 *          	"max": 5
 *          }
 *  	}
 * ]
 *  
 * If replace is set, instead of generating a new potion effect tag, it will
 * try replacing the given string in the nbt tag with a potion effect tag.
 * Example: Creating an area effect cloud with effects (assuming the item is lootoverhaul:loot_entity)
 * 
 * "functions": [
 * 		{
 * 			"function": "set_nbt",
 * 			"tag": "{EntityTag:{id:\"minecraft:area_effect_cloud\", Duration:40, RadiusPerTick:0.01, Effects:\"#GeneratedEffects\"}}"
 * 		},
 *  	{
 *  		"function": "lootoverhaul:custom_potion_effects",
 *  		"potion": [
 *  			{
 *  				"name": "minecraft:regeneration",
 *         	 		"duration": {
 *          			"min": 60,
 *          			"max": 200
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:absorption",
 *         	 		"duration": {
 *          			"min": 1200,
 *          			"max": 4000
 *          		}
 *  			},
 *  			{
 *  				"name": "minecraft:saturation",
 *  				"amplifier": 5,
 *  				"duration": 1
 *  			}
 *          ],
 *          "number_to_generate": 2,
 *          "replace": "\"#GeneratedEffects\""
 *  	}
 * ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since March 2018
 */
public class FunctionCustomPotionEffects extends LootFunction {

	private List<PotionEffectDetails> details;
	private String replace;
	private RandomValueRange numberToGenerate;
	
	public FunctionCustomPotionEffects(LootCondition[] conditionsIn, List<PotionEffectDetails> details, String replace, RandomValueRange numberToGenerate) {
		super(conditionsIn);
		this.details = details;
		this.replace = replace;
		this.numberToGenerate = numberToGenerate;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		int numToRemove = details.size() - MathHelper.clamp(numberToGenerate.generateInt(rand), 1, details.size());
		
		List<PotionEffectDetails> effectsToGenerate = new ArrayList<>(details);
		
		for (int i = 0; i < numToRemove; i++) {
			effectsToGenerate.remove(rand.nextInt(effectsToGenerate.size()));
		}
		
		List<PotionEffect> effects = effectsToGenerate.stream()
				.filter(details -> Potion.getPotionFromResourceLocation(details.name) != null)
				.map(details -> new PotionEffect(Potion.getPotionFromResourceLocation(details.name), 
						details.duration.generateInt(rand), 
						details.amplifier.generateInt(rand), 
						details.isAmbient, !details.hideParticles))
				.collect(Collectors.toList());
		
		if (this.replace.isEmpty()) {
			PotionUtils.appendEffects(stack, effects);
		} else if (stack.hasTagCompound()) {
			String customPotionEffectsString = "[";
			StringJoiner effectJoiner = new StringJoiner(",");
			effects.forEach(effect -> {
				NBTTagCompound effectTag = effect.writeCustomPotionEffectToNBT(new NBTTagCompound());
				
				// Can do bad things since it contains strings
				if (effectTag.hasKey("CurativeItems")) {
					effectTag.removeTag("CurativeItems");
				}
				
				effectJoiner.add(effectTag.toString());
			});
			customPotionEffectsString += effectJoiner.toString() + "]";
			
    		String tagString = stack.getTagCompound().toString();
    		
    		if (!this.replace.equals(customPotionEffectsString)) {
				tagString = tagString.replace(this.replace, customPotionEffectsString);
    		}
			
			try {
				stack.setTagCompound(JsonToNBT.getTagFromJson(tagString));
			} catch (NBTException e) {
				LootOverhaul.logger.warn("Unable to generate loot potion effects.", e);
			}
		}

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionCustomPotionEffects> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "custom_potion_effects"), FunctionCustomPotionEffects.class);
        }

        public void serialize(JsonObject json, FunctionCustomPotionEffects value, JsonSerializationContext serializationContext) {
        	if (value.details.size() == 1) {
        		json.add("potion", value.details.get(0).serialize(serializationContext));
        	} else {
        		JsonArray jsonArray = new JsonArray();
        		value.details.forEach(detail -> jsonArray.add(detail.serialize(serializationContext)));
        		json.add("potion", jsonArray);
        	}
        	
    		json.add("number_to_generate", serializationContext.serialize(value.numberToGenerate));
    		if (!value.replace.isEmpty()) {
    			json.addProperty("replace", value.replace);
    		}
        }

        public FunctionCustomPotionEffects deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
        	
        	List<PotionEffectDetails> potions = new ArrayList<>();
        	RandomValueRange numberToGenerate;
        	String replace = JsonUtils.getString(json, "replace", "");
        	
        	if (JsonUtils.isJsonArray(json, "potion")) {
        		JsonArray jsonArray = JsonUtils.getJsonArray(json, "potion");
        		jsonArray.forEach(element -> potions.add(PotionEffectDetails.deserialize(JsonUtils.getJsonObject(element, "member of potion"), deserializationContext, conditionsIn)));
        	
	        	if (potions.size() == 0) {
	        		throw new JsonSyntaxException("At least one potion effect is required");
	        	}
        	} else {
        		potions.add(PotionEffectDetails.deserialize(JsonUtils.getJsonObject(json, "potion"), deserializationContext, conditionsIn));
        	}
        	
        	if (json.has("number_to_generate")) {
        		numberToGenerate = (RandomValueRange)JsonUtils.deserializeClass(json, "number_to_generate", deserializationContext, RandomValueRange.class);
        	} else {
        		numberToGenerate = new RandomValueRange(1);
        	}
        	
            return new FunctionCustomPotionEffects(conditionsIn, potions, replace, numberToGenerate);
        }
    }
	
	private static class PotionEffectDetails {
		private String name;
		private RandomValueRange duration;
		private RandomValueRange amplifier;
		private boolean isAmbient;
		private boolean hideParticles;
		
		private PotionEffectDetails(String potionName, RandomValueRange potionDuration, RandomValueRange potionAmplifier, boolean isAmbient, boolean hideParticles) {
			this.name = potionName;
			this.duration = potionDuration;
			this.amplifier = potionAmplifier;
			this.isAmbient = isAmbient;
			this.hideParticles = hideParticles;
		}
		
		private JsonObject serialize(JsonSerializationContext serializationContext) {
			JsonObject json = new JsonObject();
			json.addProperty("name", this.name);
        	json.addProperty("is_ambient", this.isAmbient);
        	json.addProperty("hide_particles", this.hideParticles);
    		json.add("duration", serializationContext.serialize(this.duration));
    		json.add("amplifier", serializationContext.serialize(this.amplifier));
    		return json;
		}
		
		private static PotionEffectDetails deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
			String name = JsonUtils.getString(json, "name");
        	boolean isAmbient = JsonUtils.getBoolean(json, "is_ambient", false);
        	boolean hideParticles = JsonUtils.getBoolean(json, "hide_particles", false);
        	RandomValueRange duration;
        	RandomValueRange amplifier;
        	
        	if (json.has("duration")) {
        		duration = (RandomValueRange)JsonUtils.deserializeClass(json, "duration", deserializationContext, RandomValueRange.class);
        	} else {
        		duration = new RandomValueRange(1);
        	}
        	
        	if (json.has("amplifier")) {
        		amplifier = (RandomValueRange)JsonUtils.deserializeClass(json, "amplifier", deserializationContext, RandomValueRange.class);
        	} else {
        		amplifier = new RandomValueRange(0);
        	}
        	
        	return new PotionEffectDetails(name, duration, amplifier, isAmbient, hideParticles);
		}
	}
}
