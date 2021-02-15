package mchorse.blockbuster.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class CommandOnHead extends CommandBase
{
    @Override
    public String getName()
    {
        return "on_head";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blockbuster.commands.on_head.help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getHeldItemMainhand();

        if (!stack.isEmpty())
        {
            player.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack.copy());
        }
    }
}