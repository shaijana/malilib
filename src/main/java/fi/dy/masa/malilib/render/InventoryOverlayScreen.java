package fi.dy.masa.malilib.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.mixin.entity.IMixinMerchantEntity;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.game.BlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtBlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtEntityUtils;
import fi.dy.masa.malilib.util.nbt.NbtKeys;

public class InventoryOverlayScreen extends Screen implements Drawable
{
    String modId;
    private InventoryOverlay.Context previewData;
    private final boolean shulkerBGColors;
    private final boolean villagerBGColors;
    private int ticks;

    public InventoryOverlayScreen(String modId, @Nullable InventoryOverlay.Context previewData)
    {
        this(modId, previewData, true, false);
    }

    public InventoryOverlayScreen(String modId, @Nullable InventoryOverlay.Context previewData, boolean shulkerBGColors)
    {
        this(modId, previewData, shulkerBGColors, false);
    }

    public InventoryOverlayScreen(String modId, @Nullable InventoryOverlay.Context previewData, boolean shulkerBGColors, boolean villagerBGColors)
    {
        super(StringUtils.translateAsText(MaLiLibReference.MOD_ID + ".gui.title.inventory_overlay", modId));
        this.modId = modId;
        this.previewData = previewData;
        this.shulkerBGColors = shulkerBGColors;
        this.villagerBGColors = villagerBGColors;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        // NO BLUR / MASKING
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        this.ticks++;
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = WorldUtils.getBestWorld(mc);

        if (this.previewData != null && world != null)
        {
            final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
            final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
            int x = xCenter - 52 / 2;
            int y = yCenter - 92;

            int startSlot = 0;
            int totalSlots = this.previewData.inv() == null ? 0 : this.previewData.inv().size();
            List<ItemStack> armourItems = new ArrayList<>();

            if (this.previewData.entity() instanceof AbstractHorseEntity)
            {
                if (this.previewData.inv() == null)
                {
                    MaLiLib.LOGGER.warn("InventoryOverlayScreen(): Horse inv() = null");
                    return;
                }
                armourItems.add(this.previewData.entity().getEquippedStack(EquipmentSlot.BODY));
                armourItems.add(this.previewData.inv().getStack(0));
                startSlot = 1;
                totalSlots = this.previewData.inv().size() - 1;
            }
            else if (this.previewData.entity() instanceof WolfEntity || this.previewData.entity() instanceof HappyGhastEntity)
            {
                armourItems.add(this.previewData.entity().getEquippedStack(EquipmentSlot.BODY));
                //armourItems.add(ItemStack.EMPTY);
            }

            final InventoryOverlay.InventoryRenderType type = (this.previewData.entity() instanceof VillagerEntity) ? InventoryOverlay.InventoryRenderType.VILLAGER : InventoryOverlay.getBestInventoryType(this.previewData.inv(), this.previewData.nbt() != null ? this.previewData.nbt() : new NbtCompound(), this.previewData);
            final InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, totalSlots);
            final int rows = (int) Math.ceil((double) totalSlots / props.slotsPerRow);
            Set<Integer> lockedSlots = new HashSet<>();
            int xInv = xCenter - (props.width / 2);
            int yInv = yCenter - props.height - 6;

            if (rows > 6)
            {
                yInv -= (rows - 6) * 18;
                y -= (rows - 6) * 18;
            }

            if (MaLiLibReference.DEBUG_MODE)
            {
                MaLiLib.LOGGER.warn("render():0: type [{}], previewData.type [{}], previewData.inv [{}], previewData.be [{}], previewData.ent [{}], previewData.nbt [{}]", type.toString(), this.previewData.type().toString(),
                                    this.previewData.inv() != null, this.previewData.be() != null, this.previewData.entity() != null, this.previewData.nbt() != null ? this.previewData.nbt().getString("id") : null);
                MaLiLib.LOGGER.error("0: -> inv.type [{}] // nbt.type [{}]", this.previewData.inv() != null ? InventoryOverlay.getInventoryType(this.previewData.inv()) : null, this.previewData.nbt() != null ? InventoryOverlay.getInventoryType(this.previewData.nbt()) : null);
                MaLiLib.LOGGER.error("1: -> inv.size [{}] // inv.isEmpty [{}]", this.previewData.inv() != null ? this.previewData.inv().size() : -1, this.previewData.inv() != null ? this.previewData.inv().isEmpty() : -1);
                MaLiLib.LOGGER.error("2: -> total slots [{}] // rows [{}] // startSlot [{}]", totalSlots, rows, startSlot);
            }

            if (this.previewData.entity() != null)
            {
                x = xCenter - 55;
                xInv = xCenter + 2;
                yInv = Math.min(yInv, yCenter - 92);
            }
            if (this.previewData.be() instanceof CrafterBlockEntity cbe)
            {
                lockedSlots = BlockUtils.getDisabledSlots(cbe);
            }
            else if (this.previewData.nbt() != null && this.previewData.nbt().contains(NbtKeys.DISABLED_SLOTS))
            {
                lockedSlots = NbtBlockUtils.getDisabledSlotsFromNbt(this.previewData.nbt());
            }

            if (!armourItems.isEmpty())
            {
                Inventory horseInv = new SimpleInventory(armourItems.toArray(new ItemStack[0]));
                InventoryOverlay.renderInventoryBackground(drawContext, type, xInv, yInv, 1, horseInv.size(), mc);
                InventoryOverlay.renderInventoryBackgroundSlots(drawContext, type, horseInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY);
                InventoryOverlay.renderInventoryStacks(drawContext, type, horseInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, 1, 0, horseInv.size(), mc, mouseX, mouseY);
                xInv += 32 + 4;
            }

            int color = -1;

            if (this.previewData.be() != null && this.previewData.be().getCachedState().getBlock() instanceof ShulkerBoxBlock sbb)
            {
                color = RenderUtils.setShulkerboxBackgroundTintColor(sbb, this.shulkerBGColors);
            }

            // Inv Display
            if (totalSlots > 0 && this.previewData.inv() != null)
            {
                InventoryOverlay.renderInventoryBackground(drawContext, type, xInv, yInv, props.slotsPerRow, totalSlots, color, mc);

                if (type == InventoryOverlay.InventoryRenderType.BREWING_STAND)
                {
                    InventoryOverlay.renderBrewerBackgroundSlots(drawContext, this.previewData.inv(), xInv, yInv);
                }

                InventoryOverlay.renderInventoryStacks(drawContext, type, this.previewData.inv(), xInv + props.slotOffsetX, yInv + props.slotOffsetY, props.slotsPerRow, startSlot, totalSlots, lockedSlots, mc, mouseX, mouseY);
            }

            // EnderItems Display
            if (this.previewData.type() == InventoryOverlay.InventoryRenderType.PLAYER &&
                this.previewData.nbt() != null && this.previewData.nbt().contains(NbtKeys.ENDER_ITEMS))
            {
                EnderChestInventory enderItems = InventoryUtils.getPlayerEnderItemsFromNbt(this.previewData.nbt(), world.getRegistryManager());

                if (enderItems == null)
                {
                    enderItems = new EnderChestInventory();
                }

                yInv = yCenter + 6;
                InventoryOverlay.renderInventoryBackground(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, xInv, yInv, 9, 27, color, mc);
                InventoryOverlay.renderInventoryStacks(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, enderItems, xInv + props.slotOffsetX, yInv + props.slotOffsetY, 9, 0, 27, mc, mouseX, mouseY);
            }
            // Player Inventory Display
            else if (this.previewData.entity() instanceof PlayerEntity player)
            {
                yInv = yCenter + 6;
                InventoryOverlay.renderInventoryBackground(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, xInv, yInv, 9, 27, color, mc);
                InventoryOverlay.renderInventoryStacks(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, player.getEnderChestInventory(), xInv + props.slotOffsetX, yInv + props.slotOffsetY, 9, 0, 27, mc, mouseX, mouseY);
            }

            // Villager Trades Display
            if (type == InventoryOverlay.InventoryRenderType.VILLAGER &&
                this.previewData.nbt() != null && this.previewData.nbt().contains(NbtKeys.OFFERS))
            {
                DefaultedList<ItemStack> offers = InventoryUtils.getSellingItemsFromNbt(this.previewData.nbt(), world.getRegistryManager());
                Inventory tradeOffers = InventoryUtils.getAsInventory(offers);

                if (tradeOffers != null && !tradeOffers.isEmpty())
                {
                    int xInvOffset = (xCenter - 55) - (props.width / 2);
                    int offerSlotCount = 9;

                    yInv = yCenter + 6;

                    // Realistically, this should never go above 9; but because Minecraft doesn't have these guard rails, be prepared for it.
                    if (offers.size() > 9)
                    {
                        offerSlotCount = 18;
                    }

                    color = RenderUtils.setVillagerBackgroundTintColor(NbtEntityUtils.getVillagerDataFromNbt(this.previewData.nbt()), this.villagerBGColors);
                    InventoryOverlay.renderInventoryBackground(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, xInvOffset - props.slotOffsetX, yInv, 9, offerSlotCount, color, mc);
                    InventoryOverlay.renderInventoryStacks(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, tradeOffers, xInvOffset, yInv + props.slotOffsetY, 9, 0, offerSlotCount, mc, mouseX, mouseY);
                }
            }
            // Villager Trades Display
            else if (this.previewData.entity() instanceof MerchantEntity merchant)
            {
                TradeOfferList trades = ((IMixinMerchantEntity) merchant).malilib_offers();
                DefaultedList<ItemStack> offers = trades != null ? InventoryUtils.getSellingItems(trades) : DefaultedList.of();
                Inventory tradeOffers = InventoryUtils.getAsInventory(offers);

                if (tradeOffers != null && !tradeOffers.isEmpty())
                {
                    int xInvOffset = (xCenter - 55) - (props.width / 2);
                    int offerSlotCount = 9;

                    yInv = yCenter + 6;

                    // Realistically, this should never go above 9; but because Minecraft doesn't have these guard rails, be prepared for it.
                    if (offers.size() > 9)
                    {
                        offerSlotCount = 18;
                    }

                    if (merchant instanceof VillagerEntity villager)
                    {
                        color = RenderUtils.setVillagerBackgroundTintColor(villager.getVillagerData(), this.villagerBGColors);
                    }

                    InventoryOverlay.renderInventoryBackground(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, xInvOffset - props.slotOffsetX, yInv, 9, offerSlotCount, color, mc);
                    InventoryOverlay.renderInventoryStacks(drawContext, InventoryOverlay.InventoryRenderType.GENERIC, tradeOffers, xInvOffset, yInv + props.slotOffsetY, 9, 0, offerSlotCount, mc, mouseX, mouseY);
                }
            }

            // Entity Display
            if (this.previewData.entity() != null)
            {
                InventoryOverlay.renderEquipmentOverlayBackground(drawContext, x, y, this.previewData.entity());
                InventoryOverlay.renderEquipmentStacks(drawContext, this.previewData.entity(), x, y, mc, mouseX, mouseY);
            }

            // Refresh
            if (this.ticks % 4 == 0)
            {
                this.previewData = this.previewData.handler().onContextRefresh(this.previewData, world);
            }
        }
    }

    @Override
    public boolean shouldPause()
    {
        return false;
    }
}
