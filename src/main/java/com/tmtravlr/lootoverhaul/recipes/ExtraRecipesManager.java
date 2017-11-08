package com.tmtravlr.lootoverhaul.recipes;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.network.PacketHandlerClient;
import com.tmtravlr.lootoverhaul.network.SToCMessage;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.ForgeRegistry;

public class ExtraRecipesManager {

	private static final Gson RECIPE_GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public static void loadRecipesFromFolder(File folder) {
		if (!folder.isDirectory()) {
			return;
		}
		
		for (File subFolder : folder.listFiles()) {
			if (!subFolder.isDirectory()) {
				continue;
			}
			String modid = subFolder.getName();
			JsonContext ctx = new JsonContext(modid);

			File constantsFile = new File(subFolder, "_constants.json");

			if (constantsFile.exists()) {
				BufferedReader reader = null;
				try {
					String constantsJson = Files.toString(constantsFile, StandardCharsets.UTF_8);
					ctx = getContextFromConstantsJson(modid, constantsJson);
				} catch (Exception e) {
					LootOverhaul.logger.error("Error loading _constants.json: ", e);
					continue;
				}
			}

			Path subFolderPath = subFolder.toPath();
			try {
				Iterator<Path> recipePathIterator = java.nio.file.Files.walk(subFolderPath).iterator();

				while (recipePathIterator.hasNext()) {
					Path recipePath = recipePathIterator.next();

					String relative = subFolderPath.relativize(recipePath).toString();
					if (!"json".equals(FilenameUtils.getExtension(recipePath.toString())) || relative.startsWith("_")) {
						continue;
					}

					String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
					ResourceLocation recipeId = new ResourceLocation(ctx.getModId(), name);

					try {
						File recipeFile = recipePath.toFile();
						String recipeJson = Files.toString(recipeFile, StandardCharsets.UTF_8);
						IRecipe recipe = readAndRegisterRecipeFromJson(recipeId, recipeJson, ctx);
						if (recipe == null) {
							continue;
						}
					} catch (JsonParseException e) {
						FMLLog.log.error("Parsing error loading recipe {}", recipeId, e);
					} catch (Exception e) {
						FMLLog.log.error("Couldn't read recipe {} from {}", recipeId, recipePath, e);
					}
				}
			} catch (Exception e) {
				FMLLog.log.error("Couldn't read recipes from {}", subFolderPath, e);
			}
		}
	}
	
// TODO: Re-look at overriding and 'world' recipes in 1.13!
//	public static void sendWorldRecipesToClient(EntityPlayerMP player) {
//		if (!(player.isServerWorld() && player.world.getMinecraftServer().isDedicatedServer())) {
//			return;
//		}
//		
//		SToCMessage packet;
//		
//		//Send recipe ids
//		PacketBuffer idsOut = new PacketBuffer(Unpooled.buffer());
//		
//		idsOut.writeInt(PacketHandlerClient.RECIPE_IDS);
//		idsOut.writeInt(worldRecipesJson.keySet().size());
//		worldRecipesJson.keySet().forEach(location -> idsOut.writeString(location.toString()));
//		
//		packet = new SToCMessage(idsOut);
//		LootOverhaul.networkWrapper.sendTo(packet, player);
//		
//		//Send recipes and recipe constants
//		//Not super efficient to send the constants file with each recipe... but I can't see another good way to be sure it is loaded
//		for (Map.Entry<ResourceLocation, String> entry : worldRecipesJson.entries()) {
//			String recipeConstants = worldConstantsJson.get(entry.getKey().getResourceDomain());
//			
//			if (recipeConstants == null) {
//				recipeConstants = "[]";
//			}
//			
//			PacketBuffer constantOut = new PacketBuffer(Unpooled.buffer());
//			
//			constantOut.writeInt(PacketHandlerClient.RECIPE_JSON);
//			constantOut.writeString(entry.getKey().toString());
//			constantOut.writeString(recipeConstants);
//			constantOut.writeString(entry.getValue());
//			
//			packet = new SToCMessage(constantOut);
//			LootOverhaul.networkWrapper.sendTo(packet, player);
//		}
//	}
//	
//	public static void loadClientWorldRecipeIds(PacketBuffer in) {
//		List<ResourceLocation> newRecipeIds = new ArrayList<>();
//		int newIdsSize = in.readInt();
//		
//		for (int i = 0; i < newIdsSize; i++) {
//			newRecipeIds.add(new ResourceLocation(in.readString(Integer.MAX_VALUE)));
//		}
//		
//		resetOrRemoveRecipes(newRecipeIds);
//	}
//	
//	public static void loadClientWorldRecipe(PacketBuffer in) {
//		ResourceLocation recipeId = new ResourceLocation(in.readString(Integer.MAX_VALUE));
//		String constantsJson = in.readString(Integer.MAX_VALUE);
//		String recipeJson = in.readString(Integer.MAX_VALUE);
//		
//		JsonContext ctx = getContextFromConstantsJson(recipeId.getResourceDomain(), constantsJson);
//		readAndRegisterRecipeFromJson(recipeId, recipeJson, ctx);
//	}
	
