package com.tmtravlr.lootoverhaul.loot;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtended.ExtendedEntityTarget;
import com.tmtravlr.lootoverhaul.loot.conditions.*;
import com.tmtravlr.lootoverhaul.loot.functions.*;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

/**
 * Several useful methods used by loot conditions and functions
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2016
 */
public class LootHelper {
	
	public static final ResourceLocation ALL_ENTTIIES = new ResourceLocation("entities/all");
	
	//Load loot stuff
	
	public static void loadConditions() {
		LootConditionManager.registerCondition(new ConditionNot.Serializer());
    	LootConditionManager.registerCondition(new ConditionOr.Serializer());
    	LootConditionManager.registerCondition(new ConditionDimension.Serializer());
    	LootConditionManager.registerCondition(new ConditionDimensionHasSky.Serializer());
    	LootConditionManager.registerCondition(new ConditionDimensionHeight.Serializer());
    	LootConditionManager.registerCondition(new ConditionDimensionWaterVaporize.Serializer());
    	LootConditionManager.registerCondition(new ConditionBiome.Serializer());
    	LootConditionManager.registerCondition(new ConditionBiomeType.Serializer());
    	LootConditionManager.registerCondition(new ConditionBiomeTemp.Serializer());
    	LootConditionManager.registerCondition(new ConditionBiomeHeight.Serializer());
    	LootConditionManager.registerCondition(new ConditionBiomeHumidity.Serializer());
    	LootConditionManager.registerCondition(new ConditionWeather.Serializer());
    	LootConditionManager.registerCondition(new ConditionSeeSky.Serializer());
    	LootConditionManager.registerCondition(new ConditionLightLevel.Serializer());
    	LootConditionManager.registerCondition(new ConditionMoon.Serializer());
    	LootConditionManager.registerCondition(new ConditionDifficulty.Serializer());
    	LootConditionManager.registerCondition(new ConditionEntityType.Serializer());
    	LootConditionManager.registerCondition(new ConditionNBTMatches.Serializer());
    	LootConditionManager.registerCondition(new ConditionCommand.Serializer());
    	LootConditionManager.registerCondition(new ConditionGlobalVar.Serializer());
    	LootConditionManager.registerCondition(new ConditionEntityVar.Serializer());
    	LootConditionManager.registerCondition(new ConditionNearBlock.Serializer());
    	LootConditionManager.registerCondition(new ConditionBlockState.Serializer());
    	LootConditionManager.registerCondition(new ConditionSilkTouch.Serializer());
    	LootConditionManager.registerCondition(new ConditionToolType.Serializer());
    	LootConditionManager.registerCondition(new ConditionRandomChanceWithFortune.Serializer());
	}
	
	public static void loadFuntions() {
		LootFunctionManager.registerFunction(new FunctionReplaceNBT.Serializer());
		LootFunctionManager.registerFunction(new FunctionFortuneEnchant.Serializer());
		LootFunctionManager.registerFunction(new FunctionDelay.Serializer());
		LootFunctionManager.registerFunction(new FunctionPosition.Serializer());
		LootFunctionManager.registerFunction(new FunctionOffset.Serializer());
		LootFunctionManager.registerFunction(new FunctionCustomPotionEffects.Serializer());
		LootFunctionManager.registerFunction(new FunctionEnchantments.Serializer());
		LootFunctionManager.registerFunction(new FunctionGroup.Serializer());
	}
	
	//Helper methods and such for loot
	
	/**
	 * Retrieves the scoreboard score for the given entity
	 */
	public static int getScoreboardScore(Entity entity, String scoreboardName) {
		int score = 0;
		
		if (entity.getServer() == null) {
			return score;
		}
		
		Scoreboard scoreboard = entity.getServer().getWorld(0).getScoreboard();
        ScoreObjective scoreObjective = scoreboard.getObjective(scoreboardName);
        
        if (scoreObjective == null) {
            return score;
        }
        
        String entityName;
        
        if (entity instanceof EntityPlayer) {
        	entityName = ((EntityPlayer)entity).getName();
        } else {
        	entityName = entity.getCachedUniqueIdString();
        }
        
        if(scoreboard.entityHasObjective(entityName, scoreObjective)) {
			score = scoreboard.getOrCreateScore(entityName, scoreObjective).getScorePoints();
		}
        
        return score;
	}
	
