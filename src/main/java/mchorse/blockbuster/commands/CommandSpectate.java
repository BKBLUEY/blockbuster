package mchorse.blockbuster.commands;

import mchorse.blockbuster.Blockbuster;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

import java.util.List;

/**
 * Spectate entity command - /spectate &lt;player&gt; &lt;entity&gt;
 * 
 * This command allows to make given player a spectator of given entity. 
 * I don't know why it's useful, but I think this can be useful.
 */
public class CommandSpectate extends BBCommandBase
{
    @Override
    public String getName()
    {
        return "spectate";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blockbuster.commands.spectate.help";
    }

    @Override
    public String getSyntax()
    {
        return "{l}{6}/{r}spectate {8}<player>{r} {7}<entity>{r}";
    }

    @Override
    public int getRequiredArgs()
    {
        return 2;
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getPlayer(server, sender, args[0]);

        if (player == null)
        {
            Blockbuster.l10n.error(sender, "commands.no_player", args[0]);

            return;
        }

        List<Entity> entities = EntitySelector.matchEntities(sender, args[1], Entity.class);

        if (entities.isEmpty())
        {
            Blockbuster.l10n.error(sender, "commands.no_entity", args[1]);

            return;
        }

        if (!player.isSpectator())
        {
            player.setGameType(GameType.SPECTATOR);
        }

        for (Entity entity : entities)
        {
            if (entity != player)
            {
                player.setSpectatingEntity(entity);

                break;
            }
        }
    }

    /**
     * Provide completion for player usernames for first argument
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}