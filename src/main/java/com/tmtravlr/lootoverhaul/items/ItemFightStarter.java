package com.tmtravlr.lootoverhaul.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item that starts a fight between two mobs.
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
public class ItemFightStarter extends Item {
	
	public static final Item INSTANCE = new ItemFightStarter().setMaxStackSize(1).setCreativeTab(LootOverhaul.tabLootOverhaul).setUnlocalizedName("fight_starter").setRegistryName(LootOverhaul.MOD_ID, "fight_starter");
	
	@Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
    	startFight(player.getHeldItem(hand), player, entity);
    	return true;
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase player) {
    	if(player instanceof EntityPlayer) {
    		startFight(stack, (EntityPlayer) player, entity);
    	}
        return true;
    }
    
    private void startFight(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
    	if (player.world.getMinecraftServer() != null) {
	    	if (!stack.hasTagCompound()) {
	    		stack.setTagCompound(new NBTTagCompound());
	    	}
	    	
	    	if (stack.getTagCompound().hasKey("Entity1")) {
	    		EntityLivingBase firstMob;
	    		
	    		try {
	    			UUID uuid = UUID.fromString(stack.getTagCompound().getString("Entity1"));
	    			
	    			Entity firstEntity = player.world.getMinecraftServer().getEntityFromUuid(uuid);
	    			
	    			if (!(firstEntity instanceof EntityLivingBase)) {
	    				throw new IllegalArgumentException("First entity must be a living entity.");
	    			}
	    			
	    			firstMob = (EntityLivingBase) firstEntity;
	    		} catch (IllegalArgumentException e) {
	    			e.printStackTrace();
	    			TextComponentTranslation message = new TextComponentTranslation("item.fight_starter.message.noMob");
	    			message.getStyle().setColor(TextFormatting.RED);
	    			player.sendMessage(message);
		    		
		    		stack.getTagCompound().removeTag("Entity1");
	    			return;
	    		}
	    		
	    		if (firstMob == entity) {
	    			TextComponentTranslation message = new TextComponentTranslation("item.fight_starter.message.same");
	    			message.getStyle().setColor(TextFormatting.RED);
	    			player.sendMessage(message);
		    		
		    		stack.getTagCompound().removeTag("Entity1");
	    			return;
	    		}

	    		firstMob.setRevengeTarget(entity);
	    		entity.setRevengeTarget(firstMob);
	    		
	    		stack.getTagCompound().removeTag("Entity1");
	    		
	    		player.sendMessage(new TextComponentTranslation("item.fight_starter.message.secondEntity", entity.getName()));
	    	} else {
	    		stack.getTagCompound().setString("Entity1", entity.getUniqueID().toString());
	    		
	    		player.sendMessage(new TextComponentTranslation("item.fight_starter.message.firstEntity", entity.getName()));
	    	}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world,  List<String> tooltip, ITooltipFlag flag) {
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.fight_starter.info.line1"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.fight_starter.info.line2"));
    }
	
}
