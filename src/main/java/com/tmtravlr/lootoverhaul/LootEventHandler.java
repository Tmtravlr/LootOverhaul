package com.tmtravlr.lootoverhaul;

import java.util.List;
import java.util.Random;

import com.tmtravlr.lootoverhaul.asm.ObfuscatedNames;
import com.tmtravlr.lootoverhaul.items.ItemLoot;
import com.tmtravlr.lootoverhaul.items.ItemTradeEditor;
import com.tmtravlr.lootoverhaul.loot.BlockLootManager;
import com.tmtravlr.lootoverhaul.loot.LootContextExtendedBuilder;
import com.tmtravlr.lootoverhaul.loot.LootHelper;
import com.tmtravlr.lootoverhaul.misc.SavedData;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class LootEventHandler {
	
	private boolean loadingExtras = false;
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
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
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem) {
            ItemStack stack = ((EntityItem)event.getEntity()).getItem();
            
            Item item = stack.getItem();
            if (item instanceof ItemLoot) {
               ((ItemLoot)item).generateLoot(stack, event.getWorld(), event.getEntity().getPositionVector());
               event.getEntity().setDead();
               event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(LootOverhaul.MOD_ID)) {
            if (ConfigLoader.config != null && (ConfigLoader.config.hasChanged() || !ConfigLoader.config.getConfigFile().exists())) {
                ConfigLoader.syncConfig();
            }
        }
    }
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onBlockDrop(HarvestDropsEvent event) {
    	if (!(event.getWorld() instanceof WorldServer)) {
    		return;
    	}
    	
    	ResourceLocation blockLootResource = BlockLootManager.getBlockDropLootTable(event.getWorld(), event.getPos(), event.getState());
    	Random rand = BlockLootManager.getRandom(event.getWorld(), event.getPos(), event.getState());
    	
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
    public void onLivingDrops(LivingDropsEvent event) {
    	if (ConfigLoader.enableEntityDropsAll && event.getEntity().world instanceof WorldServer) {
	    	LootTable lootTable = event.getEntity().world.getLootTableManager().getLootTableFromLocation(LootHelper.ALL_ENTTIIES);
	    	
	    	if (lootTable != null) {
	    		LootContext.Builder builder = new LootContextExtendedBuilder((WorldServer)event.getEntity().world).withLootedEntity(event.getEntity()).withDamageSource(event.getSource());
	    		int recentlyHit = ObfuscationReflectionHelper.getPrivateValue(EntityLivingBase.class, event.getEntityLiving(), ObfuscatedNames.RECENTLY_HIT_FIELD_SRG, "recentlyHit");
	    		EntityPlayer attackingPlayer = ObfuscationReflectionHelper.getPrivateValue(EntityLivingBase.class, event.getEntityLiving(), ObfuscatedNames.ATTACKING_PLAYER_FIELD_SRG, "attackingPlayer");
	    		long deathLootTableSeed = 0L;
	    		if (event.getEntityLiving() instanceof EntityLiving) {
	    			deathLootTableSeed = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, (EntityLiving)event.getEntityLiving(), ObfuscatedNames.DEATH_LOOT_TABLE_SEED_FIELD_SRG, "deathLootTableSeed");
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
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onLootTableLoad(LootTableLoadEvent event) {
    	
    	LootTable replacement = ExtraFilesManager.loadExtraLootTable(event.getName(), event.getLootTableManager());
    	
    	if (replacement != null) {
    		event.setTable(replacement);
    	}
    	
    	if (ConfigLoader.enableExtraLootTables && !this.loadingExtras) {
    		this.loadingExtras = true;
	    	List<LootTable> extraTables = ExtraFilesManager.loadLootTableExtras(event.getName(), event.getLootTableManager());
	    	
	    	if (!extraTables.isEmpty()) {
	    		LootTable table = event.getTable();
	    		
	    		if (table != null) {
	    			for (LootTable extra : extraTables) {
	    				List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, extra, ObfuscatedNames.POOLS_FIELD_SRG, "pools");
	    				
	    				for (LootPool pool : pools) {
	    					table.addPool(pool);
	    				}
	    			}
	    		}
	    	}
	    	this.loadingExtras = false;
    	}
    	
    }
    
    @SubscribeEvent
    public void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
    	
    	if (event.getEntityPlayer().isCreative() && event.getTarget() instanceof EntityVillager) {
    		if (event.getItemStack().getItem() == ItemTradeEditor.INSTANCE) {
	    		event.setCancellationResult(EnumActionResult.SUCCESS);
	    		event.setCanceled(true);
	    		
	    		if (event.getEntityPlayer() instanceof EntityPlayerMP) {
	    			ItemTradeEditor.displayTradeEditorGui((EntityPlayerMP) event.getEntityPlayer(), (EntityVillager) event.getTarget(), event.getItemStack());
	    		}
    		} else if (event.getEntityPlayer().getHeldItem(EnumHand.OFF_HAND).getItem() == ItemTradeEditor.INSTANCE) {
    			event.setCancellationResult(EnumActionResult.PASS);
	    		event.setCanceled(true);
    		}
    	}
    }
    
//    @SubscribeEvent
//    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
//    	
//    	if (event.player instanceof EntityPlayerMP) {
//    		ExtraRecipesManager.sendWorldRecipesToClient((EntityPlayerMP) event.player);
//    	}
//    	
//    }

}
