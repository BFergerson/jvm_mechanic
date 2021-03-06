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
    private final Object readLock = new Object();

    public StashDataFile(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void stashDataEntry(DataEntry dataEntry) throws IOException {
        fileChannel.position(fileChannel.size());
        fileChannel.write(dataEntry.toByteBuffer());
    }

    public DataEntry readDataEntry(long filePosition, int length) throws IOException {
        synchronized (readLock) {
            fileChannel.position(filePosition);

            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer);
            buffer.position(0);

            byte[] rawData = new byte[length];
            buffer.get(rawData);
            return new DataEntry(rawData);
        }
    }

    public long getSize() throws IOException {
        return fileChannel.size();
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
