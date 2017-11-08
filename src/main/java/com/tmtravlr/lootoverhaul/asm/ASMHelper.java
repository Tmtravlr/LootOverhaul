package com.tmtravlr.lootoverhaul.asm;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.FunctionObject;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

public class ASMHelper {

	public static boolean srgObfuscated = !((Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment")).booleanValue();
	

	public static ClassNode getClassNode(byte[] classBytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(classBytes);
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		return classNode;
	}
	
	public static byte[] getClassBytes(ClassNode classNode)
	  {
	    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	    classNode.accept(writer);
	    
	    return writer.toByteArray();
	  }

	public static MethodNode getMethod(ClassNode classNode, String name, String desc) {
		
		for (MethodNode method : classNode.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return method;
			}
		}
		return null;
	}

	public static TypeInsnNode getTypeInsnNode(MethodNode method, int opcode, String desc) {
		
		Iterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			
			if(node.getOpcode() == opcode && node.getType() == AbstractInsnNode.TYPE_INSN && node instanceof TypeInsnNode) {
				TypeInsnNode typeNode = (TypeInsnNode) node;
				if(typeNode.desc.equals(desc)) {
					return typeNode;
				}
			}
		}
		return null;
	}

	public static MethodInsnNode getMethodInsnNode(MethodNode method, int opcode, String name, String desc) {
		
		Iterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			
			if(node.getOpcode() == opcode && node.getType() == AbstractInsnNode.METHOD_INSN && node instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if(methodNode.desc.equals(desc) && methodNode.name.equals(name)) {
					return methodNode;
				}
			}
		}
		return null;
	}
	
	public static void deleteUntilLabel(MethodNode method, AbstractInsnNode node) {
		while(node.getNext() != null) {
			
			if(node.getType() == AbstractInsnNode.LABEL) {
				break;
			}
			
			AbstractInsnNode nextNode = node.getNext();
			
			method.instructions.remove(node);
			
			node = nextNode;
			
		}
	}
	
	public static AbstractInsnNode printUntilLabel(AbstractInsnNode node) {
		while(node.getNext() != null) {
			
			FMLLog.info("[lootoverhaul] Type: " + node.getType() + ", Opcode: " + node.getOpcode());
			
			switch(node.getType()) {
			case AbstractInsnNode.INSN:
				FMLLog.info("[lootoverhaul]  -- Insn");
				break;
			case AbstractInsnNode.FIELD_INSN:
				FMLLog.info("[lootoverhaul]  -- Field; Owner: " + ((FieldInsnNode)node).owner + ", Name: " + ((FieldInsnNode)node).name + ", Desc:" + ((FieldInsnNode)node).desc);
				break;
			case AbstractInsnNode.VAR_INSN:
				FMLLog.info("[lootoverhaul]  -- Variable: " + ((VarInsnNode)node).var);
				break;
			case AbstractInsnNode.TYPE_INSN:
				FMLLog.info("[lootoverhaul]  -- Type:" + ((TypeInsnNode)node).desc);
				break;
			case AbstractInsnNode.METHOD_INSN:
				FMLLog.info("[lootoverhaul]  -- Method; Owner: " + ((MethodInsnNode)node).owner + ", Name: " + ((MethodInsnNode)node).name + ", Desc:" + ((MethodInsnNode)node).desc);
				break;
			case AbstractInsnNode.LABEL:
				FMLLog.info("[lootoverhaul]  -- Label: " + ((LabelNode)node).getLabel());
				break;
			case AbstractInsnNode.LINE:
				FMLLog.info("[lootoverhaul]  -- Line: " + ((LineNumberNode)node).line);
				break;
			default: FMLLog.info("[lootoverhaul]  -- Other");
			}
			
			if(node.getType() == AbstractInsnNode.LABEL) {
				return node;
			}
			
			node = node.getNext();
			
		}
		
		return null;
	}
	
	public static void printMethod(MethodNode method) {
		AbstractInsnNode label = method.instructions.getFirst();
		while (label != null) {
			int index = method.instructions.indexOf(label);
			if (index + 1 < method.instructions.size()) {
				label = printUntilLabel(method.instructions.get(index + 1));
			} else {
				label = null;
			}
		}
	}

//    protected ResourceLocation lootTable;
//    protected long lootTableSeed;
//    private World worldObj;
//    private TileEntityLockableLoot that;
//    
//	protected void fillWithLoot(@Nullable EntityPlayer player)
//    {
//        if (this.lootTable != null)
//        {
//            LootTable loottable = this.worldObj.getLootTableManager().getLootTableFromLocation(this.lootTable);
//            this.lootTable = null;
//            Random random;
//
//            if (this.lootTableSeed == 0L)
//            {
//                random = new Random();
//            }
//            else
//            {
//                random = new Random(this.lootTableSeed);
//            }
//
//            LootContext.Builder lootcontext$builder = new com.tmtravlr.lootoverhaul.loot.BuilderExtended((WorldServer)this.worldObj).withLootingPlayer(player).withLootedTileEntity(that);
//
//            if (player != null)
//            {
//                lootcontext$builder.withLuck(player.getLuck());
//            }
//
//            loottable.fillInventory(this.that, random, lootcontext$builder.build());
//        }
//    }
//	
//	public void doStuff() {
//        Map<ResourceLocation, Advancement.Builder> map = this.loadCustomAdvancements();
//        this.loadBuiltInAdvancements(com.tmtravlr.lootoverhaul.ExtraFilesManager.loadExtraAdvancements(map));
//	}
//	
//	private Map<ResourceLocation, Advancement.Builder> loadCustomAdvancements() {
//		return null;
//	}
//	
//	private void loadBuiltInAdvancements(Map map) {
//		
//	}
//	
//	private File baseFolder;
//	
//	public boolean readTemplate(ResourceLocation server)
//    {
//        String s = server.getResourcePath();
//        File file1 = com.tmtravlr.lootoverhaul.ExtraFilesManager.getStructureFile(new File(this.baseFolder, s + ".nbt"), server);
//
//        if (!file1.exists())
//        {
//            return false;
//        }
//        
//        return false;
//    }
	
	private Map<ResourceLocation, FunctionObject> functions;
	private FunctionManager _this;
	
	private void loadFunctions() {
		if (!com.tmtravlr.lootoverhaul.ExtraFilesManager.loadExtraFunctions(this.functions, _this).isEmpty())
        {
			System.out.println("Yay");
        }
	}
}
