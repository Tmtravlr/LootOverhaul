package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Changes the drops based on tool type killed or harvested with.
 * 
 * Example Usage: Single type
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:tool_type",
 *  		"type": "shears"
 *  	}
 *  ]
 *  
 *  Example Usage: Array
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:tool_type",
 *  		"type": [
 *  			"pickaxe",
 *  			"axe"
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionToolType implements LootCondition {
	
	private String[] toolTypes;

	public ConditionToolType(String[] toolTypes) {
		this.toolTypes = toolTypes;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		Entity looter;
		if (context instanceof LootContextExtended) {
			looter = LootHelper.getEntityFromContext(context, ExtendedEntityTarget.LOOTER);
		} else {
			looter = context.getKiller();
		}
		
		if (looter instanceof EntityLivingBase) {
			ItemStack heldItemStack = ((EntityLivingBase) looter).getHeldItemMainhand();
			Item heldItem = heldItemStack.getItem();
			Set<String> heldItemTypes = new HashSet<>(heldItem.getToolClasses(heldItemStack));
			
			if (heldItem instanceof ItemSword) {
				heldItemTypes.add("sword");
			}
			
			if (heldItem instanceof ItemShears) {
				heldItemTypes.add("shears");
			}
			
			if (heldItem instanceof ItemHoe) {
				heldItemTypes.add("hoe");
			}
			
			if (heldItem instanceof ItemBow) {
				heldItemTypes.add("bow");
			}
			
			for (String toolType : toolTypes) {
				if (heldItemTypes.contains(toolType)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionToolType> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "tool_type"), ConditionToolType.class);
        }

        public void serialize(JsonObject json, ConditionToolType value, JsonSerializationContext context) {
        	LootHelper.serializeStringArray(value.toolTypes, json, "type");
        }

        public ConditionToolType deserialize(JsonObject json, JsonDeserializationContext context) {
        	String[] types = LootHelper.deserializeStringArray(json, "type");
        	return new ConditionToolType(types);
        }
    }

}
