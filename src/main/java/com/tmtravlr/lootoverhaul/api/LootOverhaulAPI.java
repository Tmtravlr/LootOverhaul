package com.tmtravlr.lootoverhaul.api;

import java.io.File;

import com.tmtravlr.lootoverhaul.ExtraFilesManager;

/**
 * Convenience methods for using loot overhaul's functionality from another mod. 
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
public class LootOverhaulAPI {
	
	/**
	 * Add extra folders to check through when loading crafting recipes.
	 * 
	 * @param folders Folders to add
	 */
	public static void addExtraRecipeFolders(File ... folders) {
		for (File folder : folders) {
			ExtraFilesManager.EXTRA_CRAFTING_RECIPE_FOLDERS.add(folder);
		}
	}
	
	/**
	 * Add extra folders to check through when loading structures.
	 * 
	 * @param folders Folders to add
	 */
	public static void addExtraStructureFolders(File ... folders) {
		for (File folder : folders) {
			ExtraFilesManager.EXTRA_STRUCTURE_FOLDERS.add(folder);
		}
	}
	
	/**
	 * Add extra folders to check through when loading advancements.
	 * 
	 * @param folders Folders to add
	 */
	public static void addExtraAdvancementFolders(File ... folders) {
		for (File folder : folders) {
			ExtraFilesManager.EXTRA_ADVANCEMENT_FOLDERS.add(folder);
		}
	}
	
	/**
	 * Add extra folders to check through when loading functions.
	 * 
	 * @param folders Folders to add
	 */
	public static void addExtraFunctionFolders(File ... folders) {
		for (File folder : folders) {
			ExtraFilesManager.EXTRA_FUNCTION_FOLDERS.add(folder);
		}
	}
	
	/**
	 * Add extra folders to check through when loading loot tables.
	 * 
	 * @param folders Folders to add
	 */
	public static void addExtraLootFolders(File ... folders) {
		for (File folder : folders) {
			ExtraFilesManager.EXTRA_LOOT_FOLDERS.add(folder);
		}
	}
	
}
