package fi.dy.masa.malilib.test;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

import fi.dy.masa.malilib.interfaces.IDataSyncer;
import fi.dy.masa.malilib.util.WorldUtils;

@ApiStatus.Experimental
public class TestDataSyncer implements IDataSyncer
{
    private static final TestDataSyncer INSTANCE = new TestDataSyncer();

    public TestDataSyncer() { }

    public static TestDataSyncer getInstance() { return INSTANCE; }

    @Override
    public World getWorld()
    {
        return WorldUtils.getBestWorld(MinecraftClient.getInstance());
    }
}
