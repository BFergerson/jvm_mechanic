package com.codebrig.jvmmechanic.agent.stash;

import java.io.IOException;
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
        fileChannel.write(dataEntry.toByteBuffer());
        //fileChannel.force(false);
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
