package com.tmtravlr.lootoverhaul.loot;

import com.tmtravlr.lootoverhaul.ConfigLoader;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.fml.common.FMLLog;

public class LootContextExtendedBuilder extends Builder {

	private DamageSource source;
	
	private BlockPos pos;
    
	//Loot specific
	private Entity looter;
    private TileEntity lootedTileEntity;
    
    //Broken block specific
    private TileEntity brokenTileEntity;
    private IBlockState brokenState;
    private Integer fortune;
    private Boolean silkTouch;
	
	public LootContextExtendedBuilder(WorldServer worldIn) {
		super(worldIn);
	}

    public LootContext.Builder withDamageSource(DamageSource dmgSource)
    {
        this.source = dmgSource;
        return this;
    }

    public LootContextExtendedBuilder withPosition(BlockPos posIn) {
        this.pos = posIn;
        return this;
    }

    public LootContextExtendedBuilder withLooter(Entity entityIn) {
        this.looter = entityIn;
        return this;
    }

    public LootContextExtendedBuilder withLootedTileEntity(TileEntity tEntityIn) {
        this.lootedTileEntity = tEntityIn;
        return this;
    }

    public LootContextExtendedBuilder withBrokenTileEntity(TileEntity tEntityIn) {
        this.brokenTileEntity = tEntityIn;
        return this;
    }

    public LootContextExtendedBuilder withBrokenState(IBlockState stateIn) {
        this.brokenState = stateIn;
        return this;
    }
    
    public LootContextExtendedBuilder withFortune(int fortuneIn) {
    	this.fortune = fortuneIn;
    	return this;
    }
    
    public LootContextExtendedBuilder withSilkTouch(boolean silkTouchIn) {
    	this.silkTouch = silkTouchIn;
    	return this;
    }

    @Override
    public LootContext build()
    {
        return new LootContextExtended(super.build(), source, pos, looter, lootedTileEntity, brokenTileEntity, brokenState, fortune, silkTouch);
    }

}
