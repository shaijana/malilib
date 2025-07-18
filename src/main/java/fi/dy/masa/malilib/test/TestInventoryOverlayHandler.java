package fi.dy.masa.malilib.test;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.interfaces.IDataSyncer;
import fi.dy.masa.malilib.interfaces.IInventoryOverlayHandler;
import fi.dy.masa.malilib.mixin.entity.IMixinAbstractHorseEntity;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.game.RayTraceUtils;
import fi.dy.masa.malilib.util.nbt.NbtBlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtKeys;
import fi.dy.masa.malilib.util.nbt.NbtView;

@ApiStatus.Experimental
public class TestInventoryOverlayHandler implements IInventoryOverlayHandler
{
    private static final TestInventoryOverlayHandler INSTANCE = new TestInventoryOverlayHandler();

    public static TestInventoryOverlayHandler getInstance() { return INSTANCE; }

    IDataSyncer syncer;
    InventoryOverlay.Context context;
    InventoryOverlay.Refresher refresher;

    //private Pair<BlockPos, InventoryOverlay.Context> lastBlockEntityContext;
    //private Pair<Integer,  InventoryOverlay.Context> lastEntityContext;

    public TestInventoryOverlayHandler()
    {
        //this.lastBlockEntityContext = null;
        //this.lastEntityContext = null;
        this.context = null;
        this.refresher = null;
        this.syncer = null;
    }

    @Override
    public String getModId()
    {
        return MaLiLibReference.MOD_ID;
    }

    @Override
    public IDataSyncer getDataSyncer()
    {
        if (this.syncer == null)
        {
            this.syncer = TestDataSyncer.getInstance();
        }

        return this.syncer;
    }

    @Override
    public void setDataSyncer(IDataSyncer syncer)
    {
        this.syncer = syncer;
    }

    @Override
    public InventoryOverlay.Refresher getRefreshHandler()
    {
        if (this.refresher == null)
        {
            this.refresher = new Refresher();
        }

        return this.refresher;
    }

    @Override
    public boolean isEmpty()
    {
        return this.context == null;
    }

    @Override
    public @Nullable InventoryOverlay.Context getRenderContextNullable()
    {
        return this.context;
    }


