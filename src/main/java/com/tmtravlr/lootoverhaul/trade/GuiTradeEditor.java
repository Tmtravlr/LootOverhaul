package com.tmtravlr.lootoverhaul.trade;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.network.CToSMessage;
import com.tmtravlr.lootoverhaul.network.PacketHandlerServer;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Gui for the trade editor item, to edit villager trades
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2017
 */
@SideOnly(Side.CLIENT)
public class GuiTradeEditor extends GuiContainer {

	private static final Random RAND = new Random();
	
    private static final ResourceLocation TRADE_EDITOR_GUI_TEXTURE = new ResourceLocation(LootOverhaul.MOD_ID, "textures/gui/container/trade_editor.png");
    private static final String INFINITE_TRADES_TAG = "InfiniteTrades";
    
	private int villagerId;
    private MerchantRecipeList recipesEditing;
    private int selectedRecipe;
	private String originalVillagerName;
	private boolean originalInvulnerable;
	private boolean originalInfiniteTrades;
    
    private GuiTextField name;
    private MerchantCheckbox invulnerableCheckbox;
    private MerchantCheckbox infiniteTradesCheckbox;
    private MerchantButton nextButton;
    private MerchantButton previousButton;
	
	public GuiTradeEditor(InventoryPlayer inventory, int villagerId, NBTTagCompound villagerTag) {
		super(new ContainerTradeEditor(inventory));
		this.villagerId = villagerId;
		this.originalVillagerName = villagerTag.getCompoundTag("ForgeData").getString("VillagerDisplayName");
		this.originalInvulnerable = villagerTag.getBoolean("Invulnerable");
		this.originalInfiniteTrades = villagerTag.getCompoundTag("ForgeData").getBoolean(INFINITE_TRADES_TAG);
		this.recipesEditing = new MerchantRecipeList(villagerTag.getCompoundTag("Offers"));
		if (this.recipesEditing.isEmpty()) {
			this.recipesEditing.add(new MerchantRecipe(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
		}
	}
	
	@Override
    public void initGui() {
        super.initGui();
        
        Keyboard.enableRepeatEvents(true);
        
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        
        this.nextButton = (MerchantButton)this.addButton(new MerchantButton(1, i + 147, j + 51, true));
        this.previousButton = (MerchantButton)this.addButton(new MerchantButton(2, i + 17, j + 51, false));
        this.nextButton.enabled = true;
        this.previousButton.enabled = false;
        
        boolean wasInvulnerable = originalInvulnerable;
        if (this.invulnerableCheckbox != null) {
        	wasInvulnerable = this.invulnerableCheckbox.checked;
        }
        this.invulnerableCheckbox = (MerchantCheckbox)this.addButton(new MerchantCheckbox(3, i + 14, j + 24));
        this.invulnerableCheckbox.checked = wasInvulnerable;
        
        boolean wasInfiniteTrades = originalInfiniteTrades;
        if (this.infiniteTradesCheckbox != null) {
        	wasInfiniteTrades = this.infiniteTradesCheckbox.checked;
        }
        this.infiniteTradesCheckbox = (MerchantCheckbox)this.addButton(new MerchantCheckbox(4, i + 14, j + 37));
        this.infiniteTradesCheckbox.checked = wasInfiniteTrades;
        
        String oldName = this.originalVillagerName;
        if (this.name != null) {
        	oldName = this.name.getText();
        }
        this.name = new GuiTextField(0, this.fontRenderer, i + 16, j + 10, 140, 10);
        this.name.setTextColor(-1);
        this.name.setDisabledTextColour(-1);
        this.name.setEnableBackgroundDrawing(false);
        this.name.setMaxStringLength(35);
        this.name.setText(oldName);
    }
	
	@Override
	public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.updateRecipesAndSendToServer();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.trade_editor.invulnerable"), 29, 26, 4210752);
        this.fontRenderer.drawString(I18n.format("gui.trade_editor.infiniteTrades"), 29, 38, 4210752);
        this.fontRenderer.drawString(String.valueOf(this.selectedRecipe), 160, 57, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TRADE_EDITOR_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (!recipesEditing.isEmpty()) {
            int i = (this.width - this.xSize) / 2;
            int j = (this.height - this.ySize) / 2;
            int k = this.selectedRecipe;
            MerchantRecipe recipe = recipesEditing.get(k);
            
            ItemStack buyStack1 = recipe.getItemToBuy();
            ItemStack buyStack2 = recipe.getSecondItemToBuy();
            ItemStack sellStack = recipe.getItemToSell();
            
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();

            if (!buyStack1.isEmpty()) {
	            this.itemRender.renderItemAndEffectIntoGUI(buyStack1, i + 36, j + 53);
	            this.itemRender.renderItemOverlays(this.fontRenderer, buyStack1, i + 36, j + 53);
            }

            if (!buyStack2.isEmpty()) {
                this.itemRender.renderItemAndEffectIntoGUI(buyStack2, i + 62, j + 53);
                this.itemRender.renderItemOverlays(this.fontRenderer, buyStack2, i + 62, j + 53);
            }

            if (!sellStack.isEmpty()) {
	            this.itemRender.renderItemAndEffectIntoGUI(sellStack, i + 120, j + 53);
	            this.itemRender.renderItemOverlays(this.fontRenderer, sellStack, i + 120, j + 53);
            }
            
            GlStateManager.disableLighting();

            if (this.isPointInRegion(36, 54, 16, 16, mouseX, mouseY) && !buyStack1.isEmpty()) {
                this.renderToolTip(buyStack1, mouseX, mouseY);
            } else if (!buyStack2.isEmpty() && this.isPointInRegion(62, 54, 16, 16, mouseX, mouseY) && !buyStack2.isEmpty()) {
                this.renderToolTip(buyStack2, mouseX, mouseY);
            } else if (!sellStack.isEmpty() && this.isPointInRegion(120, 54, 16, 16, mouseX, mouseY) && !sellStack.isEmpty()) {
                this.renderToolTip(sellStack, mouseX, mouseY);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.name.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        this.previousButton.enabled = this.selectedRecipe > 0;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
    	super.actionPerformed(button);
    	
        if (button == this.nextButton) {
            ++this.selectedRecipe;

            if (this.selectedRecipe >= recipesEditing.size()) {
                this.selectedRecipe = recipesEditing.size();
                
                recipesEditing.add(new MerchantRecipe(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, 0, this.infiniteTradesCheckbox.checked ? 1000000000 : RAND.nextInt(11) + 2));
            }
        } else if (button == this.previousButton) {
            --this.selectedRecipe;

            if (this.selectedRecipe < 0)
            {
                this.selectedRecipe = 0;
            }
        } else if (button == this.invulnerableCheckbox) {
        	this.invulnerableCheckbox.toggleCheckbox();
        	
        } else if (button == this.infiniteTradesCheckbox) {
        	this.infiniteTradesCheckbox.toggleCheckbox();
        	if (!this.recipesEditing.isEmpty()) {
	        	for (int i = 0; i < this.recipesEditing.size(); i++) {
	        		int maxUses = this.infiniteTradesCheckbox.checked ? 1000000000 : RAND.nextInt(11) + 2;
	        		
	        		MerchantRecipe recipe = this.recipesEditing.get(i);
	        		this.recipesEditing.set(i, new MerchantRecipe(recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell(), 0, maxUses));
	        	}
        	}
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.name.textboxKeyTyped(typedChar, keyCode)) {
        	super.keyTyped(typedChar, keyCode);
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
    	super.mouseReleased(mouseX, mouseY, state);
    	
    	boolean hoverBuy1 = this.isPointInRegion(36, 53, 16, 16, mouseX, mouseY);
    	boolean hoverBuy2 = this.isPointInRegion(62, 53, 16, 16, mouseX, mouseY);
    	boolean hoverSell = this.isPointInRegion(116, 50, 24, 24, mouseX, mouseY);
    	
    	if (hoverBuy1 || hoverBuy2 || hoverSell) {
	        ItemStack droppingStack = this.mc.player.inventory.getItemStack();
	        
        	MerchantRecipe recipe = this.recipesEditing.get(this.selectedRecipe);
	        ItemStack buyStack1 = recipe.getItemToBuy();
	        ItemStack buyStack2 = recipe.getSecondItemToBuy();
	        ItemStack sellStack = recipe.getItemToSell();
        	
	        if (hoverBuy1) {
	        	buyStack1 = droppingStack.copy();
	        } else if (hoverBuy2) {
	        	buyStack2 = droppingStack.copy();
	        } else if (hoverSell) {
	        	sellStack = droppingStack.copy();
	        }
	        
	        this.recipesEditing.set(this.selectedRecipe, new MerchantRecipe(buyStack1, buyStack2, sellStack, recipe.getToolUses(), recipe.getMaxTradeUses()));
    	}
    }
    
    private void updateRecipesAndSendToServer() {
    	
    	NBTTagCompound villagerTag = new NBTTagCompound();
    	this.updateVillagerName(villagerTag);
    	this.updateVillagerInvulnerable(villagerTag);
    	this.updateVillagerInfiniteTrades(villagerTag);
    	this.updateVillagerTrades(villagerTag);
    	
    	UUID playerUUID = this.mc.player.getUniqueID();
    	
		//Update the villager
    	PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
    	buff.writeInt(PacketHandlerServer.UPDATE_VILLAGER_TRADES);
    	buff.writeInt(this.mc.world.provider.getDimension());
    	buff.writeInt(this.villagerId);
    	buff.writeLong(playerUUID.getMostSignificantBits());
    	buff.writeLong(playerUUID.getLeastSignificantBits());
    	buff.writeCompoundTag(villagerTag);
    	
		CToSMessage packet = new CToSMessage(buff);
		LootOverhaul.networkWrapper.sendToServer(packet);
    }
    
    /**
     * Update the custom name on the tag to send to the server.
     */
    private void updateVillagerName(NBTTagCompound villagerTag) {
    	if (this.name.getText().equals(originalVillagerName)) {
    		villagerTag.removeTag("CustomName");
    	} else {
    		villagerTag.setString("CustomName", this.name.getText());
    	}
    }
    
    /**
     * Update the invulnerable tag on the tag to send to the server.
     */
    private void updateVillagerInvulnerable(NBTTagCompound villagerTag) {
    	villagerTag.setBoolean("Invulnerable", this.invulnerableCheckbox.checked);
    }
    
    /**
     * Update the infinite trades tag on the tag to send to the server.
     */
    private void updateVillagerInfiniteTrades(NBTTagCompound villagerTag) {
		NBTTagCompound forgeData = villagerTag.getCompoundTag("ForgeData");
		forgeData.setBoolean(INFINITE_TRADES_TAG, this.infiniteTradesCheckbox.checked);
		villagerTag.setTag("ForgeData", forgeData);
    }
    
    /**
     * Update the villager's trades on the tag to send to the server.
     */
    private void updateVillagerTrades(NBTTagCompound villagerTag) {
    	MerchantRecipeList validRecipes = new MerchantRecipeList();
    	for (MerchantRecipe recipe : this.recipesEditing) {
    		if (!recipe.getItemToSell().isEmpty() && (!recipe.getItemToBuy().isEmpty() || !recipe.getSecondItemToBuy().isEmpty())) {
    			validRecipes.add(recipe);
    		}
    	}
    	villagerTag.setInteger("CareerLevel", 1000);
    	villagerTag.setTag("Offers", validRecipes.getRecipiesAsTags());
    }

    @SideOnly(Side.CLIENT)
    static class MerchantButton extends GuiButton {
        private final boolean forward;

        public MerchantButton(int buttonID, int x, int y, boolean forward) {
            super(buttonID, x, y, 12, 19, "");
            this.forward = forward;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                mc.getTextureManager().bindTexture(TRADE_EDITOR_GUI_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                int i = 0;
                int j = 176;

                if (!this.enabled)
                {
                    j += this.width * 2;
                }
                else if (flag)
                {
                    j += this.width;
                }

                if (!this.forward)
                {
                    i += this.height;
                }

                this.drawTexturedModalRect(this.x, this.y, j, i, this.width, this.height);
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    static class MerchantCheckbox extends GuiButton {
    	
    	public boolean checked;

        public MerchantCheckbox(int buttonID, int x, int y) {
            super(buttonID, x, y, 10, 10, "");
            this.checked = false;
        }
        
        public void toggleCheckbox() {
        	this.checked = !this.checked;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                mc.getTextureManager().bindTexture(TRADE_EDITOR_GUI_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int startX = 176;
                int startY = 38;

                if (this.checked) {
                	startX += this.width;
                }

                this.drawTexturedModalRect(this.x, this.y, startX, startY, this.width, this.height);
            }
        }
    }

}
