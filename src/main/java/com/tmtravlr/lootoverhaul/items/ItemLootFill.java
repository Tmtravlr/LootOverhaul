package com.tmtravlr.lootoverhaul.items;

import java.util.ArrayList;
import java.util.Random;

import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

/**
 * Item for loot tables which fills an area, much like the fill command,
 * but it works if part of the area is out of the world, supports spheres
 * and cylinders as shapes, and takes a list of blocks.
 * 
 * Example Usage: Filling a 10x10x10 area with air
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_fill",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Blocks:[{Block:\"minecraft:air\"}], Size:{X:10, Y:10, Z:10}}"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Filling a sphere with radius 10 with either cobblestone or mossy cobblestone,
 * with twice as much cobblestone as mossy cobblestone.
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_fill",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Blocks:[{Block:\"minecraft:cobblestone\", Weight:2},{Block:\"minecraft:mossy_cobblestone\", Weight:1}], Size:{X:10, Y:10, Z:10}, Shape:\"SPHERE\"}"
 *  		}
 *  	]
 *  }
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ItemLootFill extends ItemLoot {
	
	private static final Random RAND = new Random();
	
	public static final Item INSTANCE = new ItemLootFill().setUnlocalizedName("loot_fill").setRegistryName("loot_fill");

	@Override
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {
		if (!item.hasTagCompound() || !item.getTagCompound().hasKey("Size", 10) || !item.getTagCompound().hasKey("Blocks", 9)) {
			return;
		}
		
		int sizeX = item.getTagCompound().getCompoundTag("Size").getInteger("X");
		int sizeY = item.getTagCompound().getCompoundTag("Size").getInteger("Y");
		int sizeZ = item.getTagCompound().getCompoundTag("Size").getInteger("Z");
		String axis = item.getTagCompound().hasKey("Axis") ? item.getTagCompound().getString("Axis") : "Y";
		
		FillShape fillShape = FillShape.getFillShapeOrDefault(item.getTagCompound().getString("Shape"));
		
		NBTTagList blockWeightTags = item.getTagCompound().getTagList("Blocks", 10);
		ArrayList<WeightedBlockInfo> weightedBlocks = new ArrayList<>();
		int weightTotal = 0;
		
		for (NBTBase nbtBase : blockWeightTags) {
			NBTTagCompound blockWeightTag = (NBTTagCompound) nbtBase;
			
			int weight = blockWeightTag.getInteger("Weight");
			if (weight <= 0) {
				weight = 1;
			}
			
			weightTotal += weight;
			
			weightedBlocks.add(new WeightedBlockInfo(weight, blockWeightTag));
		}
		
		if (weightTotal == 0 || weightedBlocks.isEmpty()) {
			return;
		}
		
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		int startX = MathHelper.floor(position.x);
		int startY = MathHelper.floor(position.y);
		int startZ = MathHelper.floor(position.z);
		double radiusX = (double) sizeX/2;
		double radiusY = (double) sizeY/2;
		double radiusZ = (double) sizeZ/2;
		
		for (int x = startX; x < startX + sizeX; x++) {
			for (int y = startY; y < startY + sizeY; y++) {
				for (int z = startZ; z < startZ + sizeZ; z++) {
					double offsetX = radiusX - x + startX - 0.5;
					double offsetY = radiusY - y + startY - 0.5;
					double offsetZ = radiusZ - z + startZ - 0.5;
					
					if (fillShape == FillShape.HOLLOW) {
						if(!(x == startX || x == startX + sizeX - 1 || y == startY || y == startY + sizeY - 1 || z == startZ || z == startZ + sizeZ - 1))
							continue;
					} else if (fillShape == fillShape.SPHERE) {
						
						if (offsetX*offsetX / (radiusX*radiusX) + offsetY*offsetY / (radiusY*radiusY) + offsetZ*offsetZ / (radiusZ*radiusZ) > 1.0) {
							continue;
						}
					} else if (fillShape == fillShape.CYLINDER) {
						
						if ("X".equalsIgnoreCase(axis) && offsetY*offsetY / (radiusY*radiusY) + offsetZ*offsetZ / (radiusZ*radiusZ) > 1.0) {
							continue;
						}
						
						if ("Y".equalsIgnoreCase(axis) && offsetX*offsetX / (radiusX*radiusX) + offsetZ*offsetZ / (radiusZ*radiusZ) > 1.0) {
							continue;
						}
						
						if ("Z".equalsIgnoreCase(axis) && offsetX*offsetX / (radiusX*radiusX) + offsetY*offsetY / (radiusY*radiusY) > 1.0) {
							continue;
						}
					}
					
					pos.setPos(x, y, z);
					
					if (world.isOutsideBuildHeight(pos)) {
						continue;
					}
					
					if (item.getTagCompound().getBoolean("Keep") && !world.getBlockState(pos).getMaterial().isReplaceable()) {
						continue;
					}
					
					int currentWeight = RAND.nextInt(weightTotal);
					NBTTagCompound blockTag = null;
					
					for (WeightedBlockInfo weightedBlock : weightedBlocks) {
						if (currentWeight <= 0) {
							blockTag = weightedBlock.blockTag;
							break;
						}
						currentWeight -= weightedBlock.weight;
					}
					
					if (blockTag == null) {
						continue;
					}
					
					ResourceLocation blockLocation = new ResourceLocation(blockTag.getString("Block"));
			    	boolean noUpdate = blockTag.getBoolean("NoUpdate");
			    	NBTTagCompound tileTag = blockTag.hasKey("BlockEntityTag", 10) ? blockTag.getCompoundTag("BlockEntityTag") : null;
			    	
			    	Block block = Block.REGISTRY.getObject(blockLocation);
			    	IBlockState state = block.getDefaultState();
			    	
			    	if (blockTag.hasKey("Meta")) {
			    		state = block.getStateFromMeta(blockTag.getInteger("Meta"));
			    	} else if (blockTag.hasKey("State")) {
			    		state = LootHelper.getStateFromNBT(block, blockTag.getCompoundTag("State"));
			    	}
			    	
			    	if (block != null && !(block == Blocks.AIR && !blockLocation.toString().equals("minecraft:air"))) {
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
			}
		}
		
		pos.release();
	}
	


	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack) + ((stack.hasTagCompound() && stack.getTagCompound().hasKey("Shape", 8)) ? stack.getTagCompound().getString("Shape") : "Cube");
    }
	
	private class WeightedBlockInfo {
		public int weight;
		public NBTTagCompound blockTag;
		
		public WeightedBlockInfo(int weight, NBTTagCompound blockTag) {
			this.weight = weight;
			this.blockTag = blockTag;
		}
	}
	
	public enum FillShape {
		CUBE,
		HOLLOW,
		SPHERE,
		CYLINDER;
		
		public static FillShape getFillShapeOrDefault(String name) {
			for (FillShape shape : FillShape.values()) {
				if (name.toUpperCase().equals(shape.toString().toUpperCase())) {
					return shape;
				}
			}
			
			return FillShape.CUBE;
		}
	}

}