    @Override
    public @Nullable InventoryOverlay.Context getRenderContext(DrawContext drawContext, Profiler profiler, MinecraftClient mc)
    {
        profiler.push(this.getClass().getName() + "_inventory_overlay");
        this.getTargetInventory(mc);

        if (!this.isEmpty())
        {
            if (MaLiLibConfigs.Test.TEST_INVENTORY_OVERLAY_OG.getBooleanValue())
            {
                // Tweakeroo style
                TestRenderHandler.renderInventoryOverlayOG(drawContext, this.getRenderContextNullable(), mc);
            }
            else
            {
                // MiniHUD Style
                this.renderInventoryOverlay(drawContext, this.getRenderContextNullable(), mc,
                                            true,
                                            true);
            }
        }

        profiler.pop();

        return this.getRenderContextNullable();
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventory(MinecraftClient mc)
    {
        World world = WorldUtils.getBestWorld(mc);
        Entity cameraEntity = EntityUtils.getCameraEntity();
        this.context = null;

        if (mc.player == null || world == null || mc.world == null)
        {
            return null;
        }

        if (cameraEntity == mc.player && world instanceof ServerWorld)
        {
            // We need to get the player from the server world (if available, ie. in single player),
            // so that the player itself won't be included in the ray trace
            Entity serverPlayer = world.getPlayerByUuid(mc.player.getUuid());

            if (serverPlayer != null)
            {
                cameraEntity = serverPlayer;
            }
        }

        if (cameraEntity == null)
        {
            return null;
        }

//        HitResult trace = TestRayTraceUtils.getRayTraceFromEntity(world, cameraEntity, false);
        HitResult trace;

        if (cameraEntity != mc.player)
        {
            trace = RayTraceUtils.getRayTraceFromEntity(mc.world, cameraEntity, RaycastContext.FluidHandling.NONE);
        }
        else
        {
            trace = mc.crosshairTarget;
        }

        NbtCompound nbt = new NbtCompound();

        if (trace == null || trace.getType() == HitResult.Type.MISS)
        {
            return null;
        }

        if (trace.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = ((BlockHitResult) trace).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block blockTmp = state.getBlock();
            BlockEntity be = null;

            //MaLiLib.LOGGER.warn("getTarget():1: pos [{}], state [{}]", pos.toShortString(), state.toString());

            // Keep screen from getting 'stuck' if trying to use toggle on a lectern
            /*
            if (blockTmp instanceof LecternBlock && !newScreen)
            {
                return null;
            }
             */

            if (blockTmp instanceof BlockEntityProvider)
            {
                if (world instanceof ServerWorld)
                {
                    be = world.getWorldChunk(pos).getBlockEntity(pos);

                    if (be != null)
                    {
                        nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
                    }
                }
                else
                {
                    Pair<BlockEntity, NbtCompound> pair = this.getDataSyncer().requestBlockEntity(world, pos);

                    if (pair != null)
                    {
                        nbt = pair.getRight();
                    }
                }

                //MaLiLib.LOGGER.warn("getTarget():2: pos [{}], be [{}], nbt [{}]", pos.toShortString(), be != null, nbt != null);

                return this.getTargetInventoryFromBlock(world, pos, be, nbt);
            }

            return null;
        }
        else if (trace.getType() == HitResult.Type.ENTITY)
        {
            Entity entity = ((EntityHitResult) trace).getEntity();

            if (mc.targetedEntity != null && entity.getId() != mc.targetedEntity.getId())
            {
                MaLiLib.LOGGER.error("getTargetInventory(): entityId Not Equal: [{} != {}]", entity.getId(), mc.targetedEntity.getId());
            }

            MaLiLib.LOGGER.warn("getTargetInventory(): entityUUID [{}] vs targetedUUID [{}]", entity.getUuidAsString(), mc.targetedEntity != null ? mc.targetedEntity.getUuidAsString() : "<NULL>");

            if (world instanceof ServerWorld)
            {
                NbtView view = NbtView.getWriter(world.getRegistryManager());
                entity = world.getEntityById(entity.getId());

                if (entity != null && entity.saveSelfData(view.getWriter()))
                {
                    return this.getTargetInventoryFromEntity(world.getEntityById(entity.getId()), view.readNbt());
                }
            }
            else
            {
                Pair<Entity, NbtCompound> pair = this.getDataSyncer().requestEntity(world, entity.getId());

                if (pair != null)
                {
                    return this.getTargetInventoryFromEntity(world.getEntityById(pair.getLeft().getId()), pair.getRight());
                }
            }
        }

        return null;
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventoryFromBlock(World world, BlockPos pos, @Nullable BlockEntity be, NbtCompound nbt)
    {
        Inventory inv;

        if (be != null)
        {
            if (nbt.isEmpty())
            {
                nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
            }
            inv = InventoryUtils.getInventory(world, pos);
        }
        else
        {
            if (nbt.isEmpty())
            {
                Pair<BlockEntity, NbtCompound> pair = this.getDataSyncer().requestBlockEntity(world, pos);

                if (pair != null)
                {
                    nbt = pair.getRight();
                }
            }

            inv = this.getDataSyncer().getBlockInventory(world, pos, false);
        }

        BlockEntityType<?> beType = nbt != null ? NbtBlockUtils.getBlockEntityTypeFromNbt(nbt) : null;

        if ((beType != null && beType.equals(BlockEntityType.ENDER_CHEST)) ||
             be instanceof EnderChestBlockEntity)
        {
            if (MinecraftClient.getInstance().player != null)
            {
                PlayerEntity player = world.getPlayerByUuid(MinecraftClient.getInstance().player.getUuid());

                if (player != null)
                {
                    // Fetch your own EnderItems from Server ...
                    Pair<Entity, NbtCompound> enderPair = this.getDataSyncer().requestEntity(world, player.getId());
                    EnderChestInventory enderItems = null;

                    if (enderPair != null && enderPair.getRight() != null && enderPair.getRight().contains(NbtKeys.ENDER_ITEMS))
                    {
                        enderItems = InventoryUtils.getPlayerEnderItemsFromNbt(enderPair.getRight(), world.getRegistryManager());
                    }
                    else if (world instanceof ServerWorld)
                    {
                        enderItems = player.getEnderChestInventory();
                    }

                    if (enderItems != null)
                    {
                        inv = enderItems;
                    }
                }
            }
        }

        if (nbt != null && !nbt.isEmpty())
        {
            //MaLiLib.LOGGER.warn("getTargetInventoryFromBlock(): rawNbt: [{}]", nbt.toString());

            Inventory inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, world.getRegistryManager());

            if (inv == null)
            {
                inv = inv2;
            }
        }

        MaLiLib.LOGGER.warn("getTarget():3: pos [{}], inv [{}], be [{}], nbt [{}]", pos.toShortString(), inv != null, be != null, nbt != null ? nbt.getString("id") : new NbtCompound());

        if (inv == null || nbt == null)
        {
            return null;
        }

        this.context = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inv, nbt), inv,
                                                    be != null ? be : world.getBlockEntity(pos), null, nbt, this.getRefreshHandler());

