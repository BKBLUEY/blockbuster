package mchorse.blockbuster.network.client.recording;

import java.util.ArrayList;
import java.util.List;

import mchorse.blockbuster.ClientProxy;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.recording.PacketFramesChunk;
import mchorse.blockbuster.network.common.recording.PacketPlayerRecording;
import mchorse.blockbuster.recording.RecordRecorder;
import mchorse.blockbuster.recording.data.Frame;
import mchorse.blockbuster.recording.data.Mode;
import mchorse.blockbuster.recording.data.Record;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client hanlder player recording
 *
 * This client handler is responsible for updating recording overlay status and
 * starting or stopping the recording based on the state given from packet.
 */
public class ClientHandlerPlayerRecording extends ClientMessageHandler<PacketPlayerRecording>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketPlayerRecording message)
    {
        ClientProxy.recordingOverlay.setVisible(message.recording);
        ClientProxy.recordingOverlay.setCaption(message.filename, true);

        if (message.recording)
        {
            ClientProxy.manager.record(message.filename, player, Mode.FRAMES, false, false, message.offset, null);
        }
        else
        {
            if (!message.canceled)
            {
                this.sendFrames(ClientProxy.manager.recorders.get(player));
            }

            ClientProxy.manager.halt(player, false, false, message.canceled);
        }
    }

    /**
     * Send frames to the server
     *
     * Send chunked frames to the server
     */
    @SideOnly(Side.CLIENT)
    private void sendFrames(RecordRecorder recorder)
    {
        Record record = recorder.record;

        int cap = 400;
        int length = record.getLength();
        int offset = recorder.offset;

        /* Send only one message if it's below 500 frames */
        if (length < cap)
        {
            Dispatcher.sendToServer(new PacketFramesChunk(0, 1, offset, record.filename, record.frames));

            return;
        }

        /* Send chunked frames to the server */
        for (int i = 0, c = (length / cap) + 1; i < c; i++)
        {
            List<Frame> frames = new ArrayList<Frame>();

            for (int j = 0, d = length - i * cap > cap ? cap : (length % cap); j < d; j++)
            {
                frames.add(record.frames.get(j + i * cap));
            }

            Dispatcher.sendToServer(new PacketFramesChunk(i, c, offset, record.filename, frames));
        }
    }
}