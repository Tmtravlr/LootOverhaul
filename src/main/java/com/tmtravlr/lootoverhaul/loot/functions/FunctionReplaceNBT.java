package com.tmtravlr.lootoverhaul.loot.functions;

import java.util.Random;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.commands.CommandSenderGeneric;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Replaces the given 'replace' string in the item's nbt tag with one of several options:
 * 
 * list - picks a random string in a list
 * range - picks a random integer or float in a range
 * name - name of an entity type (possible values are 'this', 'killer', 'killer_player', 'looter', and 'looter_player')
 * selector - gives a selector for commands (like @e[type=Zombie,c=1]) for an entity type (possible values are same as above)
 * scoreboard - scoreboard value for an entity type (possible values are same as above)
 * 
 * Example Usage:
 * 
 * List: Will drop an item with the name 'Eat me, I'm a ' and a random fruit name.
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{display:{Name:\"Eat me, I'm a #Fruit\"}}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#Fruit",
 *  		"list": [
 *  			"Banana",
 *  			"Kiwi",
 *  			"Watermelon"
 *  		]
 *  	}
 *  ]
 * 
 * Range: Will drop a randomly colored piece of leather armor.
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{display:{Name:\"Randomly Colored Leather Helmet\",color:\"#RandomColor\"}}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "\"#RandomColor\"",
 *  		"range": {
 *  			"min": 0,
 *  			"max": 16777215
 *  		}
 *  	}
 *  ]
 * 
 * Command: Will name an item based on the nearest player (works even if the loot isn't activated by a player)
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{display:{Name:\"#NearestPlayer's Stolen Diamond Sword\"}}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#NearestPlayer",
 *  		"name": "@p"
 *  	}
 *  ]
 * 
 * Name: Will name an item based on the entity's name that dropped it.
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{display:{Name:\"#EntityName's Epic Treasure\"}}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#EntityName",
 *  		"name": "this"
 *  	}
 *  ]
 * 
 * Selector: If the item is the "lootoverhaul:loot_command" item, runs a command to give the killer of this entity 
 * poison for 30 seconds (assuming there is a valid killer).
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{Command:\"effect #EntityName poison 30\"}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#EntityName",
 *  		"uuid": "killer"
 *  	}
 *  ]
 * 
 * Scoreboard: If the item is the "lootoverhaul:loot_command" item, gives the killer a strength buff that increases duration with each kill.
 * "functions": [
 *  	{
 *  		"function": "set_nbt",
 *  		"nbt": "{Command:\"effect #killer minecraft:strength #killCount\"}"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#killer",
 *  		"uuid": "killer"
 *  	},
 *  	{
 *  		"function": "lootoverhaul:replace_nbt",
 *  		"replace": "#killCount",
 *  		"scoreboard": {
 *  			"target": "killer",
 *  			"name": "killCount"
 *  		}
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class FunctionReplaceNBT extends LootFunction {
	
	private String toReplace;
    private String[] valueList;
    private RandomValueRange valueRange;
    private String selector;
    private ExtendedEntityTarget target;
	private String scoreboardName;
	private EntityInfoType infoType;

    public FunctionReplaceNBT(LootCondition[] conditionsIn, String replaceIn, String[] replaceListIn, RandomValueRange rangeIn, String selectorIn, ExtendedEntityTarget targetIn, String scoreboardIn, EntityInfoType infoTypeIn) {
        super(conditionsIn);
        this.toReplace = replaceIn;
        this.valueList = replaceListIn;
        this.valueRange = rangeIn;
        this.target = targetIn;
        this.selector = selectorIn;
        this.scoreboardName = scoreboardIn;
        this.infoType = infoTypeIn;
    }

    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
    	if (stack.hasTagCompound()) {
    		String tagString = stack.getTagCompound().toString();
    		
    		while (tagString.contains(this.toReplace)) {
    			String replacement = null;
    			if (this.valueList != null) {
    				replacement = valueList[rand.nextInt(this.valueList.length)];
    			} else if (this.valueRange != null) {
    				float value = this.valueRange.generateFloat(rand);
    				
    				if (Math.floor(this.valueRange.getMax()) == this.valueRange.getMax() && Math.floor(this.valueRange.getMin()) == this.valueRange.getMin()) {
    					
    					int intValue = MathHelper.floor(value);
    					replacement = "" + intValue;
    				} else {
    					replacement = "" + value;
    				}
    			} else if (this.infoType == EntityInfoType.NAME) {
    				Entity targetEntity = getEntityFromTargetOrSelector(context, this.target, this.selector);

    				if (targetEntity != null) {
    					replacement = targetEntity.getName();
    				}
    			} else if (this.infoType == EntityInfoType.UUID) {
    				Entity targetEntity = getEntityFromTargetOrSelector(context, this.target, this.selector);

    				if (targetEntity != null) {
    					replacement = targetEntity.getUniqueID().toString();
    				}
    			} else if (this.infoType == EntityInfoType.UUID_LEAST) {
    				Entity targetEntity = getEntityFromTargetOrSelector(context, this.target, this.selector);

    				if (targetEntity != null) {
    					replacement = String.valueOf(targetEntity.getUniqueID().getLeastSignificantBits()) + 'L';
    				}
    			} else if (this.infoType == EntityInfoType.UUID_MOST) {
    				Entity targetEntity = getEntityFromTargetOrSelector(context, this.target, this.selector);

    				if (targetEntity != null) {
    					replacement = String.valueOf(targetEntity.getUniqueID().getMostSignificantBits()) + 'L';
    				}
    			} else if (this.infoType == EntityInfoType.SCOREBOARD && this.scoreboardName != null) {
    				Entity targetEntity = getEntityFromTargetOrSelector(context, this.target, this.selector);
    				
    				if (targetEntity != null) {
    					replacement = String.valueOf(LootHelper.getScoreboardScore(targetEntity, this.scoreboardName));
    				}
    			}
    			
    			if (replacement == null) {
    				replacement = "";
    			}
    			
    			if (this.toReplace.equals(replacement)) {
					break;
				}
				tagString = tagString.replaceFirst(this.toReplace, replacement);
    		}
    		
    		try {
				stack.setTagCompound(JsonToNBT.getTagFromJson(tagString));
			} catch (NBTException e) {
				LootOverhaul.logger.catching(e);
			}
    	}
        return stack;
    }
    
    private Entity getEntityFromTargetOrSelector(LootContext context, ExtendedEntityTarget target, String selector) {
    	if (target != null) {
    		return LootHelper.getEntityFromContext(context, this.target);
    	} else {
    		try {
    			return CommandBase.getEntity(context.getWorld().getMinecraftServer(), context.getWorld().getMinecraftServer(), selector);
    		} catch (CommandException e) {
    			// Swallow exception here, since the selector couldn't find a target means there is no target
    			return null;
    		}
    	}
    }

    public static class Serializer extends LootFunction.Serializer<FunctionReplaceNBT> {
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "replace_nbt"), FunctionReplaceNBT.class);
        }

        public void serialize(JsonObject json, FunctionReplaceNBT value, JsonSerializationContext context) {
        	json.add("replace", new JsonPrimitive(value.toReplace));
        	
        	if (value.valueRange != null) {
        		json.add("range", context.serialize(value.valueRange));
        	} else if (value.valueList != null) {
        		LootHelper.serializeStringArray(value.valueList, json, "list");
        	} else if (value.infoType == EntityInfoType.NAME) {
        		if (value.target == null) {
        			json.addProperty("name", value.selector);
        		} else {
        			LootHelper.serializeExtendedEntityTarget(json, "name", value.target);
        		}
        	} else if (value.infoType == EntityInfoType.UUID) {
        		if (value.target == null) {
        			json.addProperty("uuid", value.selector);
        		} else {
        			LootHelper.serializeExtendedEntityTarget(json, "uuid", value.target);
        		}
        	} else if (value.infoType == EntityInfoType.UUID_LEAST) {
        		if (value.target == null) {
        			json.addProperty("uuid", value.selector);
        		} else {
        			LootHelper.serializeExtendedEntityTarget(json, "uuid_least", value.target);
        		}
        	} else if (value.infoType == EntityInfoType.UUID_MOST) {
        		if (value.target == null) {
        			json.addProperty("uuid", value.selector);
        		} else {
        			LootHelper.serializeExtendedEntityTarget(json, "uuid_most", value.target);
        		}
        	} else if (value.scoreboardName != null && value.infoType == EntityInfoType.SCOREBOARD) {
        		json.addProperty("score", value.scoreboardName);
        		if (value.target == null) {
        			json.addProperty("target", value.selector);
        		} else {
        			LootHelper.serializeExtendedEntityTarget(json, "target", value.target);
        		}
        	}
        }

        public FunctionReplaceNBT deserialize(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            String name = JsonUtils.getString(json, "replace");
            String[] values = null;
            RandomValueRange range = null;
            String selector = null;
            ExtendedEntityTarget target = null;
            String scoreboardName = null;
            EntityInfoType infoType = null;
            
            if (json.has("range")) { 
            	range = (RandomValueRange)JsonUtils.deserializeClass(json, "range", context, RandomValueRange.class);
            } else if (json.has("list")) {
            	values = LootHelper.deserializeStringArray(json, "list");
            } else if (json.has("name")){
            	infoType = EntityInfoType.NAME;
            	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "name");
            	if (target == null) {
            		selector = JsonUtils.getString(json, "name");
            	}
            } else if (json.has("uuid")){
            	infoType = EntityInfoType.UUID;
            	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "uuid");
            	if (target == null) {
            		selector = JsonUtils.getString(json, "uuid");
            	}
	        } else if (json.has("uuid_least")){
            	infoType = EntityInfoType.UUID_LEAST;
            	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "uuid");
            	if (target == null) {
            		selector = JsonUtils.getString(json, "uuid");
            	}
	        } else if (json.has("uuid_most")){
            	infoType = EntityInfoType.UUID_MOST;
            	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "uuid");
            	if (target == null) {
            		selector = JsonUtils.getString(json, "uuid");
            	}
	        } else if (json.has("score")){
            	infoType = EntityInfoType.SCOREBOARD;
            	scoreboardName = JsonUtils.getString(json, "score");
            	target = LootHelper.deserializeExtendedEntityTargetOrNull(json, "target");
            	if (target == null) {
            		selector = JsonUtils.getString(json, "target");
            	}
	        } else {
	        	throw new JsonSyntaxException("Valid replacement type not found for " + name);
	        }
        	
            return new FunctionReplaceNBT(conditions, name, values, range, selector, target, scoreboardName, infoType);
        }
    }
    
    private enum EntityInfoType {
    	NAME,
    	UUID,
    	UUID_LEAST,
    	UUID_MOST,
    	SCOREBOARD
    }

}
