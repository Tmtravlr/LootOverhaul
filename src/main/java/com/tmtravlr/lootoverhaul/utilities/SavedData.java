package com.tmtravlr.lootoverhaul.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Handles various saved data in the mod (saved with the world, and with entities).
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class SavedData extends WorldSavedData {
	
	private NBTTagCompound globalVars = new NBTTagCompound();
	private Map<ItemStack, Integer> delayedLoot = new HashMap<>();
	
	public SavedData(String id) {
		super(id);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		globalVars = nbt.getCompoundTag("GlobalVariables");
		
		NBTTagList delayedLootList = nbt.getTagList("DelayedLoot", 10);
		delayedLoot.clear();
		delayedLootList.forEach(tagBase -> {
			if (tagBase instanceof NBTTagCompound) {
				NBTTagCompound tag = (NBTTagCompound) tagBase;
				ItemStack stack = new ItemStack(tag.getCompoundTag("ItemStack"));
				int delay = tag.getInteger("Delay");
				delayedLoot.put(stack, delay);
			}
		});
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("GlobalVariables", globalVars);
		
		NBTTagList delayedLootList = new NBTTagList();
		delayedLoot.forEach((stack, delay) -> {
			NBTTagCompound delayTag = new NBTTagCompound();
			delayTag.setInteger("Delay", delay);
			delayTag.setTag("ItemStack", stack.serializeNBT());
			delayedLootList.appendTag(delayTag);
		});
		nbt.setTag("DelayedLoot", delayedLootList);
		
		return nbt;
	}
	
	public void setGlobalVar(String name, StoredVar var) {
		globalVars.setTag(name, var.toNBT());
		this.markDirty();
	}
	
	@Nullable
	public StoredVar getGlobalVar(String name) {
		NBTBase tag = globalVars.getTag(name);
		
		return getVarFromTag(tag);
	}
	
	/**
	 * Returns the global variable for the given name and type, or a default if none exists.
	 */
	public StoredVar getGlobalVarByType(String name, String type) {
		NBTBase tag = globalVars.getTag(name);
		
		return getVarByTypeOrDefault(tag, name, type);
	}
	
	public String[] getGlobalVarNames() {
		return globalVars.getKeySet().toArray(new String[0]);
	}
	
	public void setLootDelay(ItemStack stack, int delay) {
		this.delayedLoot.put(stack, delay);
		this.markDirty();
	}
	
	public List<ItemStack> decrementLootDelays() {
		ArrayList<ItemStack> expiredDelays = new ArrayList<>();
		Iterator<Entry<ItemStack, Integer>> iterator = this.delayedLoot.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<ItemStack, Integer> entry = iterator.next();
			
			entry.setValue(entry.getValue() - 1);
			
			if (entry.getValue() <= 0) {
				expiredDelays.add(entry.getKey());
				iterator.remove();
			}
			
			this.markDirty();
		}
		return expiredDelays;
	}
	
	public static SavedData getSavedData(World world) {
		SavedData data = ((SavedData) world.getPerWorldStorage().getOrLoadData(SavedData.class, LootOverhaul.MOD_ID));
		
		if(data == null) {
			data = new SavedData(LootOverhaul.MOD_ID);
			world.getPerWorldStorage().setData(LootOverhaul.MOD_ID, data);
		}
		
		return data;
	}
	
	public static void setEntityVar(Entity entity, String name, StoredVar var) {
		NBTTagCompound entityVars = getEntityVarsTag(entity);
		
		entityVars.setTag(name, var.toNBT());
		
		if(entity instanceof EntityPlayer) {
			NBTTagCompound persisted = entity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			persisted.setTag("EntityVariables", entityVars);
			entity.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
		}
		else {
			entity.getEntityData().setTag("EntityVariables", entityVars);
		}
	}
	
	@Nullable
	public static StoredVar getEntityVar(Entity entity, String name) {
		NBTTagCompound entityVars = getEntityVarsTag(entity);
		NBTBase tag = entityVars.getTag(name);
		
		return getVarFromTag(tag);
	}
	
	/**
	 * Returns the entity variable for the given name and type, or a default if none exists.
	 */
	public static StoredVar getEntityVar(Entity entity, String name, String type) {
		NBTTagCompound entityVars = getEntityVarsTag(entity);
		NBTBase tag = entityVars.getTag(name);
		
		return getVarByTypeOrDefault(tag, name, type);
	}
	
	public static String[] getEntityVarNames(Entity entity) {
		NBTTagCompound entityVars = getEntityVarsTag(entity);
		
		return entityVars.getKeySet().toArray(new String[0]);
	}
	
	public static NBTTagCompound getEntityVarsTag(Entity entity) {
		if(entity instanceof EntityPlayer) {
			return entity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag("EntityVariables");
		}
		else {
			return entity.getEntityData().getCompoundTag("EntityVariables");
		}
	}
	
	@Nullable
	public static StoredVar getVarFromTag(NBTBase tag) {
		if(tag instanceof NBTTagString) {
			return new StoredVarString((NBTTagString)tag);
		}
		else if(tag instanceof NBTTagByte) {
			return new StoredVarBoolean((NBTTagByte)tag);
		}
		else if(tag instanceof NBTTagInt) {
			return new StoredVarInt((NBTTagInt)tag);
		}
		else if(tag instanceof NBTTagFloat) {
			return new StoredVarFloat((NBTTagFloat)tag);
		}
		
		return null;
	}
	
	public static StoredVar getVarByTypeOrDefault(NBTBase tag, String name, String type) {
		if(type.equalsIgnoreCase("string")) {
			if(tag instanceof NBTTagString) {
				return getVarFromTag(tag);
			}
			return new StoredVarString("");
		}
		if(type.equalsIgnoreCase("integer")) {
			if(tag instanceof NBTTagInt) {
				return getVarFromTag(tag);
			}
			return new StoredVarInt(0);
		}
		if(type.equalsIgnoreCase("float")) {
			if(tag instanceof NBTTagFloat) {
				return getVarFromTag(tag);
			}
			return new StoredVarFloat(0f);
		}
		if(tag instanceof NBTTagByte) {
			return getVarFromTag(tag);
		}
		
		return new StoredVarBoolean(false);		
	}
	
	/**
	 * Represents a variable that can be stored with the world save data and on an entity.
	 * These variables are useful; they can be manipulated and checked with the loot system.
	 * @author Rebeca Rey (Tmtravlr)
	 * @since November 2016
	 */
	public static abstract class StoredVar {
		
		//Returns the nbt tag version of the variable
		public abstract NBTBase toNBT();
		
		//Returns the string representing this variable's type
		public abstract String getType();
		
		//'Inverts' the variable
		public void invert() {}
		
		//Adds another variable to it. Returns false if not possible.
		public boolean add(StoredVar other) {
			return false;
		}

		//Multiplies another variable with it. Returns false if not possible.
		public boolean multiply(StoredVar other) {
			return false;
		}

		//Divides another variable with it (this/other). Returns false if not possible.
		public boolean divide(StoredVar other) {
			return false;
		}
		
		//Modulates it using the other variable. Returns false if not possible.
		public boolean mod(StoredVar other) {
			return false;
		}
		
		//Returns true if equal to the other variable, and false if not possible.
		public boolean equals(StoredVar other) {
			return false;
		}
		
		//Returns true if equal to the other variable, and false if not possible.
		public boolean lessThan(StoredVar other) {
			return false;
		}
		
		//Returns true if equal to the other variable, and false if not possible.
		public boolean greaterThan(StoredVar other) {
			return false;
		}
		
		//Tests this variable against the other variable using this operation, if possible.
		//Returns true if the test passes, and false if not possible or it fails.
		public boolean test(String operation, StoredVar other) {
			boolean equal = this.equals(other);
			
			if(operation.equals("=")) {
				return equal;
			}
			else if(operation.equals("!=")) {
				return !equal;
			}
			
			if(operation.contains("<")) {
				boolean less = lessThan(other);
				
				if(operation.equals("<")) {
					return less;
				}
				else if(operation.equals("<=")) {
					return less || equal;
				}
			}
			
			if(operation.contains(">")) {
				boolean greater = greaterThan(other);
				
				if(operation.equals(">")) {
					return greater;
				}
				else if(operation.equals(">=")) {
					return greater || equal;
				}
			}
			
			return false;
		}
	}
	
	public static class StoredVarString extends StoredVar {
		public String value;
		
		public StoredVarString(String valueToSet) {
			value = valueToSet;
		}
		
		public StoredVarString(NBTTagString tag) {
			this(tag.getString());
		}
		
		@Override
		public void invert() {
			value = new StringBuilder(value).reverse().toString();
		}
		
		@Override
		public boolean add(StoredVar other) {
			value = value + other;
			return true;
		}
		
		@Override
		public boolean equals(StoredVar other) {
			return this.value.equals(other.toString());
		}
		
		@Override
		public boolean lessThan(StoredVar other) {
			return this.value.length() < other.toString().length();
		}
		
		@Override
		public boolean greaterThan(StoredVar other) {
			return this.value.length() > other.toString().length();
		}
		
		@Override
		public String toString() {
			return value;
		}

		@Override
		public NBTBase toNBT() {
			return new NBTTagString(value);
		}
		
		@Override
		public String getType() {
			return "string";
		}
	}
	
	public static class StoredVarBoolean extends StoredVar {
		public boolean value;
		
		public StoredVarBoolean(boolean valueToSet) {
			value = valueToSet;
		}
		
		public StoredVarBoolean(NBTTagByte tag) {
			this(tag.getByte() != 0);
		}
		
		@Override
		public void invert() {
			value = !value;
		}
		
		@Override
		public boolean equals(StoredVar other) {
			if(other instanceof StoredVarBoolean) {
				return this.value == ((StoredVarBoolean)other).value;
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "" + value;
		}

		@Override
		public NBTBase toNBT() {
			return new NBTTagByte((byte) (value ? 1 : 0));
		}
		
		@Override
		public String getType() {
			return "boolean";
		}
	}
	
	public static class StoredVarInt extends StoredVar {
		public int value;
		
		public StoredVarInt(int valueToSet) {
			value = valueToSet;
		}
		
		public StoredVarInt(NBTTagInt tag) {
			this(tag.getInt());
		}
		
		@Override
		public void invert() {
			value = -value;
		}
		
		@Override
		public boolean add(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value + ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = MathHelper.floor(value + ((StoredVarFloat)other).value);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean multiply(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value * ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = MathHelper.floor(value * ((StoredVarFloat)other).value);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean divide(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value / ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = MathHelper.floor(value / ((StoredVarFloat)other).value);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean mod(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value % ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = MathHelper.floor(value % ((StoredVarFloat)other).value);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean equals(StoredVar other) {
			if(other instanceof StoredVarBoolean) {
				return ((StoredVarBoolean)other).value ? this.value == 1 : this.value == 0;
			}
			else if(other instanceof StoredVarInt) {
				return this.value == ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value == ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		@Override
		public boolean lessThan(StoredVar other) {
			if(other instanceof StoredVarInt) {
				return this.value < ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value < ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		@Override
		public boolean greaterThan(StoredVar other) {
			if(other instanceof StoredVarInt) {
				return this.value > ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value > ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "" + value;
		}

		@Override
		public NBTBase toNBT() {
			return new NBTTagInt(value);
		}
		
		@Override
		public String getType() {
			return "integer";
		}
	}
	
	public static class StoredVarFloat extends StoredVar {
		public float value;
		
		public StoredVarFloat(float valueToSet) {
			value = valueToSet;
		}
		
		public StoredVarFloat(NBTTagFloat tag) {
			this(tag.getFloat());
		}
		
		@Override
		public void invert() {
			value = -value;
		}
		
		@Override
		public boolean add(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value + ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = value + ((StoredVarFloat)other).value;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean multiply(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value * ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = value * ((StoredVarFloat)other).value;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean divide(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value / ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = value / ((StoredVarFloat)other).value;
				return true;
			}
			return false;
		}
		
		public boolean mod(StoredVar other) {
			if(other instanceof StoredVarInt) {
				value = value % ((StoredVarInt)other).value;
				return true;
			}
			if(other instanceof StoredVarFloat) {
				value = value % ((StoredVarFloat)other).value;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean equals(StoredVar other) {
			if(other instanceof StoredVarBoolean) {
				return ((StoredVarBoolean)other).value ? this.value == 1.0f : this.value == 0.0f;
			}
			else if(other instanceof StoredVarInt) {
				return this.value == ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value == ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		@Override
		public boolean lessThan(StoredVar other) {
			if(other instanceof StoredVarInt) {
				return this.value < ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value < ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		@Override
		public boolean greaterThan(StoredVar other) {
			if(other instanceof StoredVarInt) {
				return this.value > ((StoredVarInt)other).value;
			}
			else if(other instanceof StoredVarFloat) {
				return this.value > ((StoredVarFloat)other).value;
			}
			return false;
		}
		
		public String toString() {
			return "" + value;
		}

		@Override
		public NBTBase toNBT() {
			return new NBTTagFloat(value);
		}
		
		@Override
		public String getType() {
			return "float";
		}
	}
	
	

}
