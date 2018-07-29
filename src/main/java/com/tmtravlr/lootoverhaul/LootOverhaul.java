package com.tmtravlr.lootoverhaul;

import org.apache.logging.log4j.Logger;

import com.tmtravlr.lootoverhaul.commands.CommandEntityVar;
import com.tmtravlr.lootoverhaul.commands.CommandGlobalVar;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.network.CToSMessage;
import com.tmtravlr.lootoverhaul.network.PacketHandlerClient;
import com.tmtravlr.lootoverhaul.network.PacketHandlerServer;
import com.tmtravlr.lootoverhaul.network.SToCMessage;
import com.tmtravlr.lootoverhaul.utilities.ConfigIdFileGenerator;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = LootOverhaul.MOD_ID, name = LootOverhaul.MOD_NAME, version = LootOverhaul.VERSION)
public class LootOverhaul {
    public static final String MOD_ID = "lootoverhaul";
    public static final String MOD_NAME = "Loot Overhaul";
	public static final String VERSION = "@VERSION@";
	
	@Mod.Instance(LootOverhaul.MOD_ID)
	public static LootOverhaul instance;
	
	@SidedProxy(clientSide="com.tmtravlr.lootoverhaul.ClientProxy", serverSide="com.tmtravlr.lootoverhaul.CommonProxy")
	public static CommonProxy proxy;
	
	public static Logger logger;
	
	public static SimpleNetworkWrapper networkWrapper;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	logger = event.getModLog();
		
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
		networkWrapper.registerMessage(PacketHandlerServer.class, CToSMessage.class, 0, Side.SERVER);
		networkWrapper.registerMessage(PacketHandlerClient.class, SToCMessage.class, 1, Side.CLIENT);
    	
    	ConfigLoader.loadConfigFiles(event.getSuggestedConfigurationFile().getParentFile());
    	ConfigLoader.loadConfig();
    	
    	LootHelper.loadConditions();
    	LootHelper.loadFuntions();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
    }
    
    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandGlobalVar());
    	event.registerServerCommand(new CommandEntityVar());
    	
    	ConfigIdFileGenerator.generateIDFiles();
    }
}