        return this.context;
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventoryFromEntity(Entity entity, NbtCompound nbt)
    {
        Inventory inv = null;
        LivingEntity entityLivingBase = null;

        if (entity instanceof LivingEntity)
        {
            entityLivingBase = (LivingEntity) entity;
        }

        if (entity instanceof Inventory)
        {
            inv = (Inventory) entity;
        }
        else if (entity instanceof PlayerEntity player)
        {
            inv = new SimpleInventory(player.getInventory().getMainStacks().toArray(new ItemStack[36]));
        }
        else if (entity instanceof AbstractHorseEntity)
        {
            inv = ((IMixinAbstractHorseEntity) entity).malilib_getHorseInventory();
        }
        else if (entity instanceof InventoryOwner)
        {
            inv = ((InventoryOwner) entity).getInventory();
        }
        if (!nbt.isEmpty())
        {
            Inventory inv2;

            MaLiLib.LOGGER.warn("getTargetInventoryFromEntity(): rawNbt: [{}]", nbt.toString());

            // Fix for empty horse inv
            if (inv != null &&
                nbt.contains(NbtKeys.ITEMS) &&
                nbt.getList(NbtKeys.ITEMS).orElse(new NbtList()).size() > 1)
            {
                if (entity instanceof AbstractHorseEntity)
                {
                    inv2 = InventoryUtils.getNbtInventoryHorseFix(nbt, -1, entity.getRegistryManager());
                }
                else
                {
                    inv2 = InventoryUtils.getNbtInventory(nbt, -1, entity.getRegistryManager());
                }
                inv = null;
            }
            // Fix for saddled horse, no inv
            else if (inv != null &&
                    nbt.contains(NbtKeys.EQUIPMENT) && nbt.contains(NbtKeys.EATING_HAY))
            {
                inv2 = InventoryUtils.getNbtInventoryHorseFix(nbt, -1, entity.getRegistryManager());
                inv = null;
            }
            // Fix for empty Villager/Piglin inv
            else if (inv != null && inv.size() == 8 &&
                    nbt.contains(NbtKeys.INVENTORY) &&
                    !nbt.getList(NbtKeys.INVENTORY).orElse(new NbtList()).isEmpty())
            {
                inv2 = InventoryUtils.getNbtInventory(nbt, 8, entity.getRegistryManager());
                inv = null;
            }
            else
            {
                inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, entity.getRegistryManager());

                if (inv2 != null)
                {
                    inv = null;
                }
            }

            MaLiLib.LOGGER.error("getTargetInventoryFromEntity(): inv.size [{}], inv2.size [{}]", inv != null ? inv.size() : "null", inv2 != null ? inv2.size() : "null");

            if (inv2 != null)
            {
                inv = inv2;
            }
        }

        if (inv == null && entityLivingBase == null)
        {
            return null;
        }

        this.context = new InventoryOverlay.Context(inv != null ? InventoryOverlay.getBestInventoryType(inv, nbt) : InventoryOverlay.getInventoryType(nbt), inv,
                                                    null, entityLivingBase, nbt, this.getRefreshHandler());

        return this.context;
    }

    public static class Refresher implements InventoryOverlay.Refresher
    {
        public Refresher() {}

        @Override
        public InventoryOverlay.Context onContextRefresh(InventoryOverlay.Context data, World world)
        {
            // Refresh data
            if (data.be() != null)
            {
                TestInventoryOverlayHandler.getInstance().requestBlockEntityAt(world, data.be().getPos());
                data = TestInventoryOverlayHandler.getInstance().getTargetInventoryFromBlock(data.be().getWorld(), data.be().getPos(), data.be(), data.nbt());
            }
            else if (data.entity() != null)
            {
                TestInventoryOverlayHandler.getInstance().getDataSyncer().requestEntity(world, data.entity().getId());
                data = TestInventoryOverlayHandler.getInstance().getTargetInventoryFromEntity(data.entity(), data.nbt());
            }

            return data;
        }
    }
}
