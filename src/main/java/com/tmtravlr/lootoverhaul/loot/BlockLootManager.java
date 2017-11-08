package com.tmtravlr.lootoverhaul.loot;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
* Loads and maintains block loot.
* 
* @author Tmtravlr (Rebeca Rey)
* @since November 2016
*/
public class BlockLootManager {
	
	public static final ResourceLocation ALL_BLOCKS = new ResourceLocation("minecraft:blocks/all");
	
	private static Random rand = new Random();

	public static ResourceLocation getBlockDropLootTable(World world, BlockPos pos, IBlockState state) {
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if (tileEntity instanceof ITileEntityLootable) {
			return ((ITileEntityLootable)tileEntity).getDropLootTable();
		}
		
		ResourceLocation blockName = state.getBlock().getRegistryName();
    	return new ResourceLocation(blockName.getResourceDomain(), "blocks/" + blockName.getResourcePath());
	}
	
	public static Random getRandom(World world, BlockPos pos, IBlockState state) {
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if (tileEntity instanceof ITileEntityLootable) {
			return ((ITileEntityLootable)tileEntity).getRand();
		}
		
		return rand;
	}
}
