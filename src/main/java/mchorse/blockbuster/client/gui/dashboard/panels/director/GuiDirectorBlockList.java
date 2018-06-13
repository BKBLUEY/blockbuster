package mchorse.blockbuster.client.gui.dashboard.panels.director;

import java.util.function.Consumer;

import mchorse.blockbuster.client.gui.dashboard.panels.GuiBlockList;
import mchorse.blockbuster.common.tileentity.TileEntityDirector;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Model block list 
 */
public class GuiDirectorBlockList extends GuiBlockList<BlockPos>
{
    public GuiDirectorBlockList(Minecraft mc, String title, Consumer<BlockPos> callback)
    {
        super(mc, title, callback);
    }

    @Override
    public boolean addBlock(BlockPos pos)
    {
        TileEntity tile = this.mc.world.getTileEntity(pos);

        if (tile instanceof TileEntityDirector)
        {
            this.elements.add(pos);

            this.scroll.setSize(this.elements.size());
            this.scroll.clamp();

            return true;
        }

        return false;
    }

    @Override
    public void render(int x, int y, BlockPos item, boolean hovered)
    {
        BlockPos pos = item;
        String label = String.format("(%s, %s, %s)", pos.getX(), pos.getY(), pos.getZ());

        this.font.drawStringWithShadow(label, x + 10, y + 6, hovered ? 16777120 : 0xffffff);
    }
}