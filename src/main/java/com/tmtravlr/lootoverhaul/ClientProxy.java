package com.tmtravlr.lootoverhaul;

import com.tmtravlr.lootoverhaul.items.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ClientProxy extends CommonProxy {

	private static final Minecraft MC = Minecraft.getMinecraft();
	
	@Override
	public void registerRenderers() {

		RenderItem renderer = MC.getRenderItem();
		
		renderer.getItemModelMesher().register(ItemLootEntity.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootEntity.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootBlock.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootBlock.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootFill.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootFill.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootItem.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootItem.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootStructure.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootStructure.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootCommand.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootCommand.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemLootEffect.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemLootEffect.INSTANCE), "inventory"));

		renderer.getItemModelMesher().register(ItemTriggerCommand.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemTriggerCommand.INSTANCE), "inventory"));
		renderer.getItemModelMesher().register(ItemTriggerLoot.INSTANCE, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(ItemTriggerLoot.INSTANCE), "inventory"));
	}
}
