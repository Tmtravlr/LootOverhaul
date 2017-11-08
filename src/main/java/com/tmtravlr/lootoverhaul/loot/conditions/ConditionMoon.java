package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Explanation
 * 
 * Example Usage: During a full moon
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:moon_phase"
 *  		"phase": "full"
 *  	}
 *  ]
 *  
 * Example Usage: During a crecent moon
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:moon_phase"
 *  		"phase": {
 *  			"waxing_crecent",
 *  			"waning_crecent"
 * 			}
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionMoon implements LootCondition {
	
	private MoonPhase[] moonPhases;

	public ConditionMoon(MoonPhase[] moonPhases) {
		this.moonPhases = moonPhases;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		int moonPhaseIndex = context.getWorld().provider.getMoonPhase(context.getWorld().getWorldTime());
		MoonPhase currentMoonPhase = MoonPhase.getMoonPhaseFromIndex(moonPhaseIndex);
		
		for (MoonPhase moonPhase : this.moonPhases) {
			if (currentMoonPhase == moonPhase) {
				return true;
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionMoon> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "moon_phase"), ConditionMoon.class);
        }

        public void serialize(JsonObject json, ConditionMoon value, JsonSerializationContext context) {
        	String[] phaseStrings = new String[value.moonPhases.length];
        	for (int i = 0; i < value.moonPhases.length; i++) {
        		phaseStrings[i] = value.moonPhases[i].name().toLowerCase();
        	}
        	LootHelper.serializeStringArray(phaseStrings, json, "phase");
        }

        public ConditionMoon deserialize(JsonObject json, JsonDeserializationContext context) {
        	String[] phaseStrings = LootHelper.deserializeStringArray(json, "phase");
        	MoonPhase[] phases = new MoonPhase[phaseStrings.length];
        	
        	for (int i = 0; i < phaseStrings.length; i++) {
        		MoonPhase phase = MoonPhase.valueOf(phaseStrings[i].toUpperCase());
        		if (phase == null) {
        			throw new JsonSyntaxException("Moon phase '"+phaseStrings[i]+"' not recognized; it must be 'new', 'waxing_crecent', 'first_quarter', 'waxing_gibbous', 'full', 'waning_gibbous', 'last_quarter', or 'waning_crecent'.");
        		}
        		phases[i] = phase;
        	}

            return new ConditionMoon(phases);
        }
    }
	
	enum MoonPhase {
		FULL(0),
		WANING_GIBBOUS(1),
		LAST_QUARTER(2),
		WANING_CRECENT(3),
		NEW(4),
		WAXING_CRECENT(5),
		FIRST_QUARTER(6),
		WAXING_GIBBOUS(7);
		
		private int phaseIndex;
		
		private MoonPhase(int phaseIndex) {
			this.phaseIndex = phaseIndex;
		}
		
		public static MoonPhase getMoonPhaseFromIndex(int index) {
			for (MoonPhase phase : values()) {
				if (index == phase.phaseIndex) {
					return phase;
				}
			}
			
			return null;
		}
	}

}
