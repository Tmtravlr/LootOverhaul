package com.tmtravlr.lootoverhaul;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;

public class ConfigLoader {

	public static File configFolder;
	public static Configuration config;
	
	public static File idFolder;

	public static boolean loadUsefulInfoFiles = true;
	public static boolean enableBlockDrops = true;
	public static boolean enableBlockDropsAll = true;
	public static boolean enableEntityDropsAll = true;
	public static boolean enableExtraLootTables = true;
	public static Set<String> extraLootTableBlacklist = new HashSet<>();
	
	public static void loadConfigFiles(File parentFolder) {
		configFolder = new File(parentFolder, "Loot Overhaul");
		
		if (!configFolder.exists()) {
			configFolder.mkdir();
		}
		
		idFolder = new File(configFolder, "Useful Info");
		
		if (!idFolder.exists()) {
			idFolder.mkdir();
		}
		
		config = new Configuration(new File(configFolder, "Loot Overhaul.cfg"));
	}
	
	public static void loadConfig() {
		config.load();
		syncConfig();
	}
	
	public static void syncConfig() {
		loadUsefulInfoFiles = config.getBoolean("Load useful info files", "options", loadUsefulInfoFiles, "Set to false to disable the 'Useful Info' files from generating.");
		enableBlockDrops = config.getBoolean("Enable loot table block drops", "options", enableBlockDrops, "Set to false to disable being able to override block drops with loot tables.");
		enableBlockDropsAll = config.getBoolean("Enable loot table 'minecraft:blocks/all' to drop for every block", "options", enableBlockDropsAll, "Set to false to disable the loot table 'minecraft:blocks/all' to drop for every block.\n");
		enableEntityDropsAll = config.getBoolean("Enable loot table 'minecraft:entities/all' to drop for every mob", "options", enableEntityDropsAll, "Set to false to disable the loot table 'minecraft:entities/all' to drop for every mob.\n");
		enableExtraLootTables = config.getBoolean("Enable loot table extras", "options", enableExtraLootTables, "Set to false to disable extra loot table drops being added to loot tables,\nlike 'minecraft:entities/zombie_extra' being added to 'minecraft:entities/zombie'.\n");
		extraLootTableBlacklist = new HashSet<>(Arrays.asList(config.getStringList("Extra loot table blacklist", "options", new String[0], "Add loot table names here to blacklist them from trying to pull in extra drops. Good\nif you have a mod that happens to have a loot table ending in _extra")));
		
		config.save();
	}
	
}
