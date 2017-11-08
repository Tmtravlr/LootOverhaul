package com.tmtravlr.lootoverhaul;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommonProxy {

	public void registerRenderers() {}
	
	public void registerEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new LootEventHandler());
	}
	
	public void callFromMainThread(Runnable runnable) {
		MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (mcServer != null) {
			mcServer.addScheduledTask(runnable);
		}
	}
	
	public void displayTradeEditor(int id, int villagerId, NBTTagCompound villagerTag) {}
	
}
