package com.tmtravlr.lootoverhaul;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.tmtravlr.lootoverhaul.commands.CommandEntityVar;
import com.tmtravlr.lootoverhaul.commands.CommandGlobalVar;
import com.tmtravlr.lootoverhaul.items.ItemFightStarter;
import com.tmtravlr.lootoverhaul.items.ItemLootBlock;
import com.tmtravlr.lootoverhaul.items.ItemLootCommand;
import com.tmtravlr.lootoverhaul.items.ItemLootEffect;
import com.tmtravlr.lootoverhaul.items.ItemLootEntity;
import com.tmtravlr.lootoverhaul.items.ItemLootFill;
import com.tmtravlr.lootoverhaul.items.ItemLootItem;
import com.tmtravlr.lootoverhaul.items.ItemLootStructure;
import com.tmtravlr.lootoverhaul.items.ItemNBTChecker;
import com.tmtravlr.lootoverhaul.items.ItemTradeEditor;
import com.tmtravlr.lootoverhaul.items.ItemTriggerCommand;
import com.tmtravlr.lootoverhaul.items.ItemTriggerLoot;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.network.CToSMessage;
import com.tmtravlr.lootoverhaul.network.PacketHandlerClient;
import com.tmtravlr.lootoverhaul.network.PacketHandlerServer;
import com.tmtravlr.lootoverhaul.network.SToCMessage;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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
	
	public static CreativeTabs tabLootOverhaul = new CreativeTabs(MOD_ID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(ItemTradeEditor.INSTANCE);
		}
	};
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	logger = event.getModLog();
		
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
		networkWrapper.registerMessage(PacketHandlerServer.class, CToSMessage.class, 0, Side.SERVER);
		networkWrapper.registerMessage(PacketHandlerClient.class, SToCMessage.class, 1, Side.CLIENT);
    	
    	proxy.registerEventHandlers();
    	
    	ConfigLoader.loadConfigFiles(event.getSuggestedConfigurationFile().getParentFile());
    	ConfigLoader.loadConfig();
    	
    	ForgeRegistries.ITEMS.register(ItemLootItem.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootBlock.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootFill.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootEntity.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootStructure.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootCommand.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemLootEffect.INSTANCE);
    	
    	ForgeRegistries.ITEMS.register(ItemTriggerCommand.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemTriggerLoot.INSTANCE);

    	ForgeRegistries.ITEMS.register(ItemTradeEditor.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemNBTChecker.INSTANCE);
    	ForgeRegistries.ITEMS.register(ItemFightStarter.INSTANCE);
    	
    	LootHelper.loadConditions();
    	LootHelper.loadFuntions();
    	
    	//Testing:
//    	File extraDataFolder = new File(event.getSuggestedConfigurationFile().getParentFile().getParentFile(), "extra_data");
//    	ExtraFilesManager.EXTRA_ADVANCEMENT_FOLDERS.add(new File(extraDataFolder, "advancements"));
//    	ExtraFilesManager.EXTRA_CRAFTING_RECIPE_FOLDERS.add(new File(extraDataFolder, "recipes"));
//    	ExtraFilesManager.EXTRA_FUNCTION_FOLDERS.add(new File(extraDataFolder, "functions"));
//    	ExtraFilesManager.EXTRA_LOOT_FOLDERS.add(new File(extraDataFolder, "loot_tables"));
//    	ExtraFilesManager.EXTRA_STRUCTURE_FOLDERS.add(new File(extraDataFolder, "structures"));
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
        ExtraFilesManager.loadExtraCraftingRecipes();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	
    }
    
    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandGlobalVar());
    	event.registerServerCommand(new CommandEntityVar());
    }
}
