package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Arrays;
import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;
import com.tmtravlr.lootoverhaul.utilities.BlockStateMatcher;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the blocks are found within the given radius
 * 
 * Example Usage: Within 0 blocks of (so inside) water or lava
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": [
 *  			"minecraft:flowing_water",
 *  			"minecraft:water",
 *  			"minecraft:flowing_lava",
 *  			"minecraft:lava"
 *  		],
 *  		"radius": 0
 *  	}
 *  ]
 *  
 *  Or a single block: Within 5 blocks of mossy cobblestone
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": "minecraft:mossy_cobblestone",
 *  		"radius": 5
 *  	}
 *  ]
 *  
 *  Or a block with metadata: Beside a dispenser facing upwards
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": "minecraft:dispenser",
 *  		"meta": 1,
 *  		"radius": 1
 *  	}
 *  ]
 *  
 *  Or a block with a range of metadata: Near wheat, carrot, or potato crops that are at least halfway grown
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": [
 *  			"minecraft:wheat",
 *  			"minecraft:carrots",
 *  			"minecraft:potatoes"
 *  		],
 *  		"meta": {
 *  			"min": 4
 *  		},
 *  		"radius": 6
 *  	}
 *  ]
 *  
 *  Or a list metadata: Near oak or dark oak planks
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": "minecraft:planks",
 *  		"meta": [
 *  			0,
 *  			5
 *  		],
 *  		"radius": 6
 *  	}
 *  ]
 *  
 *  Or a block with a block state: Next to a dispenser facing upwards
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"block": "minecraft:dispenser",
 *  		"state": {
 *  			"facing": "up"
 *  		},
 *  		"radius": 1
 *  	}
 *  ]
 *  
 *  Or only a block state: Next to any purple or pink block (wool, terracotta, concrete, etc.) 
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:near_block",
 *  		"state": {
 *  			"color": [
 *  				"purple",
 *  				"pink"
 *  			]
 *  		},
 *  		"radius": 1
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionNearBlock implements LootCondition {

	private String[] blocks;
	private RangeFloat metaRange;
	private int[] metaList;
	private int radius;
	private BlockStateMatcher[] states;
	
	public ConditionNearBlock(String[] blocksToSet, RangeFloat metaRangeToSet, int[] metaListToSet, int radiusToSet, BlockStateMatcher[] statesToSet) {
		this.blocks = blocksToSet;
		this.metaRange = metaRangeToSet;
		this.metaList = metaListToSet;
		this.radius = radiusToSet;
		this.states = statesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		BlockPos origin = LootHelper.getPosFromContext(context);
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		
		if (origin != null) {
			for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
				for (int y = origin.getY() - radius; y <= origin.getY() + radius; y++) {
					for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
						pos.setPos(x, y, z);
						IBlockState state = context.getWorld().getBlockState(pos);
						Block block = state.getBlock();
						int meta = block.getMetaFromState(state);
						
						if (metaRange != null) {
							if (!metaRange.isInRange((float) meta)) {
								continue;
							}
						}
						
						if (metaList != null) {
							if(!Arrays.asList(metaList).contains(meta)) {
								continue;
							}
						}
						
						if (states != null) {
							boolean stateMatches = true;
							for (BlockStateMatcher stateMatcher : states) {
								if (!stateMatcher.matches(state)) {
									stateMatches = false;
								}
							}
							if (!stateMatches) {
								continue;
							}
						}
						
						if (blocks != null) {
							boolean blockMatches = false;
							for (String blockName : blocks) {
								Block blockToCheck = Block.REGISTRY.getObject(new ResourceLocation(blockName));
								if(blockToCheck == block) {
									blockMatches = true;
								}
							}
							if (!blockMatches) {
								continue;
							}
						}
						
						pos.release();
						return true;
					}
				}
			}
		}
		
		pos.release();
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionNearBlock> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "near_block"), ConditionNearBlock.class);
        }

        public void serialize(JsonObject json, ConditionNearBlock value, JsonSerializationContext context) {
        	if (value.blocks != null) {
        		LootHelper.serializeStringArray(value.blocks, json, "block");
        	}
        	
        	if (value.metaList != null) {
        		LootHelper.serializeIntArray(value.metaList, json, "meta");
        	} else if (value.metaRange != null) {
        		value.metaRange.serialize(json, "meta");
        	}
        	if (value.states != null) {
        		BlockStateMatcher.serialize(value.states, json, "state");
        	}
        	
        	json.addProperty("radius", value.radius);
        	
        }

        public ConditionNearBlock deserialize(JsonObject json, JsonDeserializationContext context) {
        	String[] blocks = null;
        	RangeFloat metaRange = null;
        	int[] metaList = null;
        	BlockStateMatcher[] states = null;
        	
        	if (json.has("block")) {
        		blocks = LootHelper.deserializeStringArray(json, "block");
        	}
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
        	
        	int radius = JsonUtils.getInt(json, "radius");
        	
        	return new ConditionNearBlock(blocks, metaRange, metaList, radius, states);
        }
	}

}
