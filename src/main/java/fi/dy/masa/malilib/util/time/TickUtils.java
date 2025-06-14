package fi.dy.masa.malilib.util.time;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import fi.dy.masa.malilib.util.MathUtils;

public class TickUtils
{
    private static final Data INSTANCE = new Data();
    public static Data getInstance() { return INSTANCE; }

    public static float getTickRate()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
        {
            return mc.getServer().getTickManager().getTickRate();
        }
        else if (mc.world != null)
        {
            return mc.world.getTickManager().getTickRate();
        }

        return -1F;
    }

    public static float getMillisPerTick()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
        {
            return mc.getServer().getTickManager().getMillisPerTick();
        }
        else if (mc.world != null)
        {
            return mc.world.getTickManager().getMillisPerTick();
        }

        return -1F;
    }

    public static boolean isStepping()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
        {
            return mc.getServer().getTickManager().isStepping();
        }
        else if (mc.world != null)
        {
            return mc.world.getTickManager().isStepping();
        }

        return false;
    }
    public static boolean isFrozen()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
        {
            return mc.getServer().getTickManager().isFrozen();
        }
        else if (mc.world != null)
        {
            return mc.world.getTickManager().isFrozen();
        }

        return false;
    }
    public static boolean isSprinting()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
        {
            return mc.getServer().getTickManager().isSprinting();
        }
        else if (mc.world != null)
        {
            Data timeData = getInstance();

            // MSPT drops when sprinting due to math also.
            return (timeData.hasTimeSynced() && (timeData.getActualTPS() / 3 > timeData.getTickRate()));
        }

        return false;
    }

    public static boolean isMeasuredEstimated()
    {
        return getInstance().hasTimeSynced();
    }

    public static double getMeasuredMSPT()
    {
        Data timeData = getInstance();

        if (timeData.isValid())
        {
            return timeData.getMeasuredMSPT();
        }

        return 0.0D;
    }

    public static double getMeasuredTPS()
    {
        Data timeData = getInstance();

        if (timeData.isValid())
        {
            return timeData.getMeasuredTPS();
        }

        return 0.0D;
    }

    // The non-scaled version based on tickRate (The raw math version)
    public static double getActualTPS()
    {
        Data timeData = getInstance();

        if (timeData.isValid())
        {
            return timeData.getActualTPS();
        }

        return 0.0D;
    }

    public static double getAvgMSPT()
    {
        Data timeData = getInstance();

        if (timeData.isValid())
        {
            return timeData.getAverageMSPT();
        }

        return 0.0D;
    }

    public static double getAvgTPS()
    {
        Data timeData = getInstance();

        if (timeData.isValid())
        {
            return timeData.getAverageTPS();
        }

        return 0.0D;
    }

    @ApiStatus.Internal
    public static class Data
    {
        private double tickRate = TickUtils.getTickRate();
        private double measuredTPS = -1.0D;
        private double measuredMSPT = -1.0D;
        private double actualTPS = -1.0D;
        private long lastNanoTick = -1L;
        private long lastNanoTime = -1L;
        private final int MAX_HISTORY = 30;
        private final double[] prevMeasuredMSPT = new double[MAX_HISTORY];
        private final double[] prevMeasuredTPS  = new double[MAX_HISTORY];
        private int lastMeasurementTick = 0;
        private double avgMeasuredMSPT = -1.0D;
        private double avgMeasuredTPS = -1.0D;
        private boolean isValid = false;
        private boolean hasTimeSynced = false;

        private Data() {}

        public void updateTickRate(float tickRate)
        {
            this.tickRate = tickRate;
        }

        public void updateNanoTick(long timeUpdate)
        {
            if (!MinecraftClient.getInstance().isIntegratedServerRunning())
            {
                final long currentTime = System.nanoTime();

                if (this.hasTimeSynced)
                {
                    final long elapsed = timeUpdate - this.lastNanoTick;

                    if (elapsed > 0)
                    {
                        this.measuredMSPT = ((double) (currentTime - this.lastNanoTime) / (double) elapsed) / 1000000D;
                        this.measuredTPS = this.measuredMSPT <= 50 ? this.tickRate : (1000D / this.measuredMSPT);
                        this.actualTPS = (1000D / this.measuredMSPT);
                        this.calculateAverages();
                        this.isValid = true;
                    }
                }

                this.lastNanoTick = timeUpdate;
                this.lastNanoTime = currentTime;
                this.hasTimeSynced = true;
            }
        }

        public void updateNanoTickFromServer(MinecraftServer server)
        {
            this.lastNanoTime = System.nanoTime();

            if (server != null)
            {
                this.measuredMSPT = MathUtils.average(server.getTickTimes()) / 1000000D;
                this.measuredTPS = this.measuredMSPT <= 50 ? this.tickRate : (1000D / this.measuredMSPT);
                this.actualTPS = (1000D / this.measuredMSPT);
                this.calculateAverages();
                this.isValid = true;
            }
        }

        public void updateTicksFromServerDirect(final double tps, final double mspt)
        {
            // For things like Carpet / Servux
            this.lastNanoTime = System.nanoTime();
            this.measuredMSPT = mspt;
            this.measuredTPS = tps;
            this.actualTPS = (1000D / this.measuredMSPT);
            this.calculateAverages();
            this.isValid = true;
        }

        private void calculateAverages()
        {
            if (this.lastMeasurementTick >= MAX_HISTORY)
            {
                this.lastMeasurementTick = 0;
            }

            this.prevMeasuredMSPT[this.lastMeasurementTick] = this.measuredMSPT;
            this.prevMeasuredTPS[this.lastMeasurementTick] = this.measuredTPS;
            this.avgMeasuredMSPT = MathUtils.average(this.prevMeasuredMSPT);
            this.avgMeasuredTPS = MathUtils.average(this.prevMeasuredTPS);
            this.lastMeasurementTick++;
        }

        public boolean isValid() { return this.isValid; }

        public boolean hasTimeSynced() { return this.hasTimeSynced; }

        public double getTickRate() { return this.tickRate; }

        public double getMeasuredTPS() { return this.measuredTPS; }

        public double getMeasuredMSPT() { return this.measuredMSPT; }

        public double getActualTPS() { return this.actualTPS; }

        public double getAverageMSPT() { return this.avgMeasuredMSPT; }

        public double getAverageTPS() { return this.avgMeasuredTPS; }
    }
}
