package com.tmtravlr.lootoverhaul.loot;

import java.util.Random;

import net.minecraft.util.ResourceLocation;

public interface ITileEntityLootable {

	public Random getRNG();
	
	public ResourceLocation getDropLootTable();
	
}
