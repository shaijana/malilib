package fi.dy.masa.malilib.test.gui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;

public class GuiTestList extends GuiListBase<GuiTestList.Entry, WidgetTestListEntry, WidgetTestList>
        implements ISelectionListener<GuiTestList.Entry>
{
    protected final int iconSize = 64;
    public GuiTestList()
    {
        super(10, 60);
    }

    @Override
    protected int getBrowserWidth()
    {
        return this.getScreenWidth() - 20;
    }

    @Override
    protected int getBrowserHeight()
    {
        return this.getScreenHeight() - 94;
    }

    @Override
    protected WidgetTestList createListWidget(int listX, int listY)
    {
        return new WidgetTestList(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this);
    }

    @Override
    protected ISelectionListener<Entry> getSelectionListener()
    {
        return this;
    }

    @Override
    public void onSelectionChange(@Nullable GuiTestList.Entry entry)
    {
        if (entry != null)
        {
            MaLiLib.LOGGER.warn("GuiTestListWidget#onSelectionChange(): name: [{}], state: [{}]", entry.name(), entry.state().toString());
        }
    }

    public record Entry(String name, BlockState state)
    {
        public Block getBlock()
        {
            return this.state().getBlock();
        }
    }
}
