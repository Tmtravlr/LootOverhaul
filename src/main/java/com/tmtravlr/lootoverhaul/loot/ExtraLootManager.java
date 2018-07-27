package com.tmtravlr.lootoverhaul.loot;

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
import com.tmtravlr.lootoverhaul.LootOverhaul;

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
		ArrayList<LootTable> lootTables = new ArrayList<>();
		File folder = ObfuscationReflectionHelper.getPrivateValue(LootTableManager.class, lootTableManager, "field_186528_d", "baseFolder");

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

		return lootTables;
	}

}
