package com.tmtravlr.lootoverhaul.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.tmtravlr.lootoverhaul.LootOverhaul;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;

public class ExtraFilesTransformer implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		
		if (transformedName.equals("net.minecraft.advancements.AdvancementManager")) {
			return patchAdvancmentLoading(basicClass, name.equals(ObfuscatedNames.ADVANCEMENT_MANAGER_CLASS));
		}
		
		if (transformedName.equals("net.minecraft.world.gen.structure.template.TemplateManager")) {
			return patchStructureLoading(basicClass, name.equals(ObfuscatedNames.TEMPLATE_MANAGER_CLASS));
		}
		
		if (transformedName.equals("net.minecraft.advancements.FunctionManager")) {
			return patchFunctionLoading(basicClass, name.equals(ObfuscatedNames.FUNCTION_MANAGER_CLASS));
		}
		
		return basicClass;
	}
	
	private byte[] patchAdvancmentLoading(byte[] basicClass, boolean obfuscated) {
		
		FMLLog.log.info("[lootoverhaul] Patching advancement loader.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.ADVANCEMENT_MANAGER_RELOAD_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.ADVANCEMENT_MANAGER_RELOAD_METHOD_SRG : "reload", "()V");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find AdvancementManager reload method! Aborting!");
			return basicClass;
		}
		
		//Find the method node to add the new code after
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKESPECIAL, obfuscated ? ObfuscatedNames.LOAD_BUILT_IN_ADVANCEMENTS_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.LOAD_BUILT_IN_ADVANCEMENTS_METHOD_SRG : "loadBuiltInAdvancements", "(Ljava/util/Map;)V");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find built in advancements node! Aborting!");
			return basicClass;
		}
		
		//Add the new loading method
		
		method.instructions.insertBefore(methodNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tmtravlr/lootoverhaul/ExtraFilesManager", "loadExtraAdvancements", "(Ljava/util/Map;)Ljava/util/Map;", false));
		
		FMLLog.log.info("[lootoverhaul] Finished patching advancement loader.");
		
		return ASMHelper.getClassBytes(classNode);
	}
	
	private byte[] patchStructureLoading(byte[] basicClass, boolean obfuscated) {
		
		FMLLog.log.info("[lootoverhaul] Patching structure loader.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.READ_TEMPLATE_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.READ_TEMPLATE_METHOD_SRG : "readTemplate", obfuscated ? "(L" + ObfuscatedNames.RESOURCE_LOCATION_CLASS + ";)Z" : "(Lnet/minecraft/util/ResourceLocation;)Z");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find readTemplate method! Aborting!");
			return basicClass;
		}
		
		//Find the method node to add the new code after
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKESPECIAL, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find structure file init node! Aborting!");
			return basicClass;
		}
		
		//Add the new loading method
		
		InsnList instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tmtravlr/lootoverhaul/ExtraFilesManager", "getStructureFile", obfuscated ? "(Ljava/io/File;L" + ObfuscatedNames.RESOURCE_LOCATION_CLASS + ";)Ljava/io/File;" : "(Ljava/io/File;Lnet/minecraft/util/ResourceLocation;)Ljava/io/File;", false));
		
		method.instructions.insert(methodNode, instructions);
		
		FMLLog.log.info("[lootoverhaul] Finished patching structure loader.");
		
		return ASMHelper.getClassBytes(classNode);
	}
	
	private byte[] patchFunctionLoading(byte[] basicClass, boolean obfuscated) {
		
		FMLLog.log.info("[lootoverhaul] Patching function loader.");
		
		ClassNode classNode = ASMHelper.getClassNode(basicClass);
		MethodNode method = ASMHelper.getMethod(classNode, obfuscated ? ObfuscatedNames.LOAD_FUNCTIONS_METHOD : ASMHelper.srgObfuscated ? ObfuscatedNames.LOAD_FUNCTIONS_METHOD_SRG : "loadFunctions", "()V");
		
		if(method == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find loadFunctions method! Aborting!");
			return basicClass;
		}
		
		//Find the method node to add the new code before
		
		MethodInsnNode methodNode = ASMHelper.getMethodInsnNode(method, Opcodes.INVOKEINTERFACE, "isEmpty", "()Z");
		
		if(methodNode == null) {
			FMLLog.log.warn("[lootoverhaul] Couldn't find structure file init node! Aborting!");
			return basicClass;
		}
		
		//Add the new loading method
		
		InsnList instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tmtravlr/lootoverhaul/ExtraFilesManager", "loadExtraFunctions", obfuscated ? "(Ljava/util/Map;L" + ObfuscatedNames.FUNCTION_MANAGER_CLASS + ";)Ljava/util/Map;" : "(Ljava/util/Map;Lnet/minecraft/advancements/FunctionManager;)Ljava/util/Map;", false));
				
		method.instructions.insertBefore(methodNode, instructions);
		
		FMLLog.log.info("[lootoverhaul] Finished patching function loader.");
		
		return ASMHelper.getClassBytes(classNode);
	}
	
}
