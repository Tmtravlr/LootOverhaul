package com.tmtravlr.lootoverhaul;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.tmtravlr.lootoverhaul.asm.ObfuscatedNames;
import com.tmtravlr.lootoverhaul.recipes.ExtraRecipesManager;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.FunctionObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ExtraFilesManager {

	private static final Gson LOOT_TABLE_GSON = (new GsonBuilder())
			.registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
			.registerTypeAdapter(LootPool.class, new LootPool.Serializer())
			.registerTypeAdapter(LootTable.class, new LootTable.Serializer())
			.registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer())
			.registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
			.registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
			.registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
			.create();

	public static final List<File> EXTRA_LOOT_FOLDERS = new ArrayList<>();
	public static final List<File> EXTRA_ADVANCEMENT_FOLDERS = new ArrayList<>();
	public static final List<File> EXTRA_FUNCTION_FOLDERS = new ArrayList<>();
	public static final List<File> EXTRA_STRUCTURE_FOLDERS = new ArrayList<>();
	public static final List<File> EXTRA_CRAFTING_RECIPE_FOLDERS = new ArrayList<>();

	public static LootTable loadExtraLootTable(ResourceLocation resource, LootTableManager lootTableManager) {		
		for (File folder : EXTRA_LOOT_FOLDERS) {
			if (folder.isDirectory()) {
				File lootTableFile = new File(new File(folder, resource.getResourceDomain()), resource.getResourcePath() + ".json");

				if (lootTableFile.exists()) {
					try {
						String data = Files.toString(lootTableFile, StandardCharsets.UTF_8);
						
						LootTable table = ForgeHooks.loadLootTable(LOOT_TABLE_GSON, new ResourceLocation(resource.getResourceDomain(), resource.getResourcePath() + "_extra.json"), data, true, lootTableManager);

						return table;
					}
					catch (IOException ioexception)
					{
						LootOverhaul.logger.warn("Couldn't load loot table {} from {}", resource, lootTableFile, ioexception);
						return LootTable.EMPTY_LOOT_TABLE;
					}
				}
			}
		}

		return null;
	}

	public static List<LootTable> loadLootTableExtras(ResourceLocation resource, LootTableManager lootTableManager) {
		ArrayList<LootTable> lootTables = new ArrayList<>();
		ArrayList<File> foldersToCheck = new ArrayList<>(EXTRA_LOOT_FOLDERS);

		foldersToCheck.add(ObfuscationReflectionHelper.getPrivateValue(LootTableManager.class, lootTableManager, ObfuscatedNames.BASE_FOLDER_FIELD_SRG, "baseFolder"));

		for (File folder : foldersToCheck) {
			if (folder.isDirectory()) {
				File lootTableFile = new File(new File(folder, resource.getResourceDomain()), resource.getResourcePath() + "_extra.json");

				if (lootTableFile.exists()) {
					try {
						String data = Files.toString(lootTableFile, StandardCharsets.UTF_8);
						
						LootTable table = ForgeHooks.loadLootTable(LOOT_TABLE_GSON, new ResourceLocation(resource.getResourceDomain(), resource.getResourcePath() + "_extra.json"), data, true, lootTableManager);

						lootTables.add(table);
					}
					catch (IOException ioexception)
					{
						LootOverhaul.logger.warn("Couldn't load loot table {} from {}", resource, lootTableFile, ioexception);
					}
				}
			}
		}

		return lootTables;
	}

	public static Map<ResourceLocation, Advancement.Builder> loadExtraAdvancements(Map<ResourceLocation, Advancement.Builder> map) {

		for (File folder : EXTRA_ADVANCEMENT_FOLDERS) {
			if (folder.isDirectory()) {
				
				for (File advancementFile : FileUtils.listFiles(folder, new String[] {"json"}, true)) {
					String fileName = FilenameUtils.removeExtension(folder.toURI().relativize(advancementFile.toURI()).toString());
					String[] astring = fileName.split("/", 2);

					if (astring.length == 2) {
						ResourceLocation resource = new ResourceLocation(astring[0], astring[1]);

						if (!map.containsKey(resource)) {
							try {
								Advancement.Builder advancementBuilder = (Advancement.Builder)JsonUtils.gsonDeserialize(AdvancementManager.GSON, FileUtils.readFileToString(advancementFile, StandardCharsets.UTF_8), Advancement.Builder.class);
	
								if (advancementBuilder == null)
								{
									LootOverhaul.logger.error("Couldn't load extra advancement " + resource + " from " + advancementFile + " as it's empty or null");
								} else {
									map.put(resource, advancementBuilder);
								}
							} catch (IllegalArgumentException | JsonParseException e) {
								LootOverhaul.logger.error("Parsing error loading extra advancement " + resource, e);
							} catch (IOException e) {
								LootOverhaul.logger.error("Couldn't read extra advancement " + resource + " from " + advancementFile, e);
							}
						}
					}
				}
			}
		}

		return map;
	}
	
	public static Map<ResourceLocation, FunctionObject> loadExtraFunctions(Map<ResourceLocation, FunctionObject> map, FunctionManager functionManager) {
		for (File folder : EXTRA_FUNCTION_FOLDERS) {
			if (folder.isDirectory()) {
				
				for (File functionFile : FileUtils.listFiles(folder, new String[] {"mcfunction"}, true)) {
					
	                String s = FilenameUtils.removeExtension(folder.toURI().relativize(functionFile.toURI()).toString());
	                String[] astring = s.split("/", 2);

	                if (astring.length == 2) {
	                    ResourceLocation resourcelocation = new ResourceLocation(astring[0], astring[1]);
	                    
	                    if (!map.containsKey(resourcelocation)) {
		                    try {
		                        map.put(resourcelocation, FunctionObject.create(functionManager, Files.readLines(functionFile, StandardCharsets.UTF_8)));
		                    } catch (Throwable throwable) {
		                        LootOverhaul.logger.error("Couldn't read custom function " + resourcelocation + " from " + functionFile, throwable);
		                    }
	                    }
	                }
	            }
			}
		}
		
		return map;
	}

	public static File getStructureFile(File customFile, ResourceLocation fileLocation) {
		if (customFile.exists()) {
			return customFile;
		}

		for (File folder : EXTRA_STRUCTURE_FOLDERS) {
			String domain = fileLocation.getResourceDomain();
			String path = fileLocation.getResourcePath();

			File structureFile = new File(folder, domain + "/" + path + ".nbt");

			if (structureFile.exists()) {
				return structureFile;
			}
		}

		return customFile;
	}

	public static void loadExtraCraftingRecipes() {
		for (File folder : EXTRA_CRAFTING_RECIPE_FOLDERS) {
			ExtraRecipesManager.loadRecipesFromFolder(folder);
		}
	}

}
