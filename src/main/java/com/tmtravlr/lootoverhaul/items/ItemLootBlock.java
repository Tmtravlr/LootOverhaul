package com.tmtravlr.lootoverhaul.items;

import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
 * Item for loot tables which places a block
 * 
 * Example Usage: Dropping a fire block
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_block",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Block:\"minecraft:fire\"}"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Dropping a purple wool block
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_block",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Block:\"minecraft:wool\", State:{color:\"purple\"}}"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Dropping a chest with dungeon chest loot
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_block",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Block:\"minecraft:chest\", BlockEntityTag:{LootTable:\"minecraft:chests/simple_dungeon\"}}"
 *  		}
 *  	]
 *  }
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2016
 */
public class ItemLootBlock extends ItemLoot {
	
	public static final Item INSTANCE = new ItemLootBlock().setUnlocalizedName("loot_block").setRegistryName("loot_block");

	@Override
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {
		BlockPos pos = new BlockPos(position);
    	ResourceLocation blockLocation = new ResourceLocation(item.getTagCompound().getString("Block"));
    	boolean noUpdate = item.getTagCompound().getBoolean("NoUpdate");
    	NBTTagCompound tileTag = item.getTagCompound().hasKey("BlockEntityTag", 10) ? item.getTagCompound().getCompoundTag("BlockEntityTag") : null;
    	
    	Block block = Block.REGISTRY.getObject(blockLocation);
    	IBlockState state = block.getDefaultState();
    	
    	if (item.getTagCompound().hasKey("Meta")) {
    		state = block.getStateFromMeta(item.getTagCompound().getInteger("Meta"));
    	} else if (item.getTagCompound().hasKey("State")) {
    		state = LootHelper.getStateFromNBT(block, item.getTagCompound().getCompoundTag("State"));
    	}
    	
    	if(block != null && !(block == Blocks.AIR && !blockLocation.toString().equals("minecraft:air"))) {
    		if (world.setBlockState(pos, state, 2)) {
	    		if (tileTag != null) {
	                TileEntity tileentity = world.getTileEntity(pos);
	
	                if (tileentity != null) {
	                	tileTag.setInteger("x", pos.getX());
	                	tileTag.setInteger("y", pos.getY());
	                	tileTag.setInteger("z", pos.getZ());
	                    tileentity.readFromNBT(tileTag);
	                }
	            }
	    		
	    		if (!noUpdate) {
	    			world.notifyNeighborsRespectDebug(pos, block, false);
	    		}
    		}
    	}
	}

	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        String blockName = (stack.hasTagCompound() && stack.getTagCompound().hasKey("Block", 8)) ? stack.getTagCompound().getString("Block") : null;

        if (blockName != null) {
        	Block block = Block.REGISTRY.getObject(new ResourceLocation(blockName));
        	
        	if (block != null) {
        		Item item = Item.getItemFromBlock(block);
        		
        		if (item != null) {
        			int data = stack.getTagCompound().getInteger("Meta");
        			name += (new ItemStack(item, 1, data)).getDisplayName();
        		}
        		else {
        	
        			name += I18n.translateToLocal(block.getUnlocalizedName() + ".name");
        		}
        	}
        }

        return name;
    }

}
