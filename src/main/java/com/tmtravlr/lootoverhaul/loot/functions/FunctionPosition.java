package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.items.ItemLoot;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

/**
 * Sets the loot's position to an entity or a static position
 * 
 * Example Usage:
 * 
 * Entity: Will set the loot's position to the looter
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:position",
 *  		"entity": "looter"
 *  	}
 *  ]
 *  
 *  Entity: Will set the loot's position to the nearest player
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:position",
 *  		"entity": "@p"
 *  	}
 *  ]
 *  
 *  Static Position: Will set the loot's position to the given position
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:position",
 *  		"x": "0",
 *  		"y": "64",
 *  		"z": "0"
 *  	}
 *  ]
 *  
 *  Static Position: Just set the y position
 * 
 * "functions": [
 *  	{
 *  		"function": "lootoverhaul:position",
 *  		"y": "128"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class FunctionPosition extends LootFunction {
	
    private ExtendedEntityTarget target;
    private String selector;
	private Double posX;
	private Double posY;
	private Double posZ;
    
    public FunctionPosition(LootCondition[] conditions, ExtendedEntityTarget target, String selector, Double posX, Double posY, Double posZ) {
    	super(conditions);
        this.target = target;
        this.selector = selector;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (!(stack.getItem() instanceof ItemLoot)) {
			stack = ItemLoot.createStackFromItem(stack);
		}
		
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		NBTTagCompound positionTag = new NBTTagCompound();
		
		if (this.target != null) {
			Entity targetEntity = LootHelper.getEntityFromContext(context, target);
			
			if (targetEntity != null) {
				positionTag.setString("Entity", targetEntity.getUniqueID().toString());
			}
		} else if (this.selector != null) {
			positionTag.setString("Entity", this.selector);
		} else {
			if (posX != null) {
				positionTag.setDouble("X", posX);
			}
			if (posY != null) {
				positionTag.setDouble("Y", posY);
			}
			if (posZ != null) {
				positionTag.setDouble("Z", posZ);
			}
		}
		
		stack.getTagCompound().setTag("Position", positionTag);

        return stack;
	}
	
	public static class Serializer extends LootFunction.Serializer<FunctionPosition> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "position"), FunctionPosition.class);
        }

        public void serialize(JsonObject json, FunctionPosition value, JsonSerializationContext serializationContext) {
        	if (value.target != null) {
        		LootHelper.serializeExtendedEntityTarget(json, "entity", value.target);
        	}
        	if (value.selector != null) {
        		json.addProperty("entity", value.selector);
        	}
        	if (value.posX != null) {
        		json.addProperty("x", value.posX);
        	}
        	if (value.posY != null) {
        		json.addProperty("y", value.posY);
        	}
        	if (value.posZ != null) {
        		json.addProperty("z", value.posZ);
        	}
        }

        public FunctionPosition deserialize(JsonObject json, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
        	
        	ExtendedEntityTarget target = null;
            String selector = null;
        	Double posX = null;
        	Double posY = null;
        	Double posZ = null;
        	
        	if (json.has("entity")) {
	        	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "entity");
	        	if (target == null) {
	        		selector = JsonUtils.getString(json, "entity");
	        	}
        	} else {
        		if (json.has("x")) {
        			posX = (double) JsonUtils.getFloat(json, "x");
        		}
        		if (json.has("y")) {
        			posY = (double) JsonUtils.getFloat(json, "y");
        		}
        		if (json.has("z")) {
        			posZ = (double) JsonUtils.getFloat(json, "z");
        		}
        	}
        	
            return new FunctionPosition(conditionsIn, target, selector, posX, posY, posZ);
        }
    }

}