	/**
	 * Takes an nbt tag with state information (like color:"red"), and converts it into a block state for the given block.
	 */
	public static IBlockState getStateFromNBT(Block block, NBTTagCompound stateTag) {
		IBlockState state = block.getDefaultState();
		BlockStateContainer blockstatecontainer = block.getBlockState();
		
		if (stateTag == null || stateTag.hasNoTags()) {
			return state;
		}
		
		for (String key : stateTag.getKeySet()) {
			
			NBTBase value = stateTag.getTag(key);
			IProperty<? extends Comparable> property = blockstatecontainer.getProperty(key);
			
			if (property == null) {
                continue;
            }
			
			Comparable<? extends Comparable> comparable = null;
			
			switch(value.getId()) {
			case 1:
				comparable = ((NBTTagByte)value).getByte();
				break;
			case 2:
				comparable = ((NBTTagShort)value).getShort();
				break;
            case 3:
            	comparable = ((NBTTagInt)value).getInt();
                break;
            case 4:
            	comparable = ((NBTTagLong)value).getLong();
                break;
            case 5:
            	comparable = ((NBTTagFloat)value).getFloat();
                break;
            case 6:
            	comparable = ((NBTTagDouble)value).getDouble();
                break;
            case 8:
            	comparable = property.parseValue(((NBTTagString)value).getString()).orNull();
                break;
			}
			
			if (comparable == null) {
				continue;
			}
			
			try {
				state = getBlockState(state, property, comparable);
			} catch (IllegalArgumentException e) {
				LootOverhaul.logger.warn("Tried to parse an invalid block state '" + key + ": " + comparable + "'. Ignoring.", e);
			}
			
		}
		
		return state;
	}

    private static <T extends Comparable<T>> IBlockState getBlockState(IBlockState state, IProperty<T> property, Comparable<?> comparable) {
        return state.withProperty(property, (T)comparable);
    }

	/**
	 * Returns the position of the loot context.
	 * @param context LootContext to find the position for
	 * @return Position of the loot context
	 */
    @Nullable
	public static BlockPos getPosFromContext(LootContext context) {
		BlockPos pos = null;
		
		if (context instanceof LootContextExtended) {
			pos = ((LootContextExtended)context).getPosition();
		} else {
			if(context.getLootedEntity() != null) {
				pos = context.getLootedEntity().getPosition();
			} else if (context.getKiller() != null) {
				pos = context.getKiller().getPosition();
			} else if (context.getKillerPlayer() != null) {
				pos = context.getKillerPlayer().getPosition();
			}
		}
		
		return pos;
	}

	/**
	 * Returns the entity the entity target is targeting.
	 * @param context LootContext to find the position for
	 * @param target Extended entity target, which has the vanilla EntityTarget values, plus a few extra
	 * @return Position of the loot context
	 */
    @Nullable
	public static Entity getEntityFromContext(LootContext context, ExtendedEntityTarget target) {
		Entity entity = null;
		
		if (context instanceof LootContextExtended) {
			entity = ((LootContextExtended)context).getEntityExtended(target);
		} else {
			if (target.getEntityTargets().length > 0) {
				for (EntityTarget oldTarget : target.getEntityTargets()) {
					entity = context.getEntity(oldTarget);
					if (entity != null) {
						break;
					}
				}
			}
		}
		
		return entity;
	}

	/**
	 * Returns the fortune level of the looter/breaker, if possible.
	 * @param context LootContext to find the fortune level for
	 * @return Fortune level of the loot context
	 */
	public static int getFortuneModifierFromContext(LootContext context) {
		int fortune = 0;
		
		if (context instanceof LootContextExtended) {
			fortune = ((LootContextExtended)context).getFortuneModifier();
		} else {
			Entity looter = getEntityFromContext(context, ExtendedEntityTarget.LOOTER);
			
			if (looter instanceof EntityLivingBase) {
				fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, ((EntityLivingBase)looter).getHeldItemMainhand());
			}
		}
		
