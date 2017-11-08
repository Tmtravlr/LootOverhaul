package com.tmtravlr.lootoverhaul.items;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

/**
 * Item for loot pools that holds extra info about an item
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ItemLootItem extends ItemLoot {

	public static final Item INSTANCE = new ItemLootBlock().setUnlocalizedName("loot_item").setRegistryName("loot_item");

	@Override
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {
		if (!(item.hasTagCompound() && item.getTagCompound().hasKey("ItemStack", 10))) {
			return;
		}
		
		ItemStack storedStack = new ItemStack(item.getTagCompound().getCompoundTag("ItemStack"));
		EntityItem entityItem = new EntityItem(world, position.x, position.y, position.z, storedStack);
		entityItem.setDefaultPickupDelay();
		
		if (item.getTagCompound().hasKey("Motion", 10)) {
			NBTTagCompound motionTag = item.getTagCompound().getCompoundTag("Motion");
			entityItem.motionX = motionTag.getInteger("X");
			entityItem.motionY = motionTag.getInteger("Y");
			entityItem.motionZ = motionTag.getInteger("Z");
		}
		
		world.spawnEntity(entityItem);
	}
	


	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        
    	NBTTagCompound stackTag = (stack.hasTagCompound() && stack.getTagCompound().hasKey("ItemStack", 10)) ? stack.getTagCompound().getCompoundTag("ItemStack") : null;
    	
    	if (stackTag!= null) {
    		ItemStack storedStack = new ItemStack(stackTag);
    		
    		name += storedStack.getDisplayName();
    	}

        return name;
    }
	
}
