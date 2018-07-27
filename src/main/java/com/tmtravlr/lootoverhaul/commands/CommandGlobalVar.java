package com.tmtravlr.lootoverhaul.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.utilities.SavedData;
import com.tmtravlr.lootoverhaul.utilities.SavedData.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandGlobalVar extends CommandBase {

	private static final Random RANDOM = new Random();
	
	@Override
	public String getName() {
		return "globalvar";
	}

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     */
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands."+this.getName()+".usage";
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1) {
			throw new WrongUsageException(this.getUsage(sender));
		}
		
		String varName = args[0];
		StoredVar var = SavedData.getSavedData(server.getEntityWorld()).getGlobalVar(varName);
		
		if(args.length == 1) {
			if(var != null) {
				notifyCommandListener(sender, this, var.toString());
				return;
			}
			throw new CommandException("commands.globalvar.failure.globalvar", varName);
		}
		
		//Must be at least two args at this point.
		
		//Invert the variable
		if(args[1].equalsIgnoreCase("invert")) {
			var.invert();
			notifyCommandListener(sender, this, "commands.globalvar.success.invert", varName);
		}
		//Set the variable
		else if(args[1].equalsIgnoreCase("set")) {
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.usage", "set");
			}
			
			StoredVar newVar = getOtherVar(server, sender, "set", Arrays.copyOfRange(args, 2, args.length));
			SavedData.getSavedData(server.getEntityWorld()).setGlobalVar(varName, newVar);
			notifyCommandListener(sender, this, "commands.globalvar.success.set", varName, newVar);
		}
		//Add the variables, if possible
		else if(args[1].equalsIgnoreCase("add")) {
			String operation = "add";
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.usage", operation);
			}
			
			StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 2, args.length));
			
			if(!var.add(other)) {
				throw new CommandException("commands.globalvar.failure.operation", operation, var.getType(), other.getType());
			}
			notifyCommandListener(sender, this, "commands.globalvar.success.add", other, varName);
		}
		//Multiply the variables, if possible
		else if(args[1].equalsIgnoreCase("multiply")) {
			String operation = "multiply";
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.usage", operation);
			}
			
			StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 2, args.length));
			
			if(!var.multiply(other)) {
				throw new CommandException("commands.globalvar.failure.operation", operation, var.getType(), other.getType());
			}
			notifyCommandListener(sender, this, "commands.globalvar.success.multiply", varName, other);
		}
		//Divide the variables, if possible
		else if(args[1].equalsIgnoreCase("divide")) {
			String operation = "divide";
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.usage", operation);
			}
			
			StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 2, args.length));
			
			if(!var.divide(other)) {
				throw new CommandException("commands.globalvar.failure.operation", operation, var.getType(), other.getType());
			}
			notifyCommandListener(sender, this, "commands.globalvar.success.divide", varName, other);
		}
		//Modulate the variables, if possible
		else if(args[1].equalsIgnoreCase("mod")) {
			String operation = "mod";
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.usage", operation);
			}
			
			StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 2, args.length));
			
			if(!var.mod(other)) {
				throw new CommandException("commands.globalvar.failure.operation", operation, var.getType(), other.getType());
			}
			notifyCommandListener(sender, this, "commands.globalvar.success.operation", "applied mod to", varName);
		}
		//Modulate the variables, if possible
		else if(args[1].equalsIgnoreCase("test")) {
			String operation = "test <operation>";
			if(args.length < 4) {
				throw new WrongUsageException("commands.globalvar.operation.usage", operation);
			}
			
			StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 3, args.length));
			
			if(!var.test(args[2], other)) {
				throw new CommandException("commands.globalvar.failure.test", var, args[2], other);
			}
			notifyCommandListener(sender, this, "commands.globalvar.success.test", var, args[2], other);
		}
		
	}
	
	public StoredVar getOtherVar(MinecraftServer server, ICommandSender sender, String operation, String[] args) throws CommandException {
		if(args.length < 1) {
			throw new WrongUsageException(this.getUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase("value")) {
			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.value.usage", operation);
			}
			
			String type = args[1];
			
			if(type.equalsIgnoreCase("string")) {
				return new StoredVarString(this.buildString(args, 2));
			}
			else if(type.equalsIgnoreCase("boolean")) {
				return new StoredVarBoolean(args[2].equals("true"));
			}
			else if(type.equalsIgnoreCase("integer")) {
				return new StoredVarInt(parseInt(args[2]));
			}
			else if(type.equalsIgnoreCase("float")) {
				return new StoredVarFloat((float)parseDouble(args[2]));
			}
		}
		else if(args[0].equalsIgnoreCase("random")) {
			if(args.length < 2) {
				throw new WrongUsageException("commands.globalvar.operation.random.usage", operation);
			}
			
			String type = args[1];
			
			if(type.equalsIgnoreCase("string")) {
				return new StoredVarString(args.length > 2 ? args[RANDOM.nextInt(args.length - 2) + 2] : "");
			}
			else if(type.equalsIgnoreCase("boolean")) {
				return new StoredVarBoolean(RANDOM.nextBoolean());
			}
			else if(type.equalsIgnoreCase("integer")) {
				int min = Integer.MIN_VALUE;
				int max = Integer.MAX_VALUE-1;
				
				if(args.length > 2) {
					min = parseInt(args[2]);
				}
				if(args.length > 3) {
					max = parseInt(args[3]);
				}
				
				if(max < min) {
					max = min;
				}
				
				return new StoredVarInt(RANDOM.nextInt(max - min + 1) + min);
			}
			else if(type.equalsIgnoreCase("float")) {
				float min = 1000000;
				float max = 1000000;
				
				if(args.length > 2) {
					min = (float)parseDouble(args[2]);
				}
				if(args.length > 3) {
					max = (float)parseDouble(args[3]);
				}
				
				if(max < min) {
					max = min;
				}
				return new StoredVarFloat(RANDOM.nextFloat() * (max - min) + min);
			}
		}
		else if(args[0].equalsIgnoreCase("globalvar")) {

			if(args.length < 2) {
				throw new WrongUsageException("commands.globalvar.operation.globalvar.usage", operation);
			}
			
			StoredVar var = SavedData.getSavedData(server.getEntityWorld()).getGlobalVar(args[1]);
			if(var == null) {
				throw new WrongUsageException("commands.globalvar.failure.globalvar", args[1]);
			}
			return var;
			
		}
		else if(args[0].equalsIgnoreCase("entityvar")) {

			if(args.length < 2) {
				throw new WrongUsageException("commands.globalvar.operation.entityvar.usage", operation);
			}
			
			Entity entity = getEntity(server, sender, args[1]);
			StoredVar var = SavedData.getEntityVar(entity, args[2]);
			if(var == null) {
				throw new WrongUsageException("commands.globalvar.failure.entityvar", args[2], entity.getName());
			}
			return var;
			
		}
		else if(args[0].equalsIgnoreCase("scoreboard")) {

			if(args.length < 3) {
				throw new WrongUsageException("commands.globalvar.operation.scoreboard.usage", operation);
			}
			
			String entityName = getEntityName(server, sender, args[1]); 
			
			Scoreboard scoreboard = server.getWorld(0).getScoreboard();
	        ScoreObjective scoreobjective = scoreboard.getObjective(args[2]);

	        if (scoreobjective == null) {
	            throw new CommandException("commands.scoreboard.objectiveNotFound", args[2]);
	        }
			
			if(scoreboard.entityHasObjective(entityName, scoreobjective)) {
				
				return new StoredVarInt(scoreboard.getOrCreateScore(entityName, scoreobjective).getScorePoints());
			}
		}
		
		throw new WrongUsageException(this.getUsage(sender), new Object[0]);
	}

	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SavedData.getSavedData(server.getEntityWorld()).getGlobalVarNames());
        }
        else if (args.length == 2) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"set", "invert", "add", "multiply", "divide", "mod", "test"});
        }
        else if(args.length == 3 && args[1].equalsIgnoreCase("test")) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"=", "!=", "<", ">", "<=", ">="});
        }
        else if(args.length == 3 || (args.length == 4 && args[1].equalsIgnoreCase("test"))) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"value", "random", "globalvar", "entityvar", "scoreboard"});
        }
        
        if(args.length >= 4) {
            //Value or Random
            if(args[args.length-2].equalsIgnoreCase("value") || args[args.length-2].equalsIgnoreCase("random")) {
            	return getListOfStringsMatchingLastWord(args, new String[]{"string", "boolean", "integer", "float"});
            }
            //Global variable
            else if(args[args.length-2].equalsIgnoreCase("globalvar")) {
            	return getListOfStringsMatchingLastWord(args, SavedData.getSavedData(server.getEntityWorld()).getGlobalVarNames());
            }
            //Entity variable
            else if(args[args.length-2].equalsIgnoreCase("entityvar")) {
            	return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }
            else if(args[args.length-3].equalsIgnoreCase("entityvar")) {
            	return getListOfStringsMatchingLastWord(args, getEntityVarNames(server, sender, args));
            }
            //Scoreboard objective
            else if(args[args.length-2].equalsIgnoreCase("scoreboard")) {
            	return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }
            else if(args[args.length-3].equalsIgnoreCase("scoreboard")) {
            	
            	//Get all the scoreboard objective names
            	Collection<ScoreObjective> scoreboardList = server.getWorld(0).getScoreboard().getScoreObjectives();
            	String[] objectiveNames = new String[scoreboardList.size()];
            	int i = 0;
            	Iterator<ScoreObjective> it = scoreboardList.iterator();
            	while(it.hasNext()) {
            		ScoreObjective objective = it.next();
            		objectiveNames[i] = objective.getName();
            		i++;
            	}
            	
            	return getListOfStringsMatchingLastWord(args, objectiveNames);
            }
        }
        
        return Collections.<String>emptyList();
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        for(int i = 0; i < args.length; i++) {
        	if(args[i].equals("entityvar") || args[i].equals("scoreboard")) {
        		return index == i + 1;
        	}
        }
    	
    	return false;
    }
	
	private String[] getEntityVarNames(MinecraftServer server, ICommandSender sender, String[] args) {
		if(args.length < 1) {
			String entityArg = args[args.length-2];
			try {
				Entity entity = getEntity(server, sender, entityArg);
				return SavedData.getEntityVarNames(entity);
			} catch (CommandException e) {}
		}
		
		return new String[0];
	}

}
