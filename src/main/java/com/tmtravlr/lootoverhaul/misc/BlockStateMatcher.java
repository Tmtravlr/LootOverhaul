package com.tmtravlr.lootoverhaul.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.loot.LootHelper.RangeFloat;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;

public abstract class BlockStateMatcher {

	protected String propertyName;
	
	public static BlockStateMatcher create(String name, String... strings) {
		return new BlockStateMatcherList(name, strings);
	}
	
	public static BlockStateMatcher create(String name, RangeFloat range) {
		return new BlockStateMatcherRange(name, range);
	}
	
	public static BlockStateMatcher[] deserialize(JsonObject jsonParent, String name) {
		JsonObject json = JsonUtils.getJsonObject(jsonParent, name);
		List<BlockStateMatcher> states = new ArrayList<>();
		
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			if (entry.getValue().isJsonObject()) {
				RangeFloat range = LootHelper.deserializeRangeFloat(json, entry.getKey());
				states.add(new BlockStateMatcherRange(entry.getKey(), range));
			} else if (entry.getValue().isJsonPrimitive()) {
				JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
				String string = getStringFromPrimitive(primitive);
				if (string != null) {
					states.add(new BlockStateMatcherList(entry.getKey(), string));
				}
			} else if (entry.getValue().isJsonArray()) {
				JsonArray array = entry.getValue().getAsJsonArray();
				List<String> strings = new ArrayList<>();
				array.forEach(jsonElement -> {
					if (jsonElement.isJsonPrimitive()) {
						JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
						String string = getStringFromPrimitive(primitive);
						if (string != null) {
							strings.add(string);
						}
					} else {
						throw new JsonSyntaxException("Expected '" + entry.getKey() + "' array to only contain json primitives.");
					}
				});
				if (!strings.isEmpty()) {
					states.add(new BlockStateMatcherList(entry.getKey(), strings.toArray(new String[0])));
				}
			}
		}
		
		return states.toArray(new BlockStateMatcher[0]);
	}
	
	@Nullable
	public static String getStringFromPrimitive(JsonPrimitive primitive) {
		String primitiveString = null;
		
		if (primitive.isBoolean()) {
			primitiveString = String.valueOf(primitive.getAsBoolean());
		}
		if (primitive.isString()) {
			primitiveString = primitive.getAsString();
		}
		if (primitive.isNumber()) {
			primitiveString = String.valueOf(primitive.getAsNumber());
		}
		
		return primitiveString;
	}
	
	public static void serialize(BlockStateMatcher[] states, JsonObject jsonParent, String name) {
		JsonObject json = new JsonObject();
		
		for (BlockStateMatcher state : states) {
			
			if (state instanceof BlockStateMatcherRange) {
				BlockStateMatcherRange stateRange = (BlockStateMatcherRange) state;
				stateRange.possibleValueRange.serialize(json, state.propertyName);
			}
			
			if (state instanceof BlockStateMatcherList) {
				BlockStateMatcherList stateList = (BlockStateMatcherList) state;
				if (stateList.possibleValueList.length == 1) {
					json.addProperty(state.propertyName, String.valueOf(stateList.possibleValueList[0]));
				} else {
					JsonArray jsonArray = new JsonArray();
					
					for (String string : stateList.possibleValueList) {
						jsonArray.add(string);
					}
					
					json.add(state.propertyName, jsonArray);
				}
			}
		}
		
		jsonParent.add(name, json);
	}
	
	public abstract boolean matches(IBlockState state);
	
	public static class BlockStateMatcherList extends BlockStateMatcher {
		
		public String[] possibleValueList;
		
		public BlockStateMatcherList(String name, String... comparables) {
			this.propertyName = name;
			this.possibleValueList = comparables;
		}
		
		public boolean matches(IBlockState state) {
			for (IProperty property : state.getPropertyKeys()) {
				Comparable value = state.getProperties().get(property);
				
				if (this.propertyName.equals(property.getName())) {
					for (Comparable possibleValue : possibleValueList) {
						if (possibleValue.equals(property.getName(value))) {
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
	}
	
	public static class BlockStateMatcherRange extends BlockStateMatcher {
		
		public RangeFloat possibleValueRange;
		
		public BlockStateMatcherRange(String name, RangeFloat range) {
			this.propertyName = name;
			this.possibleValueRange = range;
		}
		
		public boolean matches(IBlockState state) {
			for (IProperty property : state.getPropertyKeys()) {
				Comparable value = state.getProperties().get(property);
				if (this.propertyName.equals(property.getName())) {
					if (value instanceof Integer) {
						if (possibleValueRange.isInRange(Float.valueOf((Integer) value))) {
							return true;
						}
					}
					
					if (value instanceof Float) {
						if (possibleValueRange.isInRange((Float) value)) {
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
	}
	
}
