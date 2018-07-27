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

public class CommandEntityVar extends CommandBase {

	private static final Random RANDOM = new Random();
	
	@Override
	public String getName() {
		return "entityvar";
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
		if(args.length < 2) {
			throw new WrongUsageException(this.getUsage(sender));
		}
		
		List<Entity> entities = getEntityList(server, sender, args[0]);
		
		if (entities.isEmpty()) {
			throw new CommandException("commands.generic.entity.notFound", args[0]);
		}
		
		String varName = args[1];
		
		if(args.length == 2) {
			boolean foundEntity = false;
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				if(var != null) {
					notifyCommandListener(sender, this, entity.getName() + ": " + var.toString());
					foundEntity = true;
				}
			}
			if (foundEntity) {
				return;
			} else {
				throw new CommandException("commands.entityvar.failure", varName, args[0]);
			}
		}
		
		//Must be at least two args at this point.
		
		//Invert the variable
		if(args[2].equalsIgnoreCase("invert")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				var.invert();
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.invert", varName);
		}
		//Set the variable
		else if(args[2].equalsIgnoreCase("set")) {
			for (Entity entity : entities) {
				if(args.length < 4) {
					throw new WrongUsageException("commands.entityvar.operation.usage", "set");
				}
				
				StoredVar newVar = getOtherVar(server, sender, "set", Arrays.copyOfRange(args, 3, args.length));
				SavedData.setEntityVar(entity, varName, newVar);
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.set" + (entities.size() == 1 ? ".singular" : ""), varName, entities.size());
		}
		//Add the variables, if possible
		else if(args[2].equalsIgnoreCase("add")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				String operation = "add";
				if(args.length < 4) {
					throw new WrongUsageException("commands.entityvar.operation.usage", operation);
				}
				
				StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 3, args.length));
				
				if(!var.add(other)) {
					throw new CommandException("commands.entityvar.failure.operation", operation, var.getType(), other.getType(), entity.getName());
				}
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.add" + (entities.size() == 1 ? ".singular" : ""), varName, entities.size());
		}
		//Multiply the variables, if possible
		else if(args[2].equalsIgnoreCase("multiply")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				String operation = "multiply";
				if(args.length < 4) {
					throw new WrongUsageException("commands.entityvar.operation.usage", operation);
				}
				
				StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 3, args.length));
				
				if(!var.multiply(other)) {
					throw new CommandException("commands.entityvar.failure.operation", operation, var.getType(), other.getType(), entity.getName());
				}
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.multiply" + (entities.size() == 1 ? ".singular" : ""), varName, entities.size());
		}
		//Divide the variables, if possible
		else if(args[2].equalsIgnoreCase("divide")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				String operation = "divide";
				if(args.length < 4) {
					throw new WrongUsageException("commands.entityvar.operation.usage", operation);
				}
				
				StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 3, args.length));
				
				if(!var.divide(other)) {
					throw new CommandException("commands.entityvar.failure.operation", operation, var.getType(), other.getType(), entity.getName());
				}
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.divide" + (entities.size() == 1 ? ".singular" : ""), varName, entities.size());
		}
		//Modulate the variables, if possible
		else if(args[2].equalsIgnoreCase("mod")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				String operation = "mod";
				if(args.length < 4) {
					throw new WrongUsageException("commands.entityvar.operation.usage", operation);
				}
				
				StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 3, args.length));
				
				if(!var.mod(other)) {
					throw new CommandException("commands.entityvar.failure.operation", operation, var.getType(), other.getType(), entity.getName());
				}
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.mod" + (entities.size() == 1 ? ".singular" : ""), varName, entities.size());
		}
		//Modulate the variables, if possible
		else if(args[2].equalsIgnoreCase("test")) {
			for (Entity entity : entities) {
				StoredVar var = SavedData.getEntityVar(entity, varName);
				String operation = "test <operation>";
				if(args.length < 5) {
					throw new WrongUsageException("commands.entityvar.operation.usage", operation);
				}
				
				StoredVar other = getOtherVar(server, sender, operation, Arrays.copyOfRange(args, 4, args.length));
				
				if(!var.test(args[3], other)) {
					throw new CommandException("commands.entityvar.failure.test", var, args[3], other, entity.getName());
				}
			}
			notifyCommandListener(sender, this, "commands.entityvar.success.test" + (entities.size() == 1 ? ".singular" : ""), args[3], entities.size());
		}
		
	}
	
	public StoredVar getOtherVar(MinecraftServer server, ICommandSender sender, String operation, String[] args) throws CommandException {
		if(args.length < 1) {
			throw new WrongUsageException(this.getUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase("value")) {
			if(args.length < 3) {
				throw new WrongUsageException("commands.entityvar.operation.value.usage", operation);
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
				throw new WrongUsageException("commands.entityvar.operation.random.usage", operation);
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
				throw new WrongUsageException("commands.entityvar.operation.globalvar.usage", operation);
			}
			
			StoredVar var = SavedData.getSavedData(server.getEntityWorld()).getGlobalVar(args[1]);
			if(var == null) {
				throw new WrongUsageException("commands.entityvar.failure.globalvar", args[1]);
			}
			return var;
			
		}
		else if(args[0].equalsIgnoreCase("entityvar")) {

			if(args.length < 3) {
				throw new WrongUsageException("commands.entityvar.operation.entityvar.usage", operation);
			}
			
			Entity entity = getEntity(server, sender, args[1]);
			StoredVar var = SavedData.getEntityVar(entity, args[2]);
			if(var == null) {
				throw new WrongUsageException("commands.entityvar.failure.entityvar", args[2], entity.getName());
			}
			return var;
			
		}
		else if(args[0].equalsIgnoreCase("scoreboard")) {

			if(args.length < 3) {
				throw new WrongUsageException("commands.entityvar.operation.scoreboard.usage", operation);
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
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, this.getEntityVarNames(server, sender, args));
        }
        else if (args.length == 3) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"set", "invert", "add", "multiply", "divide", "mod", "test"});
        }
        else if(args.length == 4 && args[2].equalsIgnoreCase("test")) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"=", "!=", "<", ">", "<=", ">="});
        }
        else if(args.length == 4 || (args.length == 5 && args[2].equalsIgnoreCase("test"))) {
        	return getListOfStringsMatchingLastWord(args, new String[]{"value", "random", "globalvar", "entityvar", "scoreboard"});
        }
        
        if(args.length >= 5) {
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
    	if (index == 0) {
    		return true;
    	}
    	
        for (int i = 0; i < args.length; i++) {
        	if(args[i].equals("entityvar") || args[i].equals("scoreboard")) {
        		return index == i + 1;
        	}
        }
    	
    	return false;
    }
	
	private String[] getEntityVarNames(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length >= 2) {
			String entityArg = args[args.length - 2];
			try {
				Entity entity = getEntity(server, sender, entityArg);
				return SavedData.getEntityVarNames(entity);
			} catch (CommandException e) {
				LootOverhaul.logger.catching(e);
			}
		}
		
		return new String[0];
	}

}
