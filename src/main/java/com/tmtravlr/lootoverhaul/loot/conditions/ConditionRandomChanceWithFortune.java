package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Passes via random chance affected by fortune, mirroring vanilla looting.
 * 
 * Example Usage: Rare drop increased 10% per fortune level
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:random_chance_with_fortune",
 *  		"chance": 0.1,
 *  		"fortune_multiplier": 0.1
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since March 2018
 */
public class ConditionRandomChanceWithFortune implements LootCondition {
	private final float chance;
	private final float fortuneMultiplier;

	public ConditionRandomChanceWithFortune(float chanceIn, float fortuneMultiplierIn) {
		this.chance = chanceIn;
		this.fortuneMultiplier = fortuneMultiplierIn;
	}

	public boolean testCondition(Random rand, LootContext context) {
		int i = LootHelper.getFortuneModifierFromContext(context);

		return rand.nextFloat() < this.chance + (float)i * this.fortuneMultiplier;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionRandomChanceWithFortune> 	{
		public Serializer() {
			super(new ResourceLocation(LootOverhaul.MOD_ID, "random_chance_with_fortune"), ConditionRandomChanceWithFortune.class);
		}

		public void serialize(JsonObject json, ConditionRandomChanceWithFortune value, JsonSerializationContext context) {
			json.addProperty("chance", Float.valueOf(value.chance));
			json.addProperty("fortune_multiplier", Float.valueOf(value.fortuneMultiplier));
		}

		public ConditionRandomChanceWithFortune deserialize(JsonObject json, JsonDeserializationContext context) {
			return new ConditionRandomChanceWithFortune(JsonUtils.getFloat(json, "chance"), JsonUtils.getFloat(json, "fortune_multiplier"));
		}
	}
}