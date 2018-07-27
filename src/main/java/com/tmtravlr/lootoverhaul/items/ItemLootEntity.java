package com.tmtravlr.lootoverhaul.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This item will turn into an entity as soon as it enters the world in entity form. 
 * Much of the code was copied and adapted from ItemMonsterPlacer.class.
 * 
 * Example Usage: Dropping a silverfish
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_entity",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{EntityTag:{id:\"minecraft:silverfish\"}}"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Dropping a chest minecart with dungeon loot
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_entity",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{EntityTag:{id:\"minecraft:chest_minecart\", LootTable:\"minecraft:chests/simple_dungeon\"}}"
 *  		}
 *  	]
 *  }
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2016
 */
public class ItemLootEntity extends ItemLoot {

	public static final Item INSTANCE = new ItemLootEntity().setUnlocalizedName("loot_entity").setRegistryName("loot_entity");

	@Override
	protected void generateSpecificLoot(ItemStack stack, World world, Vec3d position) {

		if (!stack.hasTagCompound() || stack.getSubCompound("EntityTag") == null || !stack.getSubCompound("EntityTag").hasKey("id")) {
			LootOverhaul.logger.warn("Skipping invalid loot entity: " + stack.getTagCompound());
			return;
		}
    	
        Entity entity = AnvilChunkLoader.readWorldEntityPos(stack.getSubCompound("EntityTag"), world, position.x, position.y, position.z, true);
        
		if (entity == null) {
			LootOverhaul.logger.warn("Unable to spawn loot entity: " + stack.getSubCompound("EntityTag"));
			return;
		}
        
        if (entity instanceof EntityLiving) {
			((EntityLiving)entity).playLivingSound();
		}

        if (stack.getTagCompound().hasKey("Motion", 10)) {
			NBTTagCompound motionTag = stack.getTagCompound().getCompoundTag("Motion");
			entity.motionX = motionTag.getInteger("X");
			entity.motionY = motionTag.getInteger("Y");
			entity.motionZ = motionTag.getInteger("Z");
		}
	}

	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);

        if (stack.getSubCompound("EntityTag") != null) {
            name += " - " + stack.getSubCompound("EntityTag").getString("id");
        }

        return name;
    }

}
