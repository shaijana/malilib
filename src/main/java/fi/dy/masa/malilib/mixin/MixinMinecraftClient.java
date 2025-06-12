package fi.dy.masa.malilib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.test.ConfigTestEnum;
import fi.dy.masa.malilib.test.TestSelector;

import java.nio.file.Path;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient
{
    @Shadow public ClientWorld world;
    @Unique private ClientWorld worldBefore;

    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/LevelStorage;createSymlinkFinder(Ljava/nio/file/Path;)Lnet/minecraft/util/path/SymlinkFinder;"))
    private void malilib_onPreGameInit(RunArgs args, CallbackInfo ci,
                                       @Local Path runDir)
    {
        // Register all mod handlers
        ((InitializationHandler) InitializationHandler.getInstance()).onPreGameInit(runDir);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("RETURN"))
    private void malilib_onInitComplete(RunArgs args, CallbackInfo ci)
    {
        // Register all mod handlers
        ((InitializationHandler) InitializationHandler.getInstance()).onGameInitDone();
    }

    @Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V",
            shift = At.Shift.BEFORE))
    private void malilib_onStartIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci)
    {
        //MaLiLib.printDebug("malilib_onStartIntegratedServer(): Get DynamicRegistry from IntegratedServer");
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadImmutable(saveLoader.combinedDynamicRegistries().getCombinedRegistryManager());
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onPostKeyboardInput(CallbackInfo ci)
    {
        KeybindMulti.reCheckPressedKeys();
        TickHandler.getInstance().onClientTick((MinecraftClient)(Object) this);
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("HEAD"))
    private void onLoadWorldPre(ClientWorld worldClientIn, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        // Only handle dimension changes/respawns here.
        // The initial join is handled in MixinClientPlayNetworkHandler onGameJoin

        //MaLiLib.logger.error("MC#onLoadWorldPre(): world [{}], worldBefore [{}], worldClientIn [{}]", this.world != null, this.worldBefore != null, worldClientIn != null);
        if (this.world != null)
        {
            this.worldBefore = this.world;
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.world, worldClientIn, (MinecraftClient)(Object) this);
        }
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("RETURN"))
    private void onLoadWorldPost(ClientWorld worldClientIn, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        //MaLiLib.logger.error("MC#onLoadWorldPost(): world [{}], worldBefore [{}], worldClientIn [{}]", this.world != null, this.worldBefore != null, worldClientIn != null);
        if (this.worldBefore != null)
        {
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, worldClientIn, (MinecraftClient)(Object) this);
            this.worldBefore = null;
        }
    }

    @Inject(method = "enterReconfiguration(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onReconfigurationPre(Screen screen, CallbackInfo ci)
    {
        //MaLiLib.logger.error("MC#onReconfigurationPre(): world [{}], worldBefore [{}]", this.world != null, this.worldBefore != null);
        this.worldBefore = this.world;
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, null, (MinecraftClient)(Object) this);
    }

    @Inject(method = "enterReconfiguration(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onReconfigurationPost(Screen screen, CallbackInfo ci)
    {
        //MaLiLib.logger.error("MC#onReconfigurationPost(): world [{}], worldBefore [{}]", this.world != null, this.worldBefore != null);
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, null, (MinecraftClient)(Object) this);
        this.worldBefore = null;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnectPre(Screen screen, boolean bl, CallbackInfo ci)
    {
        //MaLiLib.logger.error("MC#onDisconnectPre(): world [{}], worldBefore [{}]", this.world != null, this.worldBefore != null);
        this.worldBefore = this.world;
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, null, (MinecraftClient)(Object) this);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("RETURN"))
    private void onDisconnectPost(Screen screen, boolean bl, CallbackInfo ci)
    {
        //MaLiLib.logger.error("MC#onDisconnectPost(): world [{}], worldBefore [{}]", this.world != null, this.worldBefore != null);
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, null, (MinecraftClient)(Object) this);
        this.worldBefore = null;
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onLeftClickMouse(CallbackInfoReturnable<Boolean> cir)
    {
        if (MaLiLibReference.DEBUG_MODE &&
            MaLiLibConfigs.Test.TEST_CONFIG_BOOLEAN.getBooleanValue() &&
            ConfigTestEnum.TEST_WALLS_HOTKEY.getBooleanValue())
        {
            TestSelector.INSTANCE.select(false);
            cir.cancel();
            return;
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onRightClickMouse(CallbackInfo ci)
    {
        if (MaLiLibReference.DEBUG_MODE &&
            MaLiLibConfigs.Test.TEST_CONFIG_BOOLEAN.getBooleanValue() &&
            ConfigTestEnum.TEST_WALLS_HOTKEY.getBooleanValue())
        {
            TestSelector.INSTANCE.select(true);
            ci.cancel();
            return;
        }
    }
}
