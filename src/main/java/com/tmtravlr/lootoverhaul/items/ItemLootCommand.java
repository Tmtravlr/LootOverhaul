package com.tmtravlr.lootoverhaul.items;

import java.util.List;
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
 * Item for loot pools that will run a command
 * 
 * Example Usage: Sending a global message to all players (from the server)
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_command",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Command:\"tellraw @a \\\"The wither has been defeated!\\\"\"}"
 *  		}
 *  	]
 *  }
 * 
 * Example Usage: Teleports the looting (or killing) entity up 2 blocks and gives them a piece of coal.
 * 
 *	 {
 *  	"type": "item",
 *  	"name": "lootoverhaul:loot_command",
 *   	"weight": 1,
 *  	"functions": [
 *  		{
 *  			"function": "set_nbt",
 *  			"tag": "{Sender:\"#looter\",Command:[\"tp ~ ~2 ~\",\"give #looter coal\"]}"
 *  		},
 *  		{
 *  			"function": "lootoverhaul:replace_nbt",
 *  			"replace": "#looter",
 *  			"uuid": "looter"
 *  		}
 *  	]
 *  }
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since October 2016
 */
public class ItemLootCommand extends ItemLoot {

	public static final Item INSTANCE = new ItemLootCommand().setUnlocalizedName("loot_command").setRegistryName("loot_command");

	@Override
	protected void generateSpecificLoot(ItemStack item, World world, Vec3d position) {
		String senderName = "@";
		ICommandSender sender = null;
		
		if(world.getMinecraftServer() == null) {
			return;
		}
		
		if (item.getTagCompound().hasKey("Sender", 8)) {
			senderName = item.getTagCompound().getString("Sender");
			
			Entity senderEntity = null;
			
			try {
				senderEntity = CommandBase.getEntity(world.getMinecraftServer(), world.getMinecraftServer(), senderName);
			} catch (CommandException e) {
				LootOverhaul.logger.warn("Tried to parse an invalid entity selector: " + senderName, e);
			} 
			
			if (senderEntity != null) {
				sender = CommandSenderWrapper.create(world.getMinecraftServer()).withEntity(senderEntity, senderEntity.getPositionVector()).withSendCommandFeedback(world.getGameRules().getBoolean("commandBlockOutput"));
			}
		}
    	
		if (sender == null) {
			sender = new CommandSenderGeneric(senderName, world, new Vec3d(position.x, position.y, position.z));
		}

		ICommandManager manager = world.getMinecraftServer().getCommandManager();
			
		//Works if Command holds a single string command, or a list of string commands.
		if(item.getTagCompound().hasKey("Command", 8)) {
			
			manager.executeCommand(sender, item.getTagCompound().getString("Command"));
		} else if(item.getTagCompound().hasKey("Command", 9)) {
			
			NBTTagList commandList = item.getTagCompound().getTagList("Command", 8);
			
			for(int i = 0; i < commandList.tagCount(); i++) {
				manager.executeCommand(sender, commandList.getStringTagAt(i));
			}
		}
	}

    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = super.getItemStackDisplayName(stack);
        String commandName = (stack.hasTagCompound() && stack.getTagCompound().hasKey("Command", 8)) ? stack.getTagCompound().getString("Command").split(" ")[0] : null;

        if (commandName != null)
        {
            name += " - " + commandName;
        }

        return name;
    }
	
}
