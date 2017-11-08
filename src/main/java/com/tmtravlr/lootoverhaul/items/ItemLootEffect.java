package com.tmtravlr.lootoverhaul.items;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.commands.CommandSenderGeneric;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item for loot pools that will give a mob an effect
 * 
 * Example Usage: Giving the looter a health boost 2 effect for 60 seconds
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_effect",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Entity:\"#looter\", Potion:\"minecraft:health_boost\", Duration:1200, Amplifier:1}"
 *  		},
 *  		{
 *  			"function": "lootoverhaul:replace_nbt",
 *  			"replace": "#looter",
 *  			"target": "looter"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Giving the looter a long swiftness effect (same as in the vanilla potion)
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_effect",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Entity:\"#looter\", PotionType:\"minecraft:long_swiftness\"}"
 *  		},
 *  		{
 *  			"function": "lootoverhaul:replace_nbt",
 *  			"replace": "#looter",
 *  			"target": "looter"
 *  		}
 *  	]
 *  }
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2017
 */
public class ItemLootEffect extends ItemLoot {
	
	public static final Item INSTANCE = new ItemLootEffect().setUnlocalizedName("loot_effect").setRegistryName("loot_effect");

	@Override
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {

		if (world.getMinecraftServer() != null && item.getTagCompound().hasKey("Entity", 8)) {
			String entityName = item.getTagCompound().getString("Entity");
			
			Entity entity = null;
			
			try {
				entity = CommandBase.getEntity(world.getMinecraftServer(), world.getMinecraftServer(), entityName);
			} catch (Exception e) {
				LootOverhaul.logger.warn("Couldn't find an entity with name, uuid, or selector " + entityName, e);
			}
			
			if (entity instanceof EntityLivingBase) {
				if (item.getTagCompound().hasKey("PotionType")) {
					PotionType potionType = PotionType.getPotionTypeForName(item.getTagCompound().getString("PotionType"));
					
					if (potionType != null) {
						for (PotionEffect effect : potionType.getEffects()) {
							((EntityLivingBase)entity).addPotionEffect(effect);
						}
					} else {
						LootOverhaul.logger.warn("Tried to parse an invalid potion type '" + item.getTagCompound().getString("PotionType") + "'");
					}
				}
				Potion potion = Potion.getPotionFromResourceLocation(item.getTagCompound().getString("Potion"));
				
				if (potion != null) {
					PotionEffect effect = new PotionEffect(potion, item.getTagCompound().getInteger("Duration"), item.getTagCompound().getInteger("Amplifier"), item.getTagCompound().getBoolean("Ambient"), !item.getTagCompound().getBoolean("HideParticles"));
					
					((EntityLivingBase)entity).addPotionEffect(effect);
				} else {
					LootOverhaul.logger.warn("Tried to parse an invalid potion '" + item.getTagCompound().getString("Potion") + "'");
				}
			}
		}
	}
	
	@Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        String effectName = stack.hasTagCompound() ? (stack.getTagCompound().hasKey("PotionType", 8) ? stack.getTagCompound().getString("PotionType") : stack.getTagCompound().getString("Potion")) : "";

        return name += effectName;
    }
}
