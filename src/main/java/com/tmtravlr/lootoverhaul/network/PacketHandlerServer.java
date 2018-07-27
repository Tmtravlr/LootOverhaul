package com.tmtravlr.lootoverhaul.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Tmtravlr 
 */
public class PacketHandlerServer implements IMessageHandler<CToSMessage,IMessage> {

	//Types of packets

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
		
		default:
			//do nothing.
		}

		return null;
	}
}
