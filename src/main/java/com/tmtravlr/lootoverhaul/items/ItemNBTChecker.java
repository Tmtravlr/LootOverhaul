package com.tmtravlr.lootoverhaul.items;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.ConfigLoader;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item that copies and saves the nbt of an entity, tile, or item.
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
public class ItemNBTChecker extends Item {
	
	public static final Item INSTANCE = new ItemNBTChecker().setMaxStackSize(1).setCreativeTab(LootOverhaul.tabLootOverhaul).setUnlocalizedName("nbt_checker").setRegistryName(LootOverhaul.MOD_ID, "nbt_checker");
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    	if(!player.world.isRemote) {
    		if (hand == EnumHand.MAIN_HAND) {
	    		TileEntity te = world.getTileEntity(pos);
		    	if(te != null) {
		    		NBTTagCompound tag = new NBTTagCompound();
		    		tag = te.writeToNBT(tag);
		    		
					copyAndSaveNBTString(tag, player);
		    	} else {
		    		TextComponentTranslation message = new TextComponentTranslation("item.nbt_checker.message.noBlock", "" + pos.getX() + " " + pos.getY() + " " + pos.getZ());
					message.getStyle().setColor(TextFormatting.RED);
					player.sendMessage(message);
		    	}
    		}
	
			return EnumActionResult.SUCCESS;
		}
    	
    	return EnumActionResult.PASS;
    }
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    	if (hand == EnumHand.MAIN_HAND) {
    		if (!player.world.isRemote) {
    			ItemStack heldItem = player.getHeldItemOffhand();
    	    	
    	    	if (!heldItem.isEmpty()) {
    	    		
    	    		this.copyAndSaveNBTString(heldItem.getTagCompound(), player);
    	    		
    	    	} else {
    				TextComponentTranslation message = new TextComponentTranslation("item.nbt_checker.message.noItem");
    				message.getStyle().setColor(TextFormatting.RED);
    				player.sendMessage(message);
    	    	}
    		}
        	
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    	}
    	
        return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
    
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
    	saveAndCopyEntityInfo(player, entity);
    	return true;
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase player) {
    	if(player instanceof EntityPlayer) {
    		saveAndCopyEntityInfo((EntityPlayer)player, entity);
    	}
    	
        return false;
    }
    
    private void saveAndCopyEntityInfo(EntityPlayer player, EntityLivingBase entity) {
    	if(!player.world.isRemote) {
	    	NBTTagCompound tag = new NBTTagCompound();
	    	tag = entity.writeToNBT(tag);
	
			copyAndSaveNBTString(tag, player);
    	}
    }
    
    private void copyAndSaveNBTString(NBTTagCompound tag, EntityPlayer player) {
    	String nbtString = tag == null ? "{}" : tag.toString();
    	
		try {
	    	StringSelection selection = new StringSelection(nbtString);
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    	
	    	File nbtOutput = new File(ConfigLoader.configFolder, "NBTOutput.txt");
			PrintStream writeStream;
	        
	        clipboard.setContents(selection, selection);
			writeStream = new PrintStream(nbtOutput);
		
		
			writeStream.println(nbtString);
			
			writeStream.close();
		
			player.sendMessage(new TextComponentTranslation("item.nbt_checker.message.savednbt"));
		
		} catch (Exception e) {
			e.printStackTrace();
			TextComponentTranslation message = new TextComponentTranslation("item.nbt_checker.message.failed", e.getMessage());
			message.getStyle().setColor(TextFormatting.RED);
			player.sendMessage(message);
		}
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world,  List<String> tooltip, ITooltipFlag flag) {
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line1"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line2"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line3"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line4"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line5"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line6"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line7"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.nbt_checker.info.line8"));
    }
	
}
