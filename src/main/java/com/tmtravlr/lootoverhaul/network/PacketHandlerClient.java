package com.tmtravlr.lootoverhaul.network;

import java.io.IOException;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.recipes.ExtraRecipesManager;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Tmtravlr
 */
public class PacketHandlerClient implements IMessageHandler<SToCMessage, IMessage> {
	
	public static final int OPEN_TRADE_EDITOR = 0;
//	public static final int RECIPE_IDS = 1;
//	public static final int RECIPE_JSON = 2;

	//Types of packets

	public IMessage onMessage(SToCMessage packet, MessageContext context)
	{
		PacketBuffer buff = new PacketBuffer(Unpooled.wrappedBuffer(packet.getData()));

		int type = buff.readInt();

		switch(type) {
		case OPEN_TRADE_EDITOR: {
			LootOverhaul.proxy.callFromMainThread(() -> {
				int windowId = buff.readInt();
				int villagerEntityId = buff.readInt();
				NBTTagCompound villagerNBT;
				try {
					villagerNBT = buff.readCompoundTag();
				} catch (IOException e) {
					LootOverhaul.logger.catching(e);
					villagerNBT = new NBTTagCompound();
				}
				
				LootOverhaul.proxy.displayTradeEditor(windowId, villagerEntityId, villagerNBT);
			});
			break;
		}
//		case RECIPE_IDS: {
//			LootOverhaul.proxy.callFromMainThread(()->{ExtraRecipesManager.loadClientWorldRecipeIds(buff);});
//			break;
//		}
//		case RECIPE_JSON: {
//			LootOverhaul.proxy.callFromMainThread(()->{ExtraRecipesManager.loadClientWorldRecipe(buff);});
//			break;
//		}
		default:
			//Do nothing
		}

		return null;
	}
}
