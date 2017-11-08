package com.tmtravlr.lootoverhaul;

import com.tmtravlr.lootoverhaul.items.ItemFightStarter;
import com.tmtravlr.lootoverhaul.items.ItemLootBlock;
import com.tmtravlr.lootoverhaul.items.ItemLootCommand;
import com.tmtravlr.lootoverhaul.items.ItemLootEntity;
import com.tmtravlr.lootoverhaul.items.ItemLootFill;
import com.tmtravlr.lootoverhaul.items.ItemLootItem;
import com.tmtravlr.lootoverhaul.items.ItemLootStructure;
import com.tmtravlr.lootoverhaul.items.ItemNBTChecker;
import com.tmtravlr.lootoverhaul.items.ItemTradeEditor;
import com.tmtravlr.lootoverhaul.items.ItemTriggerCommand;
import com.tmtravlr.lootoverhaul.items.ItemTriggerLoot;
import com.tmtravlr.lootoverhaul.trade.GuiTradeEditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class ClientProxy extends CommonProxy {

	private static final Minecraft MC = Minecraft.getMinecraft();
	
	@Override
	public void registerRenderers() {

		RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
		
		renderer.getItemModelMesher().register(ItemLootEntity.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootEntity.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootBlock.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootBlock.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootFill.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootFill.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootItem.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootItem.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootStructure.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootStructure.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootCommand.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootCommand.INSTANCE), "inventory"));

		renderer.getItemModelMesher().register(ItemTriggerCommand.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemTriggerCommand.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemTriggerLoot.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemTriggerLoot.INSTANCE), "inventory"));

		renderer.getItemModelMesher().register(ItemTradeEditor.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemTradeEditor.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemNBTChecker.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemNBTChecker.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemFightStarter.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemFightStarter.INSTANCE), "inventory"));
	}
	
	@Override
	public void callFromMainThread(Runnable runnable) {
		MC.addScheduledTask(runnable);
	}
	
	@Override
	public void displayTradeEditor(int id, int villagerId, NBTTagCompound villagerTag) {
		MC.displayGuiScreen(new GuiTradeEditor(MC.player.inventory, villagerId, villagerTag));
		MC.player.openContainer.windowId = id;
	}
}
