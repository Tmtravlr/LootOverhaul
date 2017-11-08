package com.tmtravlr.lootoverhaul.items;

import java.util.Random;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.commands.CommandSenderGeneric;

import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item that triggers a command when in inventory or when used
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ItemTriggerCommand extends Item {
	
	public static final Item INSTANCE = new ItemTriggerCommand().setUnlocalizedName("trigger_command").setRegistryName("trigger_command");

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (entity != null && entity instanceof EntityPlayer) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null && tag.getBoolean("Unwrap") == false) {
				
				if (tag.getInteger("Delay") > 0) {
					tag.setInteger("Delay", tag.getInteger("Delay") - 1);
					return;
				}
				
				EntityPlayer player = (EntityPlayer) entity;
				
				if (!world.isRemote) {
					runCommands(stack, player);
				}
			}
		}
	}

	
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		
		if (!world.isRemote) {
			runCommands(stack, player);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	public void runCommands(ItemStack stack, EntityPlayer player) {
		try {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null) {
				ICommandSender sender = CommandSenderWrapper.create(player.world.getMinecraftServer()).withEntity(player, player.getPositionVector()).withSendCommandFeedback(!tag.getBoolean("HideOutput") && player.world.getGameRules().getBoolean("commandBlockOutput"));
				ICommandManager manager = player.world.getMinecraftServer().getCommandManager();
				
				if (tag.hasKey("CommandList", 9)) {
					NBTTagList tagList = tag.getTagList("CommandList", 8);
					for (int i = 0; i < tagList.tagCount(); i++) {
						String command = tagList.getStringTagAt(i);
						manager.executeCommand(sender, command);
					}

				}
				
				if (tag.hasKey("Next", 10) && (tag.getBoolean("Stop") == false || player.capabilities.isCreativeMode)) {
					
					//Make the item into the "Next" item and move the current item to the end of the stack
					NBTTagCompound thisItem = (NBTTagCompound) tag.copy();
					thisItem.removeTag("Next");
					tag = tag.getCompoundTag("Next");
					
					if (tag.hasKey("DelayMax", 3)) {
						tag.setInteger("Delay", tag.getInteger("DelayMax"));
					}
					
					NBTTagCompound lastTag = tag;
					
					//Find the last item
					while (lastTag.hasKey("Next", 10)) {
						lastTag = lastTag.getCompoundTag("Next");
					}
					
					//Now lastTag should hold the last tag in the list; move this item there
					lastTag.setTag("Next", thisItem);
					stack.setTagCompound(tag);
				} else {
					stack.splitStack(1);
				}
				
			}
		} catch (Exception e) {
			LootOverhaul.logger.warn("Problem while running command from item!", e);
		}

	}
}
