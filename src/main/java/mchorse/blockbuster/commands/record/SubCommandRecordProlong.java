package mchorse.blockbuster.commands.record;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.commands.CommandRecord;
import mchorse.blockbuster.recording.RecordUtils;
import mchorse.blockbuster.recording.data.Record;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Command /record prolong
 * 
 * This command is responsible for duplicating player recording to a new 
 * player recording file
 */
public class SubCommandRecordProlong extends SubCommandRecordBase
{
    @Override
    public String getName()
    {
        return "prolong";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blockbuster.commands.record.prolong";
    }

    @Override
    public String getSyntax()
    {
        return "{l}{6}/{r}record {8}prolong{r} {7}<filename> [post_delay] [pre_delay]{r}";
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Record record = CommandRecord.getRecord(args[0]);

        if (args.length >= 2)
        {
            record.postDelay = CommandBase.parseInt(args[1]);
        }

        if (args.length >= 3)
        {
            record.preDelay = CommandBase.parseInt(args[2]);
        }

        try
        {
            RecordUtils.saveRecord(record);

            Blockbuster.l10n.success(sender, "record.prolonged", args[0], record.preDelay, record.postDelay);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Blockbuster.l10n.error(sender, "record.couldnt_save", args[1]);
        }
    }
}