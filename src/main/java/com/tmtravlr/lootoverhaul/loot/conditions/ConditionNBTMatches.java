package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Checks if an entity's nbt matches this tag.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:nbt_matches",
 *  		"target": "this",
 *  		"tag": "{CustomName:\"Mr. Sir\"}"
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionNBTMatches implements LootCondition {

	private NBTTagCompound tagToMatch;
	private ExtendedEntityTarget target;
	
	public ConditionNBTMatches(NBTTagCompound tagToMatch, ExtendedEntityTarget target) {
		this.tagToMatch = tagToMatch;
		this.target = target;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		Entity entity = LootHelper.getEntityFromContext(context, target);
		
		if (entity != null) {
			NBTTagCompound entityTag = new NBTTagCompound();
			entityTag = entity.writeToNBT(entityTag);
			
			return NBTUtil.areNBTEquals(tagToMatch, entityTag, true);
		}
		
		return false;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionNBTMatches> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "nbt_matches"), ConditionNBTMatches.class);
        }

        public void serialize(JsonObject json, ConditionNBTMatches value, JsonSerializationContext context) {
        	json.addProperty("tag", value.tagToMatch.toString());
        	LootHelper.serializeExtendedEntityTarget(json, "target", value.target);
        }

        public ConditionNBTMatches deserialize(JsonObject json, JsonDeserializationContext context) {
        	String tagString = JsonUtils.getString(json, "tag");
        	ExtendedEntityTarget target = LootHelper.deserializeExtendedEntityTarget(json, "target", ExtendedEntityTarget.THIS);
        	
        	try {
        		return new ConditionNBTMatches(JsonToNBT.getTagFromJson(tagString), target);
        	} catch (NBTException e) {
        		throw new JsonSyntaxException("Invalid nbt tag.", e);
        	}
        	
        }
    }

}
