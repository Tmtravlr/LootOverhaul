package com.tmtravlr.lootoverhaul.commands;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Represents a generic command sender, that can run commands.
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class CommandSenderGeneric implements ICommandSender {

	private String name;
	private World worldObj;
	private BlockPos position;
	private Vec3d vecPosition;
	
	public String lastMessage;
	
	public CommandSenderGeneric(String senderName, World world, BlockPos pos) {
		name = senderName;
		worldObj = world;
		position = pos;
		vecPosition = new Vec3d(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
	}
	
	public CommandSenderGeneric(String senderName, World world, Vec3d pos) {
		name = senderName;
		worldObj = world;
		position = new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
		vecPosition = pos;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public boolean canUseCommand(int permLevel, String commandName) {
		return true;
	}

	@Override
	public BlockPos getPosition() {
		return position;
	}

	@Override
	public Vec3d getPositionVector() {
		return vecPosition;
	}

	@Override
	public World getEntityWorld() {
		return worldObj;
	}

	@Override
	public Entity getCommandSenderEntity() {
		return null;
	}

	@Override
	public boolean sendCommandFeedback() {
		return false;
	}

	@Override
	public void setCommandStat(Type type, int amount) {

	}

	@Override
	public MinecraftServer getServer() {
		return this.worldObj.getMinecraftServer();
	}

}
