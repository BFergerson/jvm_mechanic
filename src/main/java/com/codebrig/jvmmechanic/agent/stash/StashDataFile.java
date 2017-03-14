package com.codebrig.jvmmechanic.agent.stash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashDataFile {

    private final FileChannel fileChannel;

    public StashDataFile(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void stashDataEntry(DataEntry dataEntry) throws IOException {
        fileChannel.position(fileChannel.size());
        fileChannel.write(dataEntry.toByteBuffer());
        //fileChannel.force(false);
    }

    public DataEntry readDataEntry(long filePosition, int length) throws IOException {
        fileChannel.position(filePosition);

        ByteBuffer buffer = ByteBuffer.allocate(length);
        fileChannel.read(buffer);
        buffer.position(0);

        long eventId = buffer.getLong();
        byte[] rawData = new byte[length - 8];
        buffer.get(rawData);
        return new DataEntry(eventId, rawData);
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
