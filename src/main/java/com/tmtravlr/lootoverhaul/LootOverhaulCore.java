package com.tmtravlr.lootoverhaul;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

public class LootOverhaulCore extends DummyModContainer {

	public LootOverhaulCore() {

		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "lootoverhaulcore";
		meta.name = "Loot Overhaul Core";
		meta.version = LootOverhaul.VERSION;
		meta.credits = "Made by Tmtravlr!";
		meta.authorList = Arrays.asList("Tmtravlr");
		meta.description = "Core mod portion for Loot Overhaul";
		meta.url = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";

	}

    @EventHandler
	public void modConstruction(FMLConstructionEvent evt){

	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
    
    
}
