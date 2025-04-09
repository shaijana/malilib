package fi.dy.masa.malilib.test;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;

@ApiStatus.Experimental
public class TestWalls implements AutoCloseable
{
    public static final TestWalls INSTANCE = new TestWalls();

    protected boolean renderThrough;
    protected boolean useCulling;
    protected float glLineWidth;

    private List<Box> boxes;
    private BlockPos center;
    protected BlockPos lastUpdatePos;
    private Vec3d updateCameraPos;
    private boolean hasData;
    private final boolean shouldResort;
    private final boolean needsUpdate;
    private final int updateDistance = 48;

    public TestWalls()
    {
        this.renderThrough = false;
        this.useCulling = false;
        this.glLineWidth = 3.0f;
        this.lastUpdatePos = null;
        this.updateCameraPos = Vec3d.ZERO;
        this.hasData = false;
        this.shouldResort = false;
        this.needsUpdate = true;
        this.boxes = new ArrayList<>();
        this.center = null;
    }

    public Vec3d getUpdatePosition()
    {
        return updateCameraPos;
    }

    public void setUpdatePosition(Vec3d cameraPosition)
    {
        this.updateCameraPos = cameraPosition;
    }

    public boolean needsUpdate(Entity cameraEntity, MinecraftClient mc)
    {
        return this.needsUpdate || this.lastUpdatePos == null ||
                Math.abs(cameraEntity.getX() - this.lastUpdatePos.getX()) > this.updateDistance ||
                Math.abs(cameraEntity.getZ() - this.lastUpdatePos.getZ()) > this.updateDistance ||
                Math.abs(cameraEntity.getY() - this.lastUpdatePos.getY()) > this.updateDistance;
    }

    public void update(Camera camera, Entity entity, MinecraftClient mc)
    {
        if (mc.world == null || mc.player == null)
        {
            return;
        }

        int radius = MaLiLibConfigs.Test.TEST_CONFIG_INTEGER.getIntegerValue();
        Vec3d vec = camera.getPos();
        BlockPos pos = entity.getBlockPos();
        BlockPos testPos = pos.add(2, 0, 2);
        Pair<BlockPos, BlockPos> corners = TestUtils.getSpawnChunkCorners(testPos, radius, mc.world);
        this.boxes = TestUtils.calculateBoxes(corners.getLeft(), corners.getRight());

        if (!this.boxes.isEmpty())
        {
            this.center = testPos;
            this.hasData = true;
        }
        else
        {
            this.center = null;
            this.hasData = false;
        }

        setUpdatePosition(vec);
    }

    public void render(Camera camera, Matrix4f matrix4f, Matrix4f projMatrix, MinecraftClient mc, Profiler profiler)
    {
        profiler.push("render_test_walls");

        if (this.hasData && !this.boxes.isEmpty() && this.center != null)
        {
            this.renderQuads(camera, mc, profiler);
            this.renderOutlines(camera, mc, profiler);
            this.boxes.clear();
            this.center = null;
            this.hasData = false;
        }

        profiler.pop();
    }

