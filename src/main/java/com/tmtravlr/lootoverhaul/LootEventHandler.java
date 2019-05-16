package com.tmtravlr.lootoverhaul;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.io.Files;
import com.tmtravlr.lootoverhaul.items.ItemLoot;
import com.tmtravlr.lootoverhaul.items.ItemLootBlock;
import com.tmtravlr.lootoverhaul.items.ItemLootCommand;
import com.tmtravlr.lootoverhaul.items.ItemLootEffect;
import com.tmtravlr.lootoverhaul.items.ItemLootEntity;
import com.tmtravlr.lootoverhaul.items.ItemLootFill;
import com.tmtravlr.lootoverhaul.items.ItemLootItem;
import com.tmtravlr.lootoverhaul.items.ItemLootStructure;
import com.tmtravlr.lootoverhaul.items.ItemTriggerCommand;
import com.tmtravlr.lootoverhaul.items.ItemTriggerLoot;
import com.tmtravlr.lootoverhaul.loot.BlockLootManager;
import com.tmtravlr.lootoverhaul.loot.ExtraLootManager;
import com.tmtravlr.lootoverhaul.loot.ExtraLootManager.LoadLootTableExtrasEvent;
import com.tmtravlr.lootoverhaul.loot.LootContextExtendedBuilder;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.utilities.SavedData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = LootOverhaul.MOD_ID)
public class LootEventHandler {
	
	private static boolean loadingExtras = false;
	private static final Map<EntityPlayer, TileEntity> TILE_ENTITY_INTERACTIONS = new ConcurrentHashMap<>();
	private static final Map<EntityPlayer, Entity> ENTITY_INTERACTIONS = new ConcurrentHashMap<>();
	
	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		
		registry.register(ItemLootItem.INSTANCE);
		registry.register(ItemLootBlock.INSTANCE);
		registry.register(ItemLootFill.INSTANCE);
		registry.register(ItemLootEntity.INSTANCE);
		registry.register(ItemLootStructure.INSTANCE);
		registry.register(ItemLootCommand.INSTANCE);
		registry.register(ItemLootEffect.INSTANCE);
    	
