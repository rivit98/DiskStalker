package org.agh.diskstalker.builders;

import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.limits.FolderLimits;
import org.agh.diskstalker.model.limits.LimitType;

public class FolderLimitsBuilder {
    private long totalSizeLimit = 0;
    private long filesAmountLimit = 0;
    private long largestFileLimit = 0;
    private ObservedFolder observedFolder;

    public FolderLimitsBuilder withFolder(ObservedFolder folder) {
        observedFolder = folder;
        return this;
    }

    public FolderLimitsBuilder withTotalSize(long limit) {
        totalSizeLimit = limit;
        return this;
    }

    public FolderLimitsBuilder withFileAmount(long limit) {
        filesAmountLimit = limit;
        return this;
    }

    public FolderLimitsBuilder withLargestFileSize(long limit) {
        largestFileLimit = limit;
        return this;
    }

    public FolderLimits build() {
        var limits = new FolderLimits(observedFolder);
        limits.setLimit(LimitType.TOTAL_SIZE, totalSizeLimit);
        limits.setLimit(LimitType.FILES_AMOUNT, filesAmountLimit);
        limits.setLimit(LimitType.LARGEST_FILE, largestFileLimit);

        return limits;
    }
}
