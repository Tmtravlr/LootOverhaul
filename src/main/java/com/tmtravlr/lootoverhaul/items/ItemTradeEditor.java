package com.tmtravlr.lootoverhaul.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.ConfigLoader;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.network.PacketHandlerClient;
import com.tmtravlr.lootoverhaul.network.SToCMessage;
import com.tmtravlr.lootoverhaul.trade.ContainerTradeEditor;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item that allows you to edit villager trades
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
public class ItemTradeEditor extends Item {
	
	public static final Item INSTANCE = new ItemTradeEditor().setMaxStackSize(1).setCreativeTab(LootOverhaul.tabLootOverhaul).setUnlocalizedName("trade_editor").setRegistryName(LootOverhaul.MOD_ID, "trade_editor");
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world,  List<String> tooltip, ITooltipFlag flag) {
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.trade_editor.info.line1"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.trade_editor.info.line2"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.trade_editor.info.line3"));
    	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.trade_editor.info.line4"));
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        if (stack.hasTagCompound()) {
        	if (stack.getTagCompound().getCompoundTag("Villager").hasKey("CustomName") && !stack.getTagCompound().getCompoundTag("Villager").getString("CustomName").isEmpty()) {
	        	name += " - " + stack.getTagCompound().getCompoundTag("Villager").getString("CustomName");
        	} else if (stack.getTagCompound().getCompoundTag("Villager").getCompoundTag("ForgeData").hasKey("VillagerDisplayName")) {
	        	name += " - " + stack.getTagCompound().getCompoundTag("Villager").getCompoundTag("ForgeData").getString("VillagerDisplayName");
	        }
        }
    	return name;
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        
        if (!ConfigLoader.enableVillagerTradeEditor || !player.isCreative()) {
        	return EnumActionResult.PASS;
        } else if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        } else {
        
	        if (!stack.hasTagCompound() || stack.getTagCompound().getCompoundTag("Villager").hasNoTags() || !stack.getTagCompound().getCompoundTag("Villager").hasKey("id")) {
	        	return EnumActionResult.FAIL;
	        }
	        
	        NBTTagCompound villagerTag = stack.getTagCompound().getCompoundTag("Villager");
	        ResourceLocation villagerId = new ResourceLocation(villagerTag.getString("id"));
	        
            BlockPos offsetPos = pos.offset(facing);
            Entity entity = EntityList.createEntityByIDFromName(villagerId, world);
            
            if (!(entity instanceof EntityVillager)) {
            	return EnumActionResult.FAIL;
            }
            
            EntityVillager villager = (EntityVillager) entity;
            float rotationYaw = player.rotationYaw + 180.0F;
            villager.setLocationAndAngles((double)offsetPos.getX() + 0.5, (double)offsetPos.getY() + 0.1, (double)offsetPos.getZ() + 0.5, rotationYaw > 360.0F ? rotationYaw - 360.0F : rotationYaw, 0.0F);
            villager.rotationYawHead = villager.rotationYaw;
            villager.renderYawOffset = villager.rotationYaw;
            villager.onInitialSpawn(world.getDifficultyForLocation(offsetPos), (IEntityLivingData)null);
            world.spawnEntity(entity);
            villager.playLivingSound();

            NBTTagCompound currentVillagerTag = villager.writeToNBT(new NBTTagCompound());
            UUID uuid = villager.getUniqueID();
            currentVillagerTag.merge(villagerTag);
            villager.setUniqueId(uuid);
            villager.readFromNBT(currentVillagerTag);

            return EnumActionResult.SUCCESS;
        }
    }
    
    public static void displayTradeEditorGui(EntityPlayerMP player, EntityVillager villager, ItemStack tradeStack) {
    	if (ConfigLoader.enableVillagerTradeEditor) {
	    	String displayName = villager.getDisplayName().getFormattedText();
	    	if (displayName.endsWith("\u00A7r")) {
	    		displayName = displayName.substring(0, displayName.length() - 2);
	    	}
	    	
			NBTTagCompound villagerTag = new NBTTagCompound();
			villagerTag = villager.writeToNBT(villagerTag);
			
			villagerTag.setString("id", String.valueOf(EntityList.getKey(villager)));
			NBTTagCompound forgeData = villagerTag.getCompoundTag("ForgeData");
			forgeData.setString("VillagerDisplayName", displayName);
			villagerTag.setTag("ForgeData", forgeData);
			villagerTag.removeTag("Pos");
			villagerTag.removeTag("Rotation");
			setVillagerTag(tradeStack, villagerTag);
			
	    	player.getNextWindowId();
			
			// Display trade editor GUI
			PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
			buff.writeInt(PacketHandlerClient.OPEN_TRADE_EDITOR);
			buff.writeInt(player.currentWindowId);
			buff.writeInt(villager.getEntityId());
			buff.writeCompoundTag(villagerTag);
			
			SToCMessage packet = new SToCMessage(buff);
			LootOverhaul.networkWrapper.sendTo(packet, player);
			
			player.openContainer = new ContainerTradeEditor(player.inventory);
	    	player.openContainer.windowId = player.currentWindowId;
	    	player.openContainer.addListener(player);
	    	MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
    	}
    }
    
    public static void setVillagerTag(ItemStack stack, NBTTagCompound villagerTag) {
    	if (!stack.hasTagCompound()) {
    		stack.setTagCompound(new NBTTagCompound());
    	}
    	
    	stack.getTagCompound().setTag("Villager", villagerTag);
    }
    
    public static void addVillagerTag(ItemStack stack, NBTTagCompound villagerTag) {
    	if (!stack.hasTagCompound()) {
    		stack.setTagCompound(new NBTTagCompound());
    	}
    	
    	NBTTagCompound oldVillagerTag = stack.getTagCompound().getCompoundTag("Villager");
    	oldVillagerTag.merge(villagerTag);
    	
    	stack.getTagCompound().setTag("Villager", oldVillagerTag);
    }
	
}
