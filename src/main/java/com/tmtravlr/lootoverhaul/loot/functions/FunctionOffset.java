package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.items.ItemLoot;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

/**
 * Adds this offset to the loot's position
 * 
 * Example Usage:
 *  
 * Offset: Will offset the loot's position to the given position
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:offset",
 *  		"x": 10,
 *  		"y": 10,
 *  		"z": 10
 *  	}
 * ]
 * 
 * Offset: Just offset the y position
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:offset",
 *  		"y": {
 *  			"min": 5,
 *  			"max": 30
 *  		}
 *  	}
 * ]
 * 
 * Offset Radius: Offset at a position on a circle with this radius
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:offset",
 *  		"radius": 10
 *  	}
 * ]
 * 
 * Offset to a surface: Tries to find a solid surface for the loot, checking at most
 * 5 blocks up or down.
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:offset",
 *  		"surface_search_max": 5
 *  	}
 * ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class FunctionOffset extends LootFunction {
	
	private RandomValueRange offsetX;
	private RandomValueRange offsetY;
	private RandomValueRange offsetZ;
	private RandomValueRange offsetRadius;
	private int surfaceSearchMax;
    
    public FunctionOffset(LootCondition[] conditions, RandomValueRange offsetX, RandomValueRange offsetY, RandomValueRange offsetZ, RandomValueRange offsetRadius, int surfaceSearchMax) {
    	super(conditions);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.offsetRadius = offsetRadius;
        this.surfaceSearchMax = surfaceSearchMax;
    }

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (!(stack.getItem() instanceof ItemLoot)) {
			stack = ItemLoot.createStackFromItem(stack);
		}
		
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		NBTTagCompound offsetTag = new NBTTagCompound().getCompoundTag("Offset");
		
		if (offsetX != null) {
			offsetTag.setDouble("X", offsetTag.getDouble("X") + offsetX.generateFloat(rand));
		}
		if (offsetY != null) {
			offsetTag.setDouble("Y", offsetTag.getDouble("Y") + offsetY.generateFloat(rand));
		}
		if (offsetZ != null) {
			offsetTag.setDouble("Z", offsetTag.getDouble("Z") + offsetZ.generateFloat(rand));
		}
		if (offsetRadius != null) {
			offsetTag.setDouble("Radius", offsetTag.getDouble("Radius") + offsetRadius.generateFloat(rand));
		}
		if (surfaceSearchMax >= 0) {
			offsetTag.setInteger("SurfaceSearch", surfaceSearchMax);
		}
		
		stack.getTagCompound().setTag("Offset", offsetTag);

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionOffset> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "offset"), FunctionOffset.class);
        }

        public void serialize(JsonObject json, FunctionOffset value, JsonSerializationContext serializationContext) {
        	if (value.offsetX != null) {
        		json.add("x", serializationContext.serialize(value.offsetX));
        	}
        	if (value.offsetY != null) {
        		json.add("y", serializationContext.serialize(value.offsetY));
        	}
        	if (value.offsetZ != null) {
        		json.add("z", serializationContext.serialize(value.offsetZ));
        	}
        	if (value.offsetRadius != null) {
        		json.add("radius", serializationContext.serialize(value.offsetRadius));
        	}
        	if (value.surfaceSearchMax >= 0) {
        		json.addProperty("surface_search_max", value.surfaceSearchMax);
        	}
        }

        public FunctionOffset deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
        	
        	RandomValueRange posX = null;
        	RandomValueRange posY = null;
        	RandomValueRange posZ = null;
        	RandomValueRange radius = null;
        	int surfaceSearchMax = JsonUtils.getInt(json, "surface_search_max", -1);
        	
    		if (json.has("x")) {
    			posX = (RandomValueRange)JsonUtils.deserializeClass(json, "x", deserializationContext, RandomValueRange.class);
    		}
    		if (json.has("y")) {
    			posY = (RandomValueRange)JsonUtils.deserializeClass(json, "y", deserializationContext, RandomValueRange.class);
    		}
    		if (json.has("z")) {
    			posZ = (RandomValueRange)JsonUtils.deserializeClass(json, "z", deserializationContext, RandomValueRange.class);
    		}
    		if (json.has("radius")) {
    			radius = (RandomValueRange)JsonUtils.deserializeClass(json, "radius", deserializationContext, RandomValueRange.class);
    		}
        	
            return new FunctionOffset(conditionsIn, posX, posY, posZ, radius, surfaceSearchMax);
        }
    }

}
