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

    public void stashDataEntry(DataEntry dataEntry) {
        //todo: persist journal entry
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