		registry.register(ItemTriggerCommand.INSTANCE);
		registry.register(ItemTriggerLoot.INSTANCE);
	}
	
	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if (event.phase == Phase.START && event.side == Side.SERVER) {
			SavedData savedData = SavedData.getSavedData(event.world);
			List<ItemStack> expiredLoot = savedData.decrementLootDelays();
			
			for (ItemStack stack : expiredLoot) {
				if (stack.getItem() instanceof ItemLoot) {
					NBTTagCompound positionTag = stack.getTagCompound().getCompoundTag("Position");
					Vec3d position = new Vec3d(positionTag.getDouble("X"), positionTag.getDouble("Y"), positionTag.getDouble("Z"));
					
					((ItemLoot)stack.getItem()).generateLoot(stack, event.world, position);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem) {
            ItemStack stack = ((EntityItem)event.getEntity()).getItem();
            
            if (!stack.isEmpty() && stack.hasTagCompound() && stack.getItem() instanceof ItemLoot) {
               ((ItemLoot)stack.getItem()).generateLoot(stack, event.getWorld(), event.getEntity().getPositionVector());
               event.getEntity().setDead();
               event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(LootOverhaul.MOD_ID)) {
            if (ConfigLoader.config != null && (ConfigLoader.config.hasChanged() || !ConfigLoader.config.getConfigFile().exists())) {
                ConfigLoader.syncConfig();
            }
        }
    }
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockDrop(HarvestDropsEvent event) {
    	if (!(ConfigLoader.enableBlockDrops && event.getWorld() instanceof WorldServer)) {
    		return;
    	}
    	Random rand = BlockLootManager.getRandom(event.getWorld(), event.getPos(), event.getState());
    	
    	if (!ConfigLoader.useBlockDropWhitelist || ConfigLoader.blockDropsToReplace.contains(event.getState().getBlock().getRegistryName())) {
	    	ResourceLocation blockLootResource = BlockLootManager.getBlockDropLootTable(event.getWorld(), event.getPos(), event.getState());
	    	
	    	if (blockLootResource != null) {
		    	LootTable lootTable = event.getWorld().getLootTableManager().getLootTableFromLocation(blockLootResource);
		    	
		    	if (lootTable != null) {
			    	LootContextExtendedBuilder builder = new LootContextExtendedBuilder((WorldServer)event.getWorld()).withBrokenState(event.getState()).withFortune(event.getFortuneLevel()).withSilkTouch(event.isSilkTouching());
			    	
			    	if (event.getWorld().getTileEntity(event.getPos()) != null) {
			    		builder.withBrokenTileEntity(event.getWorld().getTileEntity(event.getPos()));
			    	}
			    	
			    	if (event.getHarvester() != null) {
			    		builder.withLooter(event.getHarvester()).withLuck(event.getHarvester().getLuck());
			    	}
			    	
			    	List<ItemStack> lootList = lootTable.generateLootForPools(rand, builder.build());
			    	
			    	if (!lootList.isEmpty()) {
			    		event.getDrops().clear();
			    		
				    	for (ItemStack stack : lootList) {
				    		if (!stack.isEmpty()) {
				                event.getDrops().add(stack);
				            }
				    	}
			    	}
		    	}
	    	}
    	}
    	
    	if (ConfigLoader.enableBlockDropsAll) {
	    	LootTable lootTable = event.getWorld().getLootTableManager().getLootTableFromLocation(BlockLootManager.ALL_BLOCKS);
	    	
	    	if (lootTable != null) {
		    	LootContextExtendedBuilder builder = new LootContextExtendedBuilder((WorldServer)event.getWorld()).withBrokenState(event.getState()).withFortune(event.getFortuneLevel()).withSilkTouch(event.isSilkTouching());
		    	
		    	if (event.getWorld().getTileEntity(event.getPos()) != null) {
		    		builder.withBrokenTileEntity(event.getWorld().getTileEntity(event.getPos()));
		    	}
		    	
		    	if (event.getHarvester() != null) {
		    		builder.withLooter(event.getHarvester()).withLuck(event.getHarvester().getLuck());
		    	}
		    	
		    	for (ItemStack stack : lootTable.generateLootForPools(rand, builder.build())) {
		    		if (!stack.isEmpty()) {
		                event.getDrops().add(stack);
		            }
		    	}
	    	}
    	}
    }
    
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onLivingDrops(LivingDropsEvent event) {
    	if (ConfigLoader.enableEntityDropsAll && event.getEntity().world instanceof WorldServer) {
	    	LootTable lootTable = event.getEntity().world.getLootTableManager().getLootTableFromLocation(LootHelper.ALL_ENTTIIES);
	    	
	    	if (lootTable != null) {
	    		LootContext.Builder builder = new LootContextExtendedBuilder((WorldServer)event.getEntity().world).withLootedEntity(event.getEntity()).withDamageSource(event.getSource());
	    		int recentlyHit = ObfuscationReflectionHelper.getPrivateValue(EntityLivingBase.class, event.getEntityLiving(), "field_70718_bc", "recentlyHit");
	    		EntityPlayer attackingPlayer = ObfuscationReflectionHelper.getPrivateValue(EntityLivingBase.class, event.getEntityLiving(), "field_70717_bb", "attackingPlayer");
	    		long deathLootTableSeed = 0L;
	    		if (event.getEntityLiving() instanceof EntityLiving) {
	    			deathLootTableSeed = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, (EntityLiving)event.getEntityLiving(), "field_184653_bB", "deathLootTableSeed");
	    		}

	            if (recentlyHit > 0 && attackingPlayer != null)
	            {
	                builder = builder.withPlayer(attackingPlayer).withLuck(attackingPlayer.getLuck());
	            }
		    	
		    	for (ItemStack stack : lootTable.generateLootForPools(deathLootTableSeed == 0L ? event.getEntityLiving().getRNG() : new Random(deathLootTableSeed), builder.build())) {
		    		if (!stack.isEmpty()) {
		    			EntityItem entityItem = new EntityItem(event.getEntity().world, event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, stack);
		    			entityItem.setDefaultPickupDelay();
		                event.getDrops().add(entityItem);
		            }
		    	}
	    	}
    	}
    }
    
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
    	if (!event.getEntityPlayer().world.isRemote) {
    		
			//Find the position
			Vec3d position;

			//Use the position of the block or entity if just interacted
			if (TILE_ENTITY_INTERACTIONS.containsKey(event.getEntityPlayer())) {
				position = new Vec3d(TILE_ENTITY_INTERACTIONS.get(event.getEntityPlayer()).getPos()).addVector(0.5, 0, 0.5);
			} else if (ENTITY_INTERACTIONS.containsKey(event.getEntityPlayer())) {
				position = ENTITY_INTERACTIONS.get(event.getEntityPlayer()).getPositionVector();
			} else {
				position = event.getEntityPlayer().getPositionVector();
			}
			
			//Generate loot from the entity or container just interacted with
			List<ItemStack> lootItemsToGenerate = new ArrayList<>();
			
    		for (int i = 0; i < event.getContainer().inventoryItemStacks.size(); i++) {
    			ItemStack stack = event.getContainer().inventoryItemStacks.get(i);
    			
				if (stack.getItem() instanceof ItemLoot) {
					lootItemsToGenerate.add(stack);
					event.getContainer().putStackInSlot(i, ItemStack.EMPTY);
				}
			}
    		
    		if (!lootItemsToGenerate.isEmpty()) {
    			event.getContainer().detectAndSendChanges();
    		}
    		
    		lootItemsToGenerate.forEach(stack -> ((ItemLoot)stack.getItem()).generateLoot(stack, event.getEntityPlayer().world, position));
    	}
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void recordRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    	if (!event.getWorld().isRemote) {
    		TileEntity tile = event.getWorld().getTileEntity(event.getPos());
    		if (tile != null) {
    			TILE_ENTITY_INTERACTIONS.put(event.getEntityPlayer(), tile);
    		}
    	}
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void recordRightClickEntity(PlayerInteractEvent.EntityInteract event) {
    	if (!event.getWorld().isRemote) {
    		ENTITY_INTERACTIONS.put(event.getEntityPlayer(), event.getTarget());
    	}
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
    	if (!TILE_ENTITY_INTERACTIONS.isEmpty()) {
    		TILE_ENTITY_INTERACTIONS.clear();
    	}
    	if (!ENTITY_INTERACTIONS.isEmpty()) {
    		ENTITY_INTERACTIONS.clear();
    	}
    }
    
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onLootTableLoad(LootTableLoadEvent event) {
    	
    	if (ConfigLoader.enableExtraLootTables && !loadingExtras && !ConfigLoader.extraLootTableBlacklist.contains(event.getName())) {
    		loadingExtras = true;
	    	List<LootTable> extraTables = ExtraLootManager.loadLootTableExtras(event.getName(), event.getLootTableManager());
	    	
	    	if (!extraTables.isEmpty()) {
	    		LootTable table = event.getTable();
	    		
	    		if (table != null) {
	    			for (LootTable extra : extraTables) {
	    				List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, extra, "field_186466_c", "pools");
	    				
	    				for (LootPool pool : pools) {
	    					table.addPool(pool);
	    				}
	    			}
	    		}
	    	}
	    	
	    	loadingExtras = false;
    	}
    	
    }
    
    @SubscribeEvent
    public static void onLootTableExtrasLoad(LoadLootTableExtrasEvent event) {
		ExtraLootManager.loadDefaultLootTableExtras(event);
    }

}