		return fortune;
	}

	/**
	 * Returns true if the looter/breaker is using silk touch.
	 * @param context LootContext to find the fortune level for
	 * @return Fortune level of the loot context
	 */
	public static boolean getHasSilkTouchFromContext(LootContext context) {
		boolean silkTouch = false;
		
		if (context instanceof LootContextExtended) {
			silkTouch = ((LootContextExtended)context).isSilkTouch();
		} else {
			Entity looter = getEntityFromContext(context, ExtendedEntityTarget.LOOTER);
			
			if (looter instanceof EntityLivingBase) {
				silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, ((EntityLivingBase)looter).getHeldItemMainhand()) > 0;
			}
		}
		
		return silkTouch;
	}
	
	public static class RangeFloat {
		public Float min;
		public Float max;
		
		public RangeFloat(Float minToSet, Float maxToSet) {
			this.min = minToSet;
			this.max = maxToSet;
		}
		
		public boolean isInRange(Float toTest) {
			if (this.min != null && toTest < this.min) {
				return false;
			}
			if (this.max != null && toTest > this.max) {
				return false;
			}
			
			return true;
		}
		
		public void serialize(JsonObject json, String name) {
			if (this.min != null || this.max != null) {
	    		JsonObject innerJson = new JsonObject();
	    		if (this.min != null) {
	    			innerJson.addProperty("min", this.min);
	    		}
	    		if (this.max != null) {
	    			innerJson.addProperty("max", this.max);
	    		}
	    		json.add(name, innerJson);
	    	}
		}
		
		public String toString() {
			return "{ min: " + this.min + ", max: " + this.max + " }";
		}
	}
	
	/**
	 * Deserializes a pair of min and max values, or just one if only one is present.
	 * @param json Json object to deserialize
	 * @param name Name of the field
	 * @return Tuple containing the min and max floats
	 */
	public static RangeFloat deserializeRangeFloat(JsonObject json, String name) {
		Float min = null;
    	Float max = null;
    	
    	if (json.get(name).isJsonPrimitive() && json.get(name).getAsJsonPrimitive().isNumber()) {
    		min = max = JsonUtils.getFloat(json, name);
    	} else {
			JsonObject innerJson = JsonUtils.getJsonObject(json, name);
			if (!JsonUtils.hasField(innerJson, "min") && !JsonUtils.hasField(innerJson, "max")) {
				throw new JsonSyntaxException(name + ": Expected one or both fields 'min' and 'max'");
			}
			
			if (JsonUtils.hasField(innerJson, "min")) {
				min = JsonUtils.getFloat(innerJson, "min");
			}
			
			if (JsonUtils.hasField(innerJson, "max")) {
				max = JsonUtils.getFloat(innerJson, "max");
			}
    	}
		
		return new RangeFloat(min, max);
	}
	
	/**
	 * Deserializes a string array, which may be one string or a json array of strings.
	 * @param json Json object to deserialize
	 * @param name Name of the field
	 * @return String array deserialized
	 */
	public static String[] deserializeStringArray(JsonObject json, String name) {
		//Just one argument
        if (JsonUtils.isString(json, name)) {
        	return new String[]{JsonUtils.getString(json, name)};
        }
        
        //Array of arguments
    	JsonArray jsonArray = JsonUtils.getJsonArray(json, name);
        String[] array = new String[jsonArray.size()];
        
        for(int i = 0; i < jsonArray.size(); i++) {
        	if (jsonArray.get(i).isJsonPrimitive() && jsonArray.get(i).getAsJsonPrimitive().isString()) {
        		array[i] = jsonArray.get(i).getAsString();
        	} else {
        		throw new JsonSyntaxException("Expected '"+name+"' array element to be a string");
        	}
        }
        
        return array;
	}
	
	/**
	 * Deserializes an int array, which may be one int or a json array of int.
	 * @param json Json object to deserialize
	 * @param name Name of the field
	 * @return Integer array deserialized
	 */
	public static int[] deserializeIntArray(JsonObject json, String name) {
		//Just one argument
        if(JsonUtils.isNumber(json.get(name))) {
        	return new int[]{JsonUtils.getInt(json, name)};
        }
        
        //Array of arguments
    	JsonArray jsonArray = JsonUtils.getJsonArray(json, name);
        int[] array = new int[jsonArray.size()];
        
        for (int i = 0; i < jsonArray.size(); i++) {
        	if (JsonUtils.isNumber(jsonArray.get(i))) {
        		array[i] = jsonArray.get(i).getAsInt();
        	} else {
        		throw new JsonSyntaxException("Expected '"+name+"' array element to be an integer");
        	}
        }
        
        return array;
	}
	
	/**
	 * Serializes a string array to a json object, either to a json array, or a single string if only one element.
	 * @param array String array
	 * @param json Json object to serialize to
	 * @param name Name of the field
	 */
	public static void serializeStringArray(String[] array, JsonObject json, String name) {
		if (array.length == 1) {
    		json.add(name, new JsonPrimitive(array[0]));
    	} else {
    	
            JsonArray commandList = new JsonArray();

            for (String element : array) {
            	commandList.add(new JsonPrimitive(element));
            }

            json.add(name, commandList);
    	}
	}
	
	/**
	 * Serializes an int array to a json object, either to a json array, or a single int if only one element.
	 * @param array Int array
	 * @param json Json object to serialize to
	 * @param name Name of the field
	 */
	public static void serializeIntArray(int[] array, JsonObject json, String name) {
		if (array.length == 1) {
    		json.add(name, new JsonPrimitive(array[0]));
    	} else {
    	
            JsonArray commandList = new JsonArray();

            for (int element : array) {
            	commandList.add(new JsonPrimitive(element));
            }

            json.add(name, commandList);
    	}
	}
	
	
	/**
	 * Deserializes an nbt value with the type called 'type' from a json object
	 * @param json Json object to deserialize
	 * @param name Name of the nbt to deserialize
	 * @return Nbt tag equivalent of the json entry.
	 */
    public static NBTBase deserializeNBT(JsonObject json, String name) {
    	return deserializeNBTArray(json, name)[0];
    }
	
	
	/**
	 * Deserializes an nbt value with the type called 'type' from a json object
	 * @param json Json object to deserialize
	 * @param name Name of the nbt to deserialize
	 * @return Nbt tag equivalent of the json entry.
	 */
    public static NBTBase[] deserializeNBTArray(JsonObject json, String name) {
    	String type = JsonUtils.getString(json, "type");
    	
    	if (JsonUtils.isJsonArray(json, name)) {
    		JsonArray jsonArray = JsonUtils.getJsonArray(json, name);
    		NBTBase[] array = new NBTBase[jsonArray.size()];
            
            for (int i = 0; i < jsonArray.size(); i++) {
    	    	if (type.equalsIgnoreCase("string")) {
    	    		array[i] = new NBTTagString(JsonUtils.getString(json, name));
    	    	} else if (type.equalsIgnoreCase("boolean")) {
    	    		array[i] = new NBTTagByte(JsonUtils.getBoolean(json, name) ? (byte)1 : (byte)0);
    	    	} else if (type.equalsIgnoreCase("integer")) {
    	    		array[i] = new NBTTagInt(JsonUtils.getInt(json, name));
    	    	} else if (type.equalsIgnoreCase("float")) {
    	    		array[i] = new NBTTagFloat(JsonUtils.getFloat(json, name));
    	    	} else {
    	    		throw new JsonSyntaxException(name + ": Unrecognized variable type '" + type + "'. Valid types are 'string', 'boolean', 'integer', and 'float'.");
    	    	}
            }
            
            return array;
    	}
    	else {
	    	if (type.equalsIgnoreCase("string")) {
	    		return new NBTBase[]{new NBTTagString(JsonUtils.getString(json, name))};
	    	} else if (type.equalsIgnoreCase("boolean")) {
	    		return new NBTBase[]{new NBTTagByte(JsonUtils.getBoolean(json, name) ? (byte)1 : (byte)0)};
	    	} else if (type.equalsIgnoreCase("integer")) {
	    		return new NBTBase[]{new NBTTagInt(JsonUtils.getInt(json, name))};
	    	} else if (type.equalsIgnoreCase("float")) {
	    		return new NBTBase[]{new NBTTagFloat(JsonUtils.getFloat(json, name))};
	    	} else {
	    		throw new JsonSyntaxException(name + ": Unrecognized variable type '" + type + "'. Valid types are 'string', 'boolean', 'integer', and 'float'.");
	    	}
    	}
		
    }
    
    /**
     * Serializes an nbt tag to a json object
     * @param json Json object to serialize to
     * @param name Name of the nbt to serialize
     * @param nbt Tag to serialize
     */
    public static void serializeNBT(JsonObject json, String name, NBTBase nbt) {
    	serializeNBTArray(json, name, new NBTBase[]{nbt});
    }
    
    /**
     * Serializes an nbt array to json objects
     * @param json Json object to serialize to
     * @param name Name of the nbt to serialize
     * @param nbtArray Tag to serialize
     */
    public static void serializeNBTArray(JsonObject json, String name, NBTBase[] nbtArray) {
    	String type = "?";
	    
	    if(nbtArray.length == 1) {
    		NBTBase nbt = nbtArray[0];
    		if (nbt instanceof NBTTagString) {
        		json.add(name, new JsonPrimitive(((NBTTagString) nbt).getString()));
            	type = "string";
        	} else if (nbt instanceof NBTTagByte) {
        		json.add(name, new JsonPrimitive(((NBTTagByte) nbt).getByte() == 1 ? true : false));
            	type = "boolean";
        	} else if (nbt instanceof NBTTagInt) {
        		json.add(name, new JsonPrimitive(((NBTTagInt) nbt).getInt()));
            	type = "integer";
        	} else if (nbt instanceof NBTTagFloat) {
        		json.add(name, new JsonPrimitive(((NBTTagFloat) nbt).getFloat()));
            	type = "float";
        	}
    	}
    	else {
	    	JsonArray array = new JsonArray();
	    	
	    	for (NBTBase nbt : nbtArray) {
		    	if (nbt instanceof NBTTagString) {
		    		array.add(new JsonPrimitive(((NBTTagString) nbt).getString()));
		        	type = "string";
		    	} else if (nbt instanceof NBTTagByte) {
		    		array.add(new JsonPrimitive(((NBTTagByte) nbt).getByte() == 1 ? true : false));
		    		type = "boolean";
		    	} else if (nbt instanceof NBTTagInt) {
		    		array.add(new JsonPrimitive(((NBTTagInt) nbt).getInt()));
		    		type = "integer";
		    	} else if (nbt instanceof NBTTagFloat) {
		    		array.add(new JsonPrimitive(((NBTTagFloat) nbt).getFloat()));
		    		type = "float";
		    	}
	    	}
	    	
	    	json.add(name, array);
    	}
	    
	    json.add("type", new JsonPrimitive(type));
    }
    
    public static void serializeExtendedEntityTarget(JsonObject json, String name, ExtendedEntityTarget target) {
    	json.addProperty(name, target.name());
    }
    
    public static ExtendedEntityTarget deserializeExtendedEntityTarget(JsonObject json, String name) {
    	return deserializeExtendedEntityTarget(json, name, null);
    }
    
    public static ExtendedEntityTarget deserializeExtendedEntityTarget(JsonObject json, String name, ExtendedEntityTarget fallback) {
    	if (JsonUtils.isString(json, name)) {
	    	String targetName = JsonUtils.getString(json, name);
	    	return ExtendedEntityTarget.valueOf(targetName.toUpperCase());
    	} else {
    		if (fallback == null) {
    			throw new JsonSyntaxException("Expected entity target for " + name);
    		}
    		return fallback;
    	}
    }
    
    public static ExtendedEntityTarget deserializeExtendedEntityTargetOrNull(JsonObject json, String name) {
    	try {
    		return LootHelper.deserializeExtendedEntityTarget(json, name);
    	} catch (IllegalArgumentException e) {
    		//Swallowing exception here, since it's the simplest way to check if it's a valid enum.
    		return null;
    	}
    }
}
