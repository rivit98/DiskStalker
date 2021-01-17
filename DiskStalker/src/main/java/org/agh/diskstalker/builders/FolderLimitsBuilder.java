package org.agh.diskstalker.builders;

import org.agh.diskstalker.model.FolderLimits;
import org.agh.diskstalker.model.ObservedFolder;

public class FolderLimitsBuilder {
    private long totalSizeLimit = 0;
    private long filesAmountLimit = 0;
    private long biggestFileLimit = 0;
    private ObservedFolder observedFolder;

    public FolderLimitsBuilder withFolder(ObservedFolder folder){
        observedFolder = folder;
        return this;
    }

    public FolderLimitsBuilder withTotalSize(long limit){
        totalSizeLimit = limit;
        return this;
    }

    public FolderLimitsBuilder withFileAmount(long limit){
        filesAmountLimit = limit;
        return this;
    }

    public FolderLimitsBuilder withBiggestFileSize(long limit){
        biggestFileLimit = limit;
        return this;
    }

    public FolderLimits build(){
        var limits = new FolderLimits(observedFolder);
        limits.setMaxTotalSize(totalSizeLimit);
        limits.setMaxFilesAmount(filesAmountLimit);

        return limits;
    }
}
