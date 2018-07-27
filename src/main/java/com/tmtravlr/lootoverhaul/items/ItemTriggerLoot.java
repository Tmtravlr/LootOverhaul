package com.tmtravlr.lootoverhaul.items;

import java.util.List;
import java.util.Random;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootContextExtendedBuilder;

import net.minecraft.command.CommandResultStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTable;

/**
 * Item that generates a loot item when in inventory or when used
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ItemTriggerLoot extends Item {
	
	private static final Random RAND = new Random();
	
	public static final Item INSTANCE = new ItemTriggerLoot().setUnlocalizedName("trigger_loot").setRegistryName("trigger_loot");

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (entity != null && entity instanceof EntityPlayer) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null && !tag.getBoolean("Unwrap")) {
				
				EntityPlayer player = (EntityPlayer) entity;
				
				if (!world.isRemote) {
					ItemStack newStack = generateLootItem(stack, player);
					
					if (stack != newStack) {
						player.inventory.setInventorySlotContents(slot, newStack);
					}
					
					player.inventoryContainer.detectAndSendChanges();
				}
			}
		}
	}

	
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		
		if (!world.isRemote) {
			ItemStack newStack = generateLootItem(stack, player);
			
			player.inventoryContainer.detectAndSendChanges();
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, newStack);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	public ItemStack generateLootItem(ItemStack stack, EntityPlayer player) {
		try {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null && player.world instanceof WorldServer) {
				ResourceLocation lootTableLocation = new ResourceLocation(tag.getString("LootTable"));
				LootTable lootTable = player.world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
				
				if (!tag.getBoolean("Unwrap") || !player.isCreative()) {
					stack.splitStack(1);
				}
				
				if (lootTable != null) {
			    	LootContextExtendedBuilder builder = new LootContextExtendedBuilder((WorldServer)player.world).withLooter(player);
			    	
			    	List<ItemStack> lootList = lootTable.generateLootForPools(tag.hasKey("Seed") ? new Random(tag.getLong("Seed")) : RAND, builder.build());
			    	
			    	if (!lootList.isEmpty()) {
			    		
			    		if (stack.isEmpty()) {
			    			stack = lootList.get(0);
			    			lootList.remove(0);
			    			player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((RAND.nextFloat() - RAND.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			    		}
						
						for (ItemStack lootStack : lootList) {
							if (player.addItemStackToInventory(lootStack)) {
								player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((RAND.nextFloat() - RAND.nextFloat()) * 0.7F + 1.0F) * 2.0F);
							} else {
				                EntityItem entityitem = player.dropItem(lootStack, false);
							}
						}
			    	}
		    	}
			}
		} catch (Exception e) {
			LootOverhaul.logger.warn("Problem while generating loot from item!", e);
		}
		
		return stack;
	}
	
	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack) + ((stack.hasTagCompound() && stack.getTagCompound().hasKey("Unwrap")) ? (" " + I18n.translateToLocal("item.triggers.unwrap")) : "");
    }
}