    private void renderQuads(Camera camera, MinecraftClient mc, Profiler profiler)
    {
        if (mc.world == null || mc.player == null ||
            !this.hasData || this.boxes.isEmpty())
        {
            return;
        }

        profiler.push("quads");
        Color4f quadsColor = MaLiLibConfigs.Test.TEST_CONFIG_COLOR.getColor();
        Vec3d cameraPos = camera.getPos();

        // MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_NO_DEPTH_NO_CULL
        RenderContext ctx = new RenderContext(() -> "TestWalls Quads", MaLiLibPipelines.MINIHUD_SHAPE_OFFSET, BufferUsage.STATIC_WRITE);
        BufferBuilder builder = ctx.getBuilder();
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
//        MatrixStack matrices = new MatrixStack();
        Vec3d updatePos = this.getUpdatePosition();

        this.preRender();
        matrix4fstack.pushMatrix();
        matrix4fstack.translate((float) (updatePos.x - cameraPos.x), (float) (updatePos.y - cameraPos.y), (float) (updatePos.z - cameraPos.z));

//        matrices.push();
//        MatrixStack.Entry e = matrices.peek();

        RenderUtils.drawBlockBoundingBoxSidesBatchedQuads(this.center, cameraPos, quadsColor, 0.001, builder);

        for (Box entry : this.boxes)
        {
            TestUtils.renderWallQuads(entry, cameraPos, quadsColor, builder);
        }

//        matrices.pop();

        try
        {
//            ctx.offset(new float[]{-3f, 0f, -3f});
            BuiltBuffer meshData = builder.endNullable();

            if (meshData != null)
            {
                if (this.shouldResort)
                {
                    ctx.upload(meshData, true);
                    ctx.startResorting(meshData, ctx.createVertexSorter(camera));
                }
                else
                {
                    ctx.upload(meshData, false);
                }

                ctx.drawPost();
                meshData.close();
            }

            ctx.close();
        }
        catch (Exception err)
        {
            MaLiLib.LOGGER.error("TestWalls#renderQuads(): Exception; {}", err.getMessage());
        }

        this.postRender();
        matrix4fstack.popMatrix();
        profiler.pop();
    }

    private void renderOutlines(Camera camera, MinecraftClient mc, Profiler profiler)
    {
        if (mc.world == null || mc.player == null)
        {
            return;
        }

        profiler.push("outlines");
        Color4f linesColor = Color4f.WHITE;
        Vec3d cameraPos = camera.getPos();

        // RenderPipelines.LINES
        RenderContext ctx = new RenderContext(() -> "TestWalls Lines", RenderPipelines.LINES, BufferUsage.STATIC_WRITE);
        BufferBuilder builder = ctx.getBuilder();
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        MatrixStack matrices = new MatrixStack();
        Vec3d updatePos = this.getUpdatePosition();

//        this.preRender();
        matrix4fstack.pushMatrix();
        matrix4fstack.translate((float) (updatePos.x - cameraPos.x), (float) (updatePos.y - cameraPos.y), (float) (updatePos.z - cameraPos.z));
        matrices.push();

        MatrixStack.Entry e = matrices.peek();
        RenderUtils.drawBlockBoundingBoxOutlinesBatchedLines(this.center, cameraPos, linesColor, 0.001, builder, e);

        for (Box entry : this.boxes)
        {
            TestUtils.renderWallOutlines(entry, 16, 16, true, cameraPos, linesColor, builder, e);
        }

        matrices.pop();
        matrix4fstack.popMatrix();

        try
        {
            BuiltBuffer meshData = builder.endNullable();

            if (meshData != null)
            {
                ctx.lineWidth(this.glLineWidth);
                ctx.draw(meshData, false, true);
                meshData.close();
            }

            ctx.close();
        }
        catch (Exception err)
        {
            MaLiLib.LOGGER.error("TestWalls#renderOutlines(): Exception; {}", err.getMessage());
        }

//        this.postRender();
        profiler.pop();
    }

    protected void preRender()
    {
//        RenderUtils.polygonOffset(-3f, -3f);
//        RenderUtils.polygonOffset(true);
//        RenderUtils.blend(true);
//        RenderSystem.lineWidth(this.glLineWidth);

//        if (this.renderThrough)
//        {
//            RenderUtils.depthTest(false);
//        }
//        else
//        {
//            RenderUtils.depthMask(true);
//        }

//        RenderUtils.culling(this.useCulling);
    }

    protected void postRender()
    {
//        if (this.renderThrough)
//        {
//            RenderUtils.depthTest(true);
//        }
//        else
//        {
//            RenderUtils.depthMask(false);
//        }

//        RenderUtils.culling(!this.useCulling);
//        RenderUtils.polygonOffset(0f, 0f);
//        RenderUtils.polygonOffset(false);
//        RenderUtils.color(1f, 1f, 1f, 1f);
//        RenderUtils.blend(false);
    }

    public void clear()
    {
        this.lastUpdatePos = BlockPos.ORIGIN;
        this.hasData = false;
        this.boxes.clear();
    }

    @Override
    public void close()
    {
        clear();
    }
}
