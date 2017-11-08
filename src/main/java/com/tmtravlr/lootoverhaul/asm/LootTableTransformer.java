package com.tmtravlr.lootoverhaul.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;

public class LootTableTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		
		if(transformedName.equals("net.minecraft.tileentity.TileEntityLockableLoot")) {
			return patchChestLoot(basicClass, name.equals(ObfuscatedNames.TILE_ENTITY_LOCKABLE_LOOT_CLASS));
		}
		if(transformedName.equals("net.minecraft.entity.item.EntityMinecartContainer")) {
			return patchMinecartLoot(basicClass, name.equals(ObfuscatedNames.ENTITY_MINECART_CONTAINER_CLASS));
		}
		if(transformedName.equals("net.minecraft.entity.projectile.EntityFishHook")) {
			return patchFishingLoot(basicClass, name.equals(ObfuscatedNames.ENTITY_FISH_HOOK_CLASS));
		}
		
		
		return basicClass;
	}
	
	public byte[] patchMinecartLoot(byte[] basicClass, boolean obfuscated) {
		
		FMLLog.log.info("[lootoverhaul] Patching minecart loot.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.MINECART_ADD_LOOT_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.MINECART_ADD_LOOT_METHOD_SRG : "addLoot", obfuscated ? "(L" + ObfuscatedNames.ENTITY_PLAYER_CLASS + ";)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find addLoot method! Aborting!");
			return basicClass;
		}
		
		//Find the type node to replace, that would have instantiated the loot context builder
		
		TypeInsnNode typeNode = ASMHelper.getTypeInsnNode(method, Opcodes.NEW, ASMHelper.srgObfuscated ? ObfuscatedNames.LOOT_CONTEXT_BUILDER_CLASS : "net/minecraft/world/storage/loot/LootContext$Builder");
		
		if(typeNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder instantiation! Aborting!");
			return basicClass;
		}
		
		//Replace it with a type node for the extended builder
		
		//ASMHelper.printUntilLabel(typeNode);
		TypeInsnNode newTypeNode = new TypeInsnNode(Opcodes.NEW, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder");

		method.instructions.insert(typeNode, newTypeNode);
		method.instructions.remove(typeNode);
		
		//Also find the init method for the builder
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKESPECIAL, "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder init method! Aborting!");
			return basicClass;
		}

		//Replace it with the init method for the extended context builder, and add the new information
		//In this case, add the player that openned the chest, as well as the tile entity
		
		InsnList instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLooter", obfuscated ? "(L" + ObfuscatedNames.ENTITY_CLASS + ";)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;" : "(Lnet/minecraft/entity/Entity;)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLootedEntity", obfuscated ? "(L" + ObfuscatedNames.ENTITY_CLASS + ";)L" + ObfuscatedNames.LOOT_CONTEXT_BUILDER_CLASS + ";" : "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/world/storage/loot/LootContext$Builder;", false));
		
		method.instructions.insert(methodNode, instructions);
		method.instructions.remove(methodNode);
		
		//FMLLog.log.info("[lootoverhaul] \n\nDeleted old instructions.\n");
		//ASMHelper.printUntilLabel(newTypeNode);
		
		FMLLog.log.info("[lootoverhaul] Finished patching minecart loot.");
		
		return ASMHelper.getClassBytes(classNode);
	}
	
	public byte[] patchFishingLoot(byte[] basicClass, boolean obfuscated) {
		FMLLog.log.info("[lootoverhaul] Patching fishing loot.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.HANDLE_HOOK_RETRACTION_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.HANDLE_HOOK_RETRACTION_METHOD_SRG : "handleHookRetraction", "()I");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find handleHookRetraction method! Aborting!");
			return basicClass;
		}
		
		//Find the type node to replace, that would have instantiated the loot context builder
		
		TypeInsnNode typeNode = ASMHelper.getTypeInsnNode(method, Opcodes.NEW, ASMHelper.srgObfuscated ? ObfuscatedNames.LOOT_CONTEXT_BUILDER_CLASS : "net/minecraft/world/storage/loot/LootContext$Builder");
		
		if(typeNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder instantiation! Aborting!");
			return basicClass;
		}
		
		//Replace it with a type node for the extended builder
		
		//ASMHelper.printUntilLabel(typeNode);
		TypeInsnNode newTypeNode = new TypeInsnNode(Opcodes.NEW, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder");

		method.instructions.insert(typeNode, newTypeNode);
		method.instructions.remove(typeNode);
		
		//Also find the init method for the builder
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKESPECIAL, "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder init method! Aborting!");
			return basicClass;
		}

		//Replace it with the init method for the extended context builder, and add the new information
		//In this case, add the player that openned the chest, as well as the tile entity
		
		InsnList instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, obfuscated ? ObfuscatedNames.ANGLER_FIELD : ASMHelper.srgObfuscated ? ObfuscatedNames.ANGLER_FIELD_SRG : "angler", obfuscated ? "L" + ObfuscatedNames.ENTITY_PLAYER_CLASS + ";" : "Lnet/minecraft/entity/player/EntityPlayer;"));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLooter", obfuscated ? "(L" + ObfuscatedNames.ENTITY_CLASS + ";)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;" : "(Lnet/minecraft/entity/Entity;)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLootedEntity", obfuscated ? "(L" + ObfuscatedNames.ENTITY_CLASS + ";)L" + ObfuscatedNames.LOOT_CONTEXT_BUILDER_CLASS + ";" : "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/world/storage/loot/LootContext$Builder;", false));
		
		method.instructions.insert(methodNode, instructions);
		method.instructions.remove(methodNode);
		
		//FMLLog.log.info("[lootoverhaul] \n\nDeleted old instructions.\n");
		//ASMHelper.printUntilLabel(newTypeNode);
		
		FMLLog.log.info("[lootoverhaul] Finished patching fishing loot.");
		
		return ASMHelper.getClassBytes(classNode);
	}
	
	public byte[] patchChestLoot(byte[] basicClass, boolean obfuscated) {
		
		FMLLog.log.info("[lootoverhaul] Patching chest loot.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.FILL_WITH_LOOT_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.FILL_WITH_LOOT_METHOD_SRG : "fillWithLoot", obfuscated ? "(L" + ObfuscatedNames.ENTITY_PLAYER_CLASS + ";)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find fillWithLoot method! Aborting!");
			return basicClass;
		}
		
		//Find the type node to replace, that would have instantiated the loot context builder
		
		TypeInsnNode typeNode = ASMHelper.getTypeInsnNode(method, Opcodes.NEW, ASMHelper.srgObfuscated ? ObfuscatedNames.LOOT_CONTEXT_BUILDER_CLASS : "net/minecraft/world/storage/loot/LootContext$Builder");
		
		if(typeNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder instantiation! Aborting!");
			return basicClass;
		}
		
		//Replace it with a type node for the extended builder
		
		//ASMHelper.printUntilLabel(typeNode);
		TypeInsnNode newTypeNode = new TypeInsnNode(Opcodes.NEW, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder");

		method.instructions.insert(typeNode, newTypeNode);
		method.instructions.remove(typeNode);
		
		//Also find the init method for the builder
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKESPECIAL, "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loot context builder init method! Aborting!");
			return basicClass;
		}

		//Replace it with the init method for the extended context builder, and add the new information
		//In this case, add the player that openned the chest, as well as the tile entity
		
		InsnList instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "<init>", obfuscated ? "(L" + ObfuscatedNames.WORLD_SERVER_CLASS + ";)V" : "(Lnet/minecraft/world/WorldServer;)V", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLooter", obfuscated ? "(L" + ObfuscatedNames.ENTITY_CLASS + ";)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;" : "(Lnet/minecraft/entity/Entity;)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;", false));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder", "withLootedTileEntity", obfuscated ? "(L" + ObfuscatedNames.TILE_ENTITY_CLASS + ";)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;" : "(Lnet/minecraft/tileentity/TileEntity;)Lcom/tmtravlr/lootoverhaul/loot/LootContextExtendedBuilder;", false));
		
		method.instructions.insert(methodNode, instructions);
		method.instructions.remove(methodNode);
		
		//FMLLog.log.info("[lootoverhaul] \n\nDeleted old instructions.\n");
		//ASMHelper.printUntilLabel(newTypeNode);
		
		FMLLog.log.info("[lootoverhaul] Finished patching chest loot.");
		
		return ASMHelper.getClassBytes(classNode);
	}

}
