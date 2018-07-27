package com.tmtravlr.lootoverhaul.items;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

/**
 * Item for loot pools that will generate a structure
 * 
 * Example Usage: Generating a structure called minecraft:little_tree
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_structure",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Name:\"minecraft:little_tree\"}"
 *  		}
 *  	]
 *  }
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2016
 */
public class ItemLootStructure extends ItemLoot {

	public static final Item INSTANCE = new ItemLootStructure().setUnlocalizedName("loot_structure").setRegistryName("loot_structure");

	@Override
	protected void generateSpecificLoot(ItemStack stack, World world, Vec3d position) {
		if(world instanceof WorldServer) {
			if(!stack.hasTagCompound()) {
				return;
			}

			String name = stack.getTagCompound().getString("Name");
			String mirrorType = stack.getTagCompound().getString("Mirror");
			int rotationType = stack.getTagCompound().getInteger("Rotation");
			boolean ignoreEntities = stack.getTagCompound().getBoolean("IgnoreEntities");
			float integrity = stack.getTagCompound().hasKey("Integrity") ? stack.getTagCompound().getFloat("Integrity") : 1.0f;
			long seed = stack.getTagCompound().hasKey("Seed") ? stack.getTagCompound().getLong("Seed") : 0L;

			Mirror mirror = Mirror.NONE;

			if(mirrorType != null && !mirrorType.isEmpty()) {
				if(mirrorType.equals("left-right")) {
					mirror = Mirror.LEFT_RIGHT;
				}
				else if(mirrorType.equals("front-back")) {
					mirror = Mirror.FRONT_BACK;
				}
				else if(!mirrorType.equals("none")) {
					LootOverhaul.logger.warn("Loot Structure: Mirror type '" + mirrorType + "' not recognized. Valid options are 'none', 'left-right', and 'front-back'."); 
				}
			}

			Rotation rotation = Rotation.NONE;

			if(rotationType == 90) {
				rotation = Rotation.CLOCKWISE_90;
			}
			else if(rotationType == 180) {
				rotation = Rotation.CLOCKWISE_180;
			}
			else if(rotationType == 270) {
				rotation = Rotation.COUNTERCLOCKWISE_90;
			}
			else if(rotationType != 0) {
				LootOverhaul.logger.warn("Loot Structure: Rotation " + rotationType + " not accetped. Valid options (in degrees clockwise) are 0, 90, 180, and 270."); 
			}

			BlockPos startPos = new BlockPos(position);
			WorldServer worldserver = (WorldServer)world;
			MinecraftServer minecraftserver = world.getMinecraftServer();
			TemplateManager templatemanager = worldserver.getStructureTemplateManager();
			Template template = templatemanager.get(minecraftserver, new ResourceLocation(name));

			if (template != null)  {
				PlacementSettings placementsettings = (new PlacementSettings()).setMirror(mirror).setRotation(rotation).setIgnoreEntities(ignoreEntities).setChunk((ChunkPos)null).setReplacedBlock((Block)null).setIgnoreStructureBlock(false);

				if (integrity < 1.0F)
				{
					placementsettings.setIntegrity(MathHelper.clamp(integrity, 0.0F, 1.0F)).setSeed(seed);
				}

				template.addBlocksToWorldChunk(worldserver, startPos, placementsettings);
			}
		}
	}

	public String getItemStackDisplayName(ItemStack stack) {
		String name = super.getItemStackDisplayName(stack);
		String structureName = (stack.hasTagCompound() && stack.getTagCompound().hasKey("Name", 8)) ? stack.getTagCompound().getString("Name").split(" ")[0] : null;

		if (structureName != null) {
			name = name + " " + structureName;
		}

		return name;
	}


}
