package com.tmtravlr.lootoverhaul.network;

import java.util.UUID;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.items.ItemTradeEditor;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Tmtravlr 
 */
public class PacketHandlerServer implements IMessageHandler<CToSMessage,IMessage> {

	//Types of packets
	public static final int UPDATE_VILLAGER_TRADES = 0;

	/**
	 * Handles Server Side Packets. Only returns null.
	 */
	@Override
	public IMessage onMessage(CToSMessage packet, MessageContext context) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server == null) {
			return null;
		}
		
		PacketBuffer buff = new PacketBuffer(Unpooled.wrappedBuffer(packet.getData()));

		int type = buff.readInt();
		
		switch(type) {
		case UPDATE_VILLAGER_TRADES: {
			server.addScheduledTask(() -> {
				try {
					int dimensionId = buff.readInt();
					int villagerId = buff.readInt();
					long playerUUIDMost = buff.readLong();
					long playerUUIDLeast = buff.readLong();
					NBTTagCompound villagerTag = buff.readCompoundTag();
					
					World world = server.getWorld(dimensionId);
					Entity villager = world.getEntityByID(villagerId);
					EntityPlayer player = world.getPlayerEntityByUUID(new UUID(playerUUIDMost, playerUUIDLeast));

					if (villager instanceof EntityVillager) {
						NBTTagCompound originalTag = new NBTTagCompound();
						villager.writeToNBT(originalTag);
						
						UUID villagerUUID = villager.getUniqueID();
						originalTag.merge(villagerTag);
						villager.setUniqueId(villagerUUID);
						
						villager.readFromNBT(originalTag);
					}

					if (player != null) {
						ItemStack tradeEditor = player.getHeldItemMainhand().getItem() == ItemTradeEditor.INSTANCE ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

						if (tradeEditor.getItem() == ItemTradeEditor.INSTANCE) {
							ItemTradeEditor.addVillagerTag(tradeEditor, villagerTag);
						}
					}

				} catch (Exception e) {
					LootOverhaul.logger.warn("Error while trying to update villager trades.", e);
				}
			});
			break;
		}
		default:
			//do nothing.
		}

		return null;
	}
}
