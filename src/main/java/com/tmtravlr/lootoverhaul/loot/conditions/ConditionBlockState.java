package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Arrays;
import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;
import com.tmtravlr.lootoverhaul.misc.BlockStateMatcher;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Explanation
 * 
 *  A metadata of 1
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:block_state",
 *  		"meta": 1
 *  	}
 *  ]
 *  
 *  Or a metadata of at least 4
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:block_state",
 *  		"meta": {
 *  			"min": 4
 *  		}
 *  	}
 *  ]
 *  
 *  Or a list of metadata
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:block_state",
 *  		"meta": [
 *  			0,
 *  			5
 *  		]
 *  	}
 *  ]
 *  
 *  Or facing upwards
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:block_state",
 *  		"state": {
 *  			"facing": "up"
 *  		}
 *  	}
 *  ]
 *  
 *  Or any purple or pink block (wool, terracotta, concrete, etc.) 
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:block_state",
 *  		"state": {
 *  			"color": [
 *  				"purple",
 *  				"pink"
 *  			]
 *  		}
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ConditionBlockState implements LootCondition {

	private RangeFloat metaRange;
	private int[] metaList;
	private BlockStateMatcher[] states;
	
	public ConditionBlockState(RangeFloat metaRangeToSet, int[] metaListToSet, BlockStateMatcher[] blockStatesToSet) {
		this.metaRange = metaRangeToSet;
		this.metaList = metaListToSet;
		this.states = blockStatesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		if (context instanceof LootContextExtended) {
			LootContextExtended contextExtended = (LootContextExtended) context;
			BlockPos pos = contextExtended.getPosition();
			IBlockState state = contextExtended.getBrokenBlockState();
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);
			
			if (metaRange != null) {
				if (!metaRange.isInRange((float) meta)) {
					return false;
				}
			}
			
			if (metaList != null) {
				if(!Arrays.asList(metaList).contains(meta)) {
					return false;
				}
			}
			
			if (states != null) {
				for (BlockStateMatcher stateMatcher : states) {
					if (!stateMatcher.matches(state)) {
						return false;
					}
				}
			}
			
			return true;
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionBlockState> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "block_state"), ConditionBlockState.class);
        }

        public void serialize(JsonObject json, ConditionBlockState value, JsonSerializationContext context) {
        	if (value.metaList != null) {
        		LootHelper.serializeIntArray(value.metaList, json, "meta");
        	} else if (value.metaRange != null) {
        		value.metaRange.serialize(json, "meta");
        	}
        	if (value.states != null) {
        		BlockStateMatcher.serialize(value.states, json, "state");
        	}
        }

        public ConditionBlockState deserialize(JsonObject json, JsonDeserializationContext context) {
        	
        	RangeFloat metaRange = null;
        	int[] metaList = null;
        	BlockStateMatcher[] states = null;
        	
        	if (json.has("meta")) {
        		if(json.get("meta").isJsonObject()) {
        			metaRange = LootHelper.deserializeRangeFloat(json, "meta");
        		} else {
        			metaList = LootHelper.deserializeIntArray(json, "meta");
        		}
        	}
        	if (json.has("state")) {
        		states = BlockStateMatcher.deserialize(json, "state");
        	}
        	
        	return new ConditionBlockState(metaRange, metaList, states);
        }
    }

}
