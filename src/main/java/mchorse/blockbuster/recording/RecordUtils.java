package mchorse.blockbuster.recording;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.CommonProxy;
import mchorse.blockbuster.capabilities.recording.IRecording;
import mchorse.blockbuster.capabilities.recording.Recording;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.recording.PacketFramesLoad;
import mchorse.blockbuster.network.common.recording.PacketRequestedFrames;
import mchorse.blockbuster.network.common.recording.PacketUnloadFrames;
import mchorse.blockbuster.recording.data.Record;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities methods mostly to be used with recording code. Stuff like
 * broadcasting a message and sending records to players are located here.
 */
public class RecordUtils
{
    /**
     * String version of {@link #broadcastMessage(ITextComponent)}
     */
    public static void broadcastMessage(String message)
    {
        broadcastMessage(new TextComponentString(message));
    }

    /**
     * I18n formatting version of {@link #broadcastMessage(ITextComponent)}
     */
    public static void broadcastMessage(String string, Object... args)
    {
        broadcastMessage(new TextComponentTranslation(string, args));
    }

    /**
     * Send given message to everyone on the server, to everyone.
     *
     * Invoke this method only on the server side.
     */
    public static void broadcastMessage(ITextComponent message)
    {
        for (EntityPlayerMP player : getPlayers())
        {
            player.sendMessage(message);
        }
    }

    /**
     * Send given error to everyone on the server, to everyone.
     *
     * Invoke this method only on the server side.
     */
    public static void broadcastError(String string, Object... objects)
    {
        for (EntityPlayerMP player : getPlayers())
        {
            Blockbuster.l10n.error(player, string, objects);
        }
    }

    /**
     * Send given error to everyone on the server, to everyone.
     *
     * Invoke this method only on the server side.
     */
    public static void broadcastInfo(String string, Object... objects)
    {
        for (EntityPlayerMP player : getPlayers())
        {
            Blockbuster.l10n.info(player, string, objects);
        }
    }

    public static List<EntityPlayerMP> getPlayers()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
    }

    /**
     * Checks whether player recording exists
     */
    public static boolean isReplayExists(String filename)
    {
        return replayFile(filename).exists() || CommonProxy.manager.records.containsKey(filename);
    }

    /**
     * Get path to replay file (located in current world save's folder)
     */
    public static File replayFile(String filename)
    {
        return Utils.serverFile("blockbuster/records", filename);
    }

    /**
     * Get list of all available replays
     */
    public static List<String> getReplays()
    {
        return Utils.serverFiles("blockbuster/records");
    }

    /**
     * Get list of all available replays
     */
    public static List<String> getReplayIterations(String replay)
    {
        List<String> list = new ArrayList<String>();
        File replays = new File(DimensionManager.getCurrentSaveRootDirectory() + "/blockbuster/records");
        File[] files = replays.listFiles();

        if (files == null)
        {
            return list;
        }

        for (File file : files)
        {
            String name = file.getName();

            if (file.isFile() && name.startsWith(replay) && name.contains(".dat~"))
            {
                list.add(name.substring(name.indexOf("~") + 1));
            }
        }

        return list;
    }

    /**
     * Send record frames to given player (from the server)
     */
    public static void sendRecord(String filename, EntityPlayerMP player)
    {
        if (!playerNeedsAction(filename, player))
        {
            return;
        }

        RecordManager manager = CommonProxy.manager;
        Record record = manager.records.get(filename);

        if (record == null)
        {
            try
            {
                record = new Record(filename);
                record.load(replayFile(filename));

                manager.records.put(filename, record);
            }
            catch (FileNotFoundException e)
            {
                Blockbuster.l10n.error(player, "recording.not_found", filename);
                record = null;
            }
            catch (Exception e)
            {
                Blockbuster.l10n.error(player, "recording.read", filename);
                e.printStackTrace();
                record = null;
            }
        }

        if (record != null)
        {
            record.resetUnload();

            Dispatcher.sendTo(new PacketFramesLoad(filename, record.preDelay, record.postDelay, record.frames), player);
        }
    }

    /**
     * Send requested frames (for actor) to given player (from the server)
     */
    public static void sendRequestedRecord(int id, String filename, EntityPlayerMP player)
    {
        Record record = CommonProxy.manager.records.get(filename);

        if (playerNeedsAction(filename, player) && record != null)
        {
            record.resetUnload();

            Dispatcher.sendTo(new PacketRequestedFrames(id, record.filename, record.preDelay, record.postDelay, record.frames), player);
        }
        else if (record == null)
        {
            Blockbuster.l10n.error(player, "recording.not_found", filename);
        }
    }

    /**
     * Checks whether given player needs a new action, meaning, he has an older
     * version of given named action or he doesn't have this action at all.
     */
    private static boolean playerNeedsAction(String filename, EntityPlayer player)
    {
        IRecording recording = Recording.get(player);

        boolean has = recording.hasRecording(filename);
        long time = replayFile(filename).lastModified();

        if (has && time > recording.recordingTimestamp(filename))
        {
            recording.updateRecordingTimestamp(filename, time);

            return true;
        }

        if (!has)
        {
            recording.addRecording(filename, time);
        }

        return !has;
    }

    /**
     * Unload given record. It will send to all players a packet to unload a
     * record.
     */
    public static void unloadRecord(Record record)
    {
        String filename = record.filename;

        for (EntityPlayerMP player : RecordUtils.getPlayers())
        {
             IRecording recording = Recording.get(player);

            if (recording.hasRecording(filename))
            {
                recording.removeRecording(filename);

                Dispatcher.sendTo(new PacketUnloadFrames(filename), player);
            }
        }
    }

    public static void saveRecord(Record record) throws IOException
    {
        saveRecord(record, true);
    }

    public static void saveRecord(Record record, boolean unload) throws IOException
    {
        saveRecord(record, true, unload);
    }

    public static void saveRecord(Record record, boolean savePast, boolean unload) throws IOException
    {
        record.dirty = false;
        record.save(replayFile(record.filename), savePast);

        if (unload)
        {
            unloadRecord(record);
        }
    }

    public static void dirtyRecord(Record record)
    {
        record.dirty = true;

        unloadRecord(record);
    }
}