package com.tmtravlr.lootoverhaul;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigLoader {

	public static File configFolder;
	public static Configuration config;
	
	public static boolean enableVillagerTradeEditor = true;
	public static boolean enableExtraLootTables = true;
	public static boolean enableBlockDropsAll = true;
	public static boolean enableEntityDropsAll = true;
	
	public static void loadConfigFiles(File parentFolder) {
		configFolder = new File(parentFolder, "Loot Overhaul");
		
		if(!configFolder.exists()) {
			configFolder.mkdir();
		}
		
		config = new Configuration(new File(configFolder, "Loot Overhaul.cfg"));
	}
	
	public static void loadConfig() {
		config.load();
		syncConfig();
	}
	
	public static void syncConfig() {
		enableVillagerTradeEditor = config.getBoolean("Enable villager trade editor", "options", enableVillagerTradeEditor, "Set to false to disable using the villager trade editor (in case you don't want creative\nplayers to be able to edit villager trades and invulnerability).\n");
		enableExtraLootTables = config.getBoolean("Enable 'extra' loot tables", "options", enableExtraLootTables, "Set to false to disable loot tables ending with _extra dropping in addition\nto the normal loot tables (like 'minecraft:entities/zombie_extra' dropping with 'minecraft:entities/zombie').\n");
		enableBlockDropsAll = config.getBoolean("Enable loot table 'minecraft:blocks/all' to drop for every block", "options", enableBlockDropsAll, "Set to false to disable the loot table 'minecraft:blocks/all' to drop for every block.\n");
		enableEntityDropsAll = config.getBoolean("Enable loot table 'minecraft:entities/all' to drop for every mob", "options", enableEntityDropsAll, "Set to false to disable the loot table 'minecraft:entities/all' to drop for every mob.\n");
		
		config.save();
	}
	
}
