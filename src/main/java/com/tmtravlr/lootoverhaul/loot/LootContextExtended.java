package com.tmtravlr.lootoverhaul.loot;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;

/**
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
public class LootContextExtended extends LootContext {

	private BlockPos pos;
	private DamageSource damageSource;
	
	//Loot specific
	private Entity looter;
	private EntityPlayer looterPlayer;
    private TileEntity lootedTileEntity;
    
    //Broken block specific
    private TileEntity brokenTileEntity;
    private IBlockState brokenState;
    private int fortune;
    private boolean silkTouch;
    //TODO: Block destroyed by explosion? Maybe as a loot condition?
	
    public LootContextExtended(LootContext previous, DamageSource sourceIn, BlockPos posIn, Entity looterIn, TileEntity lootedTileEntityIn, 
    		TileEntity brokenTileEntityIn, IBlockState brokenStateIn, Integer fortuneIn, Boolean silkTouchIn) {
    	
    	super(previous.getLuck(), previous.getWorld(), previous.getLootTableManager(), previous.getLootedEntity(), 
    			previous.getKillerPlayer() instanceof EntityPlayer ? (EntityPlayer)previous.getKillerPlayer() : null, sourceIn);
    	
    	this.damageSource = sourceIn;
    	this.looter = looterIn != null ? looterIn : previous.getKiller() != null ? previous.getKiller() : previous.getKillerPlayer();
    	this.looterPlayer = (this.looter instanceof EntityPlayer) ? (EntityPlayer) this.looter : (previous.getKillerPlayer() instanceof EntityPlayer) ? (EntityPlayer)previous.getKillerPlayer() : null;
    	this.lootedTileEntity = lootedTileEntityIn;
    	this.brokenTileEntity = brokenTileEntityIn;
    	this.brokenState = brokenStateIn;
    	this.fortune = fortuneIn != null ? fortuneIn : (this.looter != null  && (this.looter instanceof EntityLivingBase)) ? EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, ((EntityLivingBase)looter).getHeldItemMainhand()) : 0;
    	this.silkTouch = silkTouchIn != null ? silkTouchIn : (this.looter != null  && (this.looter instanceof EntityLivingBase)) ? EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, ((EntityLivingBase)looter).getHeldItemMainhand()) > 0 : false;
    	this.pos = posIn != null ? posIn : this.looter != null ? this.looter.getPosition() : this.lootedTileEntity != null ? this.lootedTileEntity.getPos() : this.brokenTileEntity != null ? this.brokenTileEntity.getPos() : null;
    }
    
    @Nullable
    public BlockPos getPosition() {
    	if(this.pos != null) {
    		return pos;
    	}
    	else if(this.getLootedEntity() != null) {
    		return this.getLootedEntity().getPosition();
    	}
    	else if(this.lootedTileEntity != null) {
    		return this.lootedTileEntity.getPos();
    	}
    	else if(this.brokenTileEntity != null) {
    		return this.brokenTileEntity.getPos();
    	}
    	
    	return null;
    }
    
    @Nullable
    public DamageSource getDamageSource() {
    	return this.damageSource;
    }
    
    //Loot
    
    @Nullable
    public Entity getLooter() {
    	return this.looter;
    }
    
    @Nullable
    public EntityPlayer getLooterPlayer() {
    	return this.looterPlayer;
    }
    
    @Nullable
    public TileEntity getLootedTileEntity() {
    	return this.lootedTileEntity;
    }
    
    //Broken Block
    
    @Nullable
    public TileEntity getBrokenTileEntity() {
    	return this.brokenTileEntity;
    }
    
    @Nullable
    public IBlockState getBrokenBlockState() {
    	return this.brokenState;
    }
    
    public int getFortuneModifier() {
    	return this.fortune;
    }
    
    public boolean isSilkTouch() {
    	return this.silkTouch;
    }
    
    //Extended Entity Target
    
    @Nullable
    public Entity getEntityExtended(ExtendedEntityTarget target)
    {
        switch (target)
        {
            case THIS:
                return this.getLootedEntity();
            case KILLER:
                return this.getKiller();
            case KILLER_PLAYER:
                return this.getKillerPlayer();
            case LOOTER:
                return this.getLooter();
            default:
                return null;
        }
    }
    
    public static enum ExtendedEntityTarget {
    	THIS(EntityTarget.THIS),
        KILLER(EntityTarget.KILLER),
        KILLER_PLAYER(EntityTarget.KILLER_PLAYER),
        LOOTER(EntityTarget.KILLER, EntityTarget.KILLER_PLAYER);

        private final EntityTarget[] oldTargetTypes;

        private ExtendedEntityTarget(EntityTarget ... oldTargetTypes) {
            this.oldTargetTypes = oldTargetTypes;
        }
        
        public EntityTarget[] getEntityTargets() {
        	return this.oldTargetTypes;
        }
    }

}
