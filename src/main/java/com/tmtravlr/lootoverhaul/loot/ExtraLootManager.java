package com.tmtravlr.lootoverhaul.loot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ExtraLootManager {

	private static final Gson LOOT_TABLE_GSON = (new GsonBuilder())
			.registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
			.registerTypeAdapter(LootPool.class, new LootPool.Serializer())
			.registerTypeAdapter(LootTable.class, new LootTable.Serializer())
			.registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer())
			.registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
			.registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
			.registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
			.create();

	public static List<LootTable> loadLootTableExtras(ResourceLocation resource, LootTableManager lootTableManager) {
		LoadLootTableExtrasEvent event = new LoadLootTableExtrasEvent(resource, lootTableManager);
		ArrayList<LootTable> lootTables = new ArrayList<>();
		
		if (!MinecraftForge.EVENT_BUS.post(event)) {
			lootTables.addAll(event.extraLootTables);
		}

		return lootTables;
	}
	
	public static void loadDefaultLootTableExtras(LoadLootTableExtrasEvent event) {
		File folder = ObfuscationReflectionHelper.getPrivateValue(LootTableManager.class, event.getLootTableManager(), "field_186528_d", "baseFolder");

		if (folder.isDirectory()) {
			File lootTableFile = new File(new File(folder, event.getLootTableLocation().getResourceDomain()), event.getLootTableLocation().getResourcePath() + "_extra.json");

			if (lootTableFile.exists()) {
				try {
					String data = Files.toString(lootTableFile, StandardCharsets.UTF_8);
					
					LootTable table = ForgeHooks.loadLootTable(LOOT_TABLE_GSON, new ResourceLocation(event.getLootTableLocation().getResourceDomain(), event.getLootTableLocation().getResourcePath() + "_extra.json"), data, true, event.getLootTableManager());

					event.addExtraLootTable(table);
				}
				catch (IOException ioexception)
				{
					LootOverhaul.logger.warn("Couldn't load loot table {} from {}", event.getLootTableLocation(), lootTableFile, ioexception);
				}
			}
		}
	}
	
	public static class LoadLootTableExtrasEvent extends Event {
		
		private ResourceLocation lootTableLocation;
		private LootTableManager lootTableManager;
		private List<LootTable> extraLootTables = new ArrayList<>();
		
		public LoadLootTableExtrasEvent(ResourceLocation lootTableLocation, LootTableManager lootTableManager) {
			this.lootTableLocation = lootTableLocation;
			this.lootTableManager = lootTableManager;
		}
		
		public ResourceLocation getLootTableLocation() {
			return this.lootTableLocation;
		}
		
		public LootTableManager getLootTableManager() {
			return this.lootTableManager;
		}
		
		public void addExtraLootTable(LootTable lootTable) {
			this.extraLootTables.add(lootTable);
		}
		
	}

}
