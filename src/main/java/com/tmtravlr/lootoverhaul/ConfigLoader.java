package com.tmtravlr.lootoverhaul;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public class ConfigLoader {

	public static File configFolder;
	public static Configuration config;
	
	public static File idFolder;

	public static boolean loadUsefulInfoFiles = true;
	
	public static boolean enableExtraLootTables = true;
	public static Set<ResourceLocation> extraLootTableBlacklist = new HashSet<>();
	
	public static boolean enableBlockDrops = true;
	public static boolean enableBlockDropsAll = true;
	public static boolean useBlockDropWhitelist = true;
	public static Set<ResourceLocation> blockDropsToReplace = new HashSet<>();
	
	public static boolean enableEntityDropsAll = true;
	
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
		loadUsefulInfoFiles = config.getBoolean("Load useful info files", "other_options", loadUsefulInfoFiles, "Set to false to disable the 'Useful Info' files from generating.");
		
		enableExtraLootTables = config.getBoolean("Enable loot table extras", "loot_table_extras", enableExtraLootTables, "Set to false to disable extra loot table drops being added to loot tables,\nlike 'minecraft:entities/zombie_extra' being added to 'minecraft:entities/zombie'.\n");
		extraLootTableBlacklist = Arrays.asList(config.getStringList("Extra loot table blacklist", "loot_table_extras", new String[0], "Add loot table names here to blacklist them from trying to pull in extra drops. Good\nif you have a mod that happens to have a loot table ending in _extra"))
				.stream().map(ResourceLocation::new).collect(Collectors.toSet());
		
		enableBlockDrops = config.getBoolean("Enable loot table block drops", "block_drops", enableBlockDrops, "Set to false to disable being able to override block drops with loot tables.");
		enableBlockDropsAll = config.getBoolean("Enable loot table 'minecraft:blocks/all' to drop for every block", "block_drops", enableBlockDropsAll, "Set to false to disable the loot table 'minecraft:blocks/all' to drop for every block.\n");
		useBlockDropWhitelist = config.getBoolean("Use the block drop replacement whitelist", "block_drops", useBlockDropWhitelist, "Set to false to disable the whitelist for block drops. Just a warning,\nyour console will be spammed every time a new block is broken.\n");
		blockDropsToReplace = Arrays.asList(config.getStringList("Block drop replacement whitelist", "block_drops", new String[0], "Add block names like minecraft:stone here to allow their drops to be replaced."))
				.stream().map(ResourceLocation::new).collect(Collectors.toSet());
		
		enableEntityDropsAll = config.getBoolean("Enable loot table 'minecraft:entities/all' to drop for every mob", "mob_drops", enableEntityDropsAll, "Set to false to disable the loot table 'minecraft:entities/all' to drop for every mob.\n");
		
		config.save();
	}
	
}
