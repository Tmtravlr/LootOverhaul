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
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {
		ResourceLocation entityName = getEntityIdFromItem(item);
		
		if (entityName == null) {
			LootOverhaul.logger.warn("Skipping invalid loot entity id: " + entityName);
			return;
		}
		
		Entity entity = spawnCreature(world, entityName, position.x, position.y, position.z);
		
		if (entity == null) {
			LootOverhaul.logger.warn("Skipping invalid loot entity: " + item.getTagCompound());
			return;
		}

		applyItemEntityDataToEntity(world, item, entity);
	}

    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        ResourceLocation entityName = getEntityIdFromItem(stack);

        if (entityName != null) {
            name += entityName;
        }

        return name;
    }

	/**
	 * Spawns the creature specified by the egg's type in the location specified by the last three parameters.
	 * Parameters: world, entityID, x, y, z.
	 */
	@Nullable
	public static Entity spawnCreature(World worldIn, ResourceLocation entityID, double x, double y, double z) {
		Entity entity = null;
		EntityLiving entityliving = null;

		entity = EntityList.createEntityByIDFromName(entityID, worldIn);

		entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F), 0.0F);

		if (entity instanceof EntityLiving) {
			entityliving = (EntityLiving)entity;
			entityliving.rotationYawHead = entityliving.rotationYaw;
			entityliving.renderYawOffset = entityliving.rotationYaw;
			entityliving.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityliving)), (IEntityLivingData)null);
		}

		worldIn.spawnEntity(entity);

		if(entityliving != null) {
			entityliving.playLivingSound();
		}

		return entity;
	}

	/**
	 * Gets the entity ID associated with a given ItemStack in its NBT data.
	 */
	@Nullable
	public static ResourceLocation getEntityIdFromItem(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();

		if (tag == null)
		{
			return null;
		}
		else if (!tag.hasKey("EntityTag", 10))
		{
			return null;
		}
		else
		{
			NBTTagCompound entityTag = tag.getCompoundTag("EntityTag");
			return !entityTag.hasKey("id", 8) ? null : new ResourceLocation(entityTag.getString("id"));
		}
	}

	/**
	 * Applies the data in the EntityTag tag of the given ItemStack to the given Entity.
	 */
	public static void applyItemEntityDataToEntity(World entityWorld, ItemStack stack, @Nullable Entity targetEntity) {
		MinecraftServer minecraftserver = entityWorld.getMinecraftServer();

		if (minecraftserver != null && targetEntity != null)  {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag != null && tag.hasKey("EntityTag", 10)) {

				NBTTagCompound currentTag = targetEntity.writeToNBT(new NBTTagCompound());
				UUID uuid = targetEntity.getUniqueID();
				currentTag.merge(tag.getCompoundTag("EntityTag"));
				targetEntity.setUniqueId(uuid);
				targetEntity.readFromNBT(currentTag);
				
				if (tag.hasKey("Motion", 10)) {
					NBTTagCompound motionTag = tag.getCompoundTag("Motion");
					targetEntity.motionX = motionTag.getInteger("X");
					targetEntity.motionY = motionTag.getInteger("Y");
					targetEntity.motionZ = motionTag.getInteger("Z");
				}
			}
		}
	}

}
