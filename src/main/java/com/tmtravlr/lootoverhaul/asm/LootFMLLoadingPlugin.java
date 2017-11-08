package com.tmtravlr.lootoverhaul.asm;

import java.util.Map;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.LootOverhaulCore;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class LootFMLLoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String getModContainerClass() {
		return LootOverhaulCore.class.getName();
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{LootTableTransformer.class.getName(), ExtraFilesTransformer.class.getName()};
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}

}