	private static JsonContext getContextFromConstantsJson(String modid, String constantsJson) {
		JsonContext ctx = new JsonContext(modid);
		JsonObject[] json = RECIPE_GSON.fromJson(constantsJson, JsonObject[].class);
		Method method = ReflectionHelper.findMethod(JsonContext.class, "loadConstants", null, JsonObject[].class);
		try {
			method.invoke(ctx, (Object[])json);
		} catch (Exception e) {
			LootOverhaul.logger.warn("Unable to get recipe constants for mod " + modid, e);
		}
		return ctx;
	}
	
	private static IRecipe readAndRegisterRecipeFromJson(ResourceLocation recipeId, String recipeJson, JsonContext ctx) {
		JsonObject json = RECIPE_GSON.fromJson(recipeJson, JsonObject.class);
		
		if (json.has("conditions") && !CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx)) {
			return null;
		}
		
		IRecipe recipe;
		
		try {
			recipe = CraftingHelper.getRecipe(json, ctx);
		} catch (Exception e) {
			LootOverhaul.logger.warn("Unable to register recipe " + recipeId + ". Aborting.", e);
			return null;
		}
		
		forceRegisterRecipe(recipeId, recipe);
		
		return recipe;
	}
	
//	private static void resetOrRemoveRecipes(List<ResourceLocation> newRecipeIds) {
//		for (ResourceLocation recipeId : ForgeRegistries.RECIPES.getKeys()) {
//			if (!newRecipeIds.contains(recipeId)) {
//				if (originalRecipes.containsKey(recipeId)) {
//					forceRegisterRecipe(recipeId, originalRecipes.get(recipeId));
//				} else {
//					forceRegisterRecipe(recipeId, new EmptyRecipe());
//				}
//			}
//		}
//	}
	
	private static void forceRegisterRecipe(ResourceLocation recipeId, IRecipe recipe) {
		if (recipe == null || recipeId == null) {
			return;
		}
		
		//Apparently overriding recipes does not play nicely at all with the recipe book...
		if (ForgeRegistries.RECIPES.containsKey(recipeId)) {
			LootOverhaul.logger.warn("Tried to register recipe with id '" + recipeId + "', but there was already a recipe with that id. Aborting, since the vanilla recipe book would crash.");
			return;
		}
		
		ModContainer activeMod = Loader.instance().activeModContainer();
		
		ModContainer recipeMod = Loader.instance().getIndexedModList().get(recipeId.getResourceDomain());
		if (recipeMod == null) {
			recipeMod = Loader.instance().getMinecraftModContainer();
		}
		
		Loader.instance().setActiveModContainer(recipeMod);
		
//		if (ForgeRegistries.RECIPES instanceof ForgeRegistry) {
//			ForgeRegistry recipesRegistry = ((ForgeRegistry)ForgeRegistries.RECIPES);
//			
//			//Cheeky I know...
//			boolean isFrozen = recipesRegistry.isLocked();
//			if (isFrozen) {
//				recipesRegistry.unfreeze();
//			}
//			
//			//Avoids problems with the recipe book
//			IRecipe oldRecipe = ForgeRegistries.RECIPES.getValue(recipeId);
//			int oldRecipeId = CraftingManager.getIDForRecipe(oldRecipe);
//			IntIdentityHashBiMap<IRecipe> craftingRegistryIntegerMap = ObfuscationReflectionHelper.getPrivateValue(RegistryNamespaced.class, CraftingManager.REGISTRY, "field_148759_a", "underlyingIntegerMap");
//			
//			if (craftingRegistryIntegerMap != null)
			
			ForgeRegistries.RECIPES.register(recipe.setRegistryName(recipeId));
			
//			if (isFrozen) {
//				recipesRegistry.freeze();
//			}
//			
//			LootOverhaul.proxy.rebuildRecipeBookTable();
//		}
		
		Loader.instance().setActiveModContainer(activeMod);
	}
	
}
