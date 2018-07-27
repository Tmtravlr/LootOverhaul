package com.tmtravlr.lootoverhaul;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Class which handles ID file generation.
 * 
 * @author Rebeca Rey (Tmtravlr)
 * @Date June 2015
 */
public class ConfigIdFileGenerator {

	//Generate some files with useful info.
	public static void generateIDFiles() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		boolean singleplayer = server != null && server.isSinglePlayer();
		File itemIds = new File(ConfigLoader.idFolder, "Item IDs.txt");
		File blockIds = new File(ConfigLoader.idFolder, "Block IDs.txt");
		File biomeIds = new File(ConfigLoader.idFolder, "Biome IDs.txt");
		File dimensionIds = new File(ConfigLoader.configFolder, "Dimension IDs.txt");
		File entityIds = new File(ConfigLoader.idFolder, "Entity IDs.txt");
		File potionIds = new File(ConfigLoader.idFolder, "Potion IDs.txt");
		File potionTypeIds = new File(ConfigLoader.idFolder, "Potion Type IDs.txt");
		File enchantIds = new File(ConfigLoader.idFolder, "Enchantment IDs.txt");
		File oreDictionary = new File(ConfigLoader.idFolder, "Ore Dictionary Info.txt");

		try {
			PrintStream itemWriteStream = new PrintStream(itemIds);

			itemWriteStream.println("## Item IDs: in the form <string id> - <number id> ##");
			Item.REGISTRY.forEach(item -> {
				itemWriteStream.println(Item.REGISTRY.getNameForObject(item) + " - " + Item.getIdFromItem(item));
			});
			itemWriteStream.close();

			PrintStream blockWriteStream = new PrintStream(blockIds);

			blockWriteStream.println("## Block IDs: in the form <string id> - <number id> ##");
			Block.REGISTRY.forEach(block -> {
				blockWriteStream.println(Block.REGISTRY.getNameForObject(block) + " - " + Block.getIdFromBlock(block));
			});
			blockWriteStream.close();

			PrintStream entityWriteStream = new PrintStream(entityIds);

			entityWriteStream.println("## Entity IDs: in the form <string id> - <number id> ##");
			ForgeRegistries.ENTITIES.forEach(entity -> {
				entityWriteStream.println(ForgeRegistries.ENTITIES.getKey(entity) + " - " + EntityList.getID(entity.getEntityClass()));
			});
			entityWriteStream.close();

			PrintStream biomeWriteStream = new PrintStream(biomeIds);

			biomeWriteStream.println("## Biome IDs                                           ##");
			biomeWriteStream.println("#########################################################");
			biomeWriteStream.println("## In the form:                                        ##");
			biomeWriteStream.println("## <biome name>                                        ##");
			biomeWriteStream.println("##    - Id: <biome id>                                 ##");
			biomeWriteStream.println("##    - Height Base: <biome height base>               ##");
			biomeWriteStream.println("##    - Height Variation: <biome height variation>     ##");
			biomeWriteStream.println("##    - Humidity: <biome humidity>                     ##");
			biomeWriteStream.println("##    - Temperature: <biome temperature base>          ##");
			biomeWriteStream.println("##    - Forge Biome Types: <list of forge biome types> ##");
			biomeWriteStream.println("#########################################################");
			Biome.REGISTRY.forEach(biome -> {
				biomeWriteStream.println(Biome.REGISTRY.getNameForObject(biome));
				biomeWriteStream.println("   - Id: " + Biome.getIdForBiome(biome));
				biomeWriteStream.println("   - Height Base: " + biome.getBaseHeight());
				biomeWriteStream.println("   - Height Variation: " + biome.getHeightVariation());
				biomeWriteStream.println("   - Humidity: " + biome.getRainfall());
				biomeWriteStream.println("   - Temperature: " + biome.getDefaultTemperature());
				biomeWriteStream.print("   - Forge Biome Types: ");
				for(BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
					biomeWriteStream.print(type.getName() + " ");
				}
				biomeWriteStream.println();
				biomeWriteStream.println();
				biomeWriteStream.println();
			});
			biomeWriteStream.close();
			
			PrintStream dimensionWriteStream = new PrintStream(dimensionIds);

			dimensionWriteStream.println("## Dimension IDs                                                      ##");
			dimensionWriteStream.println("########################################################################");
			dimensionWriteStream.println("## In the form:                                                       ##");
			dimensionWriteStream.println("## <dimension name>                                                   ##");
			dimensionWriteStream.println("##    - Id: <dimension id>                                            ##");
			dimensionWriteStream.println("##    - Height: <dimension height>                                    ##");
			dimensionWriteStream.println("##    - Has Sky? <true if dimension has a sky>                        ##");
			dimensionWriteStream.println("##    - Cloud Height: <height of dimension's clouds (only on client)> ##");
			dimensionWriteStream.println("##    - Can Respawn Here? <true if players can respawn here>          ##");
			dimensionWriteStream.println("##    - Terrain Scale: <distance travelled per block (nether is 8)>   ##");
			dimensionWriteStream.println("########################################################################");
			
			if (server != null && server.worlds.length > 0) {
				for (WorldServer world : server.worlds) {
					dimensionWriteStream.println(world.provider.getDimensionType().getName());
					dimensionWriteStream.println("	- Id: " + world.provider.getDimension());
					dimensionWriteStream.println("	- Height: " + world.provider.getActualHeight());
					dimensionWriteStream.println("	- Has Sky? " + world.provider.hasSkyLight());
					if(singleplayer) {
						dimensionWriteStream.println("	- Cloud Height: " + world.provider.getCloudHeight());
					}
					dimensionWriteStream.println("	- Can Respawn Here? " + world.provider.canRespawnHere());
					dimensionWriteStream.println("	- Terrain Scale: " + world.provider.getMovementFactor());
					dimensionWriteStream.println();
					dimensionWriteStream.println();
				}
			}

			dimensionWriteStream.close();

			PrintStream potionWriteStream = new PrintStream(potionIds);

			potionWriteStream.println("## Potion IDs                                                          ##");
			potionWriteStream.println("#########################################################################");
			potionWriteStream.println("## In the form:                                                        ##");
			potionWriteStream.println("## <potion name>                                                       ##");
			potionWriteStream.println("##    - Id: <potion id>                                                ##");
			potionWriteStream.println("##    - Color: <potion color>                                          ##");
			potionWriteStream.println("##    - Is Instant: <true if an instant potion (like health)>          ##");
			potionWriteStream.println("##    - Is Bad: <true if considered a 'bad' potion>                    ##");
			potionWriteStream.println("##    - Is Beneficial: <true if considered 'beneficial' (client only)> ##");
			potionWriteStream.println("#########################################################################");
			Potion.REGISTRY.forEach(potion -> {
				potionWriteStream.println(Potion.REGISTRY.getNameForObject(potion));
				potionWriteStream.println("   - Id: " + Potion.getIdFromPotion(potion));
				potionWriteStream.println("   - Color: " + potion.getLiquidColor());
				potionWriteStream.println("   - Is Instant: " + potion.isInstant());
				potionWriteStream.println("   - Is Bad: " + potion.isBadEffect());
				if (singleplayer) {
					potionWriteStream.println("	- Is Beneficial: " + potion.isBeneficial());
				}
				potionWriteStream.println();
				potionWriteStream.println();
			});
			potionWriteStream.close();

			PrintStream potionTypeWriteStream = new PrintStream(potionTypeIds);

			potionTypeWriteStream.println("## Potion Type IDs                                                        ##");
			potionTypeWriteStream.println("############################################################################");
			potionTypeWriteStream.println("## In the form:                                                           ##");
			potionTypeWriteStream.println("## <potion type name>                                                     ##");
			potionTypeWriteStream.println("##    - Effects: <List of potion effects>                                 ##");
			potionTypeWriteStream.println("##        <name> - <duration> - <amplifier> - <ambient> - <showParticles> ##");
			potionTypeWriteStream.println("##        etc...                                                          ##");
			potionTypeWriteStream.println("############################################################################");
			PotionType.REGISTRY.forEach(potionType -> {
				potionTypeWriteStream.println(PotionType.REGISTRY.getNameForObject(potionType));
				potionTypeWriteStream.println("   - Effects: ");
				potionType.getEffects().forEach(effect -> {
					potionTypeWriteStream.println("       " + Potion.REGISTRY.getNameForObject(effect.getPotion()) + " - " + effect.getDuration() + " - " + effect.getAmplifier() + " - " + effect.getIsAmbient() + " - " + effect.doesShowParticles());
				});
				potionTypeWriteStream.println();
				potionTypeWriteStream.println();
			});
			potionTypeWriteStream.close();

			PrintStream enchantmentWriteStream = new PrintStream(enchantIds);

			enchantmentWriteStream.println("## Enchantment IDs                                   ##");
			enchantmentWriteStream.println("#######################################################");
			enchantmentWriteStream.println("## In the form:                                      ##");
			enchantmentWriteStream.println("## <enchantment name>                                ##");
			enchantmentWriteStream.println("##    - Id: <enchantment id>                         ##");
			enchantmentWriteStream.println("##    - Rarity: <enchantment rarity (weight)>        ##");
			enchantmentWriteStream.println("##    - Is Treasure: <can only get through treasure> ##");
			enchantmentWriteStream.println("##    - Min Level: <min xp level for enchantment>    ##");
			enchantmentWriteStream.println("##    - Max Level: <max xp level for enchantment>    ##");
			enchantmentWriteStream.println("#######################################################");
			Enchantment.REGISTRY.forEach(enchantment -> {
				enchantmentWriteStream.println(Enchantment.REGISTRY.getNameForObject(enchantment));
				enchantmentWriteStream.println("   - Id: " + Enchantment.getEnchantmentID(enchantment));
				enchantmentWriteStream.println("   - Rarity: " + enchantment.getRarity());
				enchantmentWriteStream.println("   - Treasure: " + enchantment.isTreasureEnchantment());
				enchantmentWriteStream.println("   - Min Level: " + enchantment.getMinLevel());
				enchantmentWriteStream.println("   - Max Level: " + enchantment.getMaxLevel());
				enchantmentWriteStream.println();
				enchantmentWriteStream.println();
			});
			enchantmentWriteStream.close();

			PrintStream oreDictWriteStream = new PrintStream(oreDictionary);
			
			oreDictWriteStream.println("## Ore Dictionary Info                      ##");
			oreDictWriteStream.println("##############################################");
			oreDictWriteStream.println("## In the form:                             ##");
			oreDictWriteStream.println("## <ore dictionary entry>                   ##");
			oreDictWriteStream.println("##     <item id registered> - <item damage> ##");
			oreDictWriteStream.println("##     etc...                               ##");
			oreDictWriteStream.println("##############################################");
			
			List<String> registeredEntries = Arrays.asList(OreDictionary.getOreNames());
			Collections.sort(registeredEntries);
			
			for(String entry : registeredEntries) {
				oreDictWriteStream.println(entry);
				for(ItemStack stack : OreDictionary.getOres(entry)) {
					oreDictWriteStream.println(Item.REGISTRY.getNameForObject(stack.getItem()) + " - " + (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? "any" : stack.getItemDamage()));
				}
				oreDictWriteStream.println();
				oreDictWriteStream.println();
			}
			
			oreDictWriteStream.close();


		} catch (IOException x) {
			LootOverhaul.logger.error("Caught IO Exception while trying to write out ID files", x);
		}
	}
}