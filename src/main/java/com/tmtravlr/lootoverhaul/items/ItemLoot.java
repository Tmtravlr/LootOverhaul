package com.tmtravlr.lootoverhaul.items;

import java.util.Random;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.misc.SavedData;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Item for loot pools that holds extra info
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public abstract class ItemLoot extends Item {
	
	private static final Random RAND = new Random();
	
	public ItemLoot() {
		this.setMaxStackSize(1);
	}
	
	public static ItemStack createStackFromItem(ItemStack original) {
		NBTTagCompound originalTag = new NBTTagCompound();
		originalTag = original.writeToNBT(originalTag);
		
		ItemStack newLootStack = new ItemStack(ItemLootItem.INSTANCE);
		newLootStack.setTagCompound(new NBTTagCompound());
		newLootStack.getTagCompound().setTag("Item", originalTag);
		
		return newLootStack;
	}
	
	@Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
    	if(!entityItem.world.isRemote) {
			//Sanity checks
	    	if(entityItem == null || entityItem.getItem() == null || entityItem.getItem().getItem() == null || !entityItem.getItem().hasTagCompound()) {
	    		entityItem.setDead();
	    		return true;
	    	}
	    	
	    	this.generateLoot(entityItem.getItem(), entityItem.world, entityItem.getPositionVector());
		}
        
    	entityItem.setDead();
		return true;
    }
	
	@Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
    	if(!world.isRemote) {
			this.generateLoot(stack, world, entity.getPositionVector());
			stack.setCount(0);
		}
    }
	
	public void generateLoot(ItemStack item, World world, Vec3d position) {
		if (!item.hasTagCompound()) {
			return;
		}
		
		if (item.getTagCompound().hasKey("Delay")) {
			int delay = item.getTagCompound().getInteger("Delay");
			item.getTagCompound().removeTag("Delay");
			if (delay > 0) {
				NBTTagCompound positionTag = item.getTagCompound().getCompoundTag("Position");
				
				if (!positionTag.hasKey("X")) {
					positionTag.setDouble("X", position.x);
				}
				if (!positionTag.hasKey("Y")) {
					positionTag.setDouble("Y", position.y);
				}
				if (!positionTag.hasKey("Z")) {
					positionTag.setDouble("Z", position.z);
				}
				
				item.getTagCompound().setTag("Position", positionTag);
				
				SavedData savedData = SavedData.getSavedData(world);
				savedData.setLootDelay(item, delay);
			}
			return;
		}
		
		if (item.getTagCompound().hasKey("Position", 10)) {
			NBTTagCompound positionTag = item.getTagCompound().getCompoundTag("Position");
			if (positionTag.hasKey("Entity")) {
				String entityString = positionTag.getString("Entity");
				Entity entity = null;
				
				try {
					entity = CommandBase.getEntity(world.getMinecraftServer(), world.getMinecraftServer(), entityString);
				} catch (Exception e) {
					LootOverhaul.logger.catching(e);
				}
				
				if (entity != null) {
					position = entity.getPositionVector();
				}
			} else {
				double posX = position.x;
				double posY = position.y;
				double posZ = position.z;
				
				if (positionTag.hasKey("X")) {
					posX = positionTag.getDouble("X");
				}
				if (positionTag.hasKey("Y")) {
					posY = positionTag.getDouble("Y");
				}
				if (positionTag.hasKey("Z")) {
					posZ = positionTag.getDouble("Z");
				}
				
				position = new Vec3d(posX, posY, posZ);
			}
		}
		
		if (item.getTagCompound().hasKey("Offset", 10)) {
			NBTTagCompound positionTag = item.getTagCompound().getCompoundTag("Offset");
			if (positionTag.hasKey("X") || positionTag.hasKey("Y") || positionTag.hasKey("Z")) {
				float offsetX = positionTag.getFloat("X");
				float offsetY = positionTag.getFloat("Y");
				float offsetZ = positionTag.getFloat("Z");
				
				position = position.addVector(offsetX, offsetY, offsetZ);
			}
			if (positionTag.hasKey("Radius")) {
				float offsetRadius = Math.abs(positionTag.getFloat("Radius"));
				
				if (offsetRadius > 0) {
					double angle = RAND.nextDouble()*Math.PI*2;
					double offsetX = Math.cos(angle)*offsetRadius;
					double offsetZ = Math.sin(angle)*offsetRadius;
					
					position = position.addVector(offsetX, 0, offsetZ);
				}
			}
			if (positionTag.hasKey("SurfaceSearch")) {
				int maxSearch = positionTag.getInteger("SurfaceSearch");
				
				BlockPos startPos = new BlockPos(position);
				BlockPos.PooledMutableBlockPos searchPos = BlockPos.PooledMutableBlockPos.retain(startPos);
				
				if (!(world.getBlockState(searchPos.down()).isSideSolid(world, searchPos.down(), EnumFacing.UP) && world.getBlockState(searchPos).getMaterial().isReplaceable())) {
					boolean foundPos = false;
					for (int ySearch = 0; ySearch < maxSearch && !foundPos; ySearch++) {
						for (int yDirection : new int[]{1, -1}) {
							searchPos.setY(startPos.getY() + ySearch*yDirection);
							IBlockState state = world.getBlockState(searchPos);
							if (world.getBlockState(searchPos.down()).isSideSolid(world, searchPos.down(), EnumFacing.UP) && (state.getMaterial().isReplaceable() || state.getCollisionBoundingBox(world, searchPos) == null)) {
								position.addVector(0, ySearch*yDirection, 0);
								foundPos = true;
								break;
							}
						}
					}
				}
				
				searchPos.release();
			}
		}
		
		this.generateSpecificLoot(item, world, position);
	}
	
	protected abstract void generateSpecificLoot(ItemStack item, World world, Vec3d position);
	
}
