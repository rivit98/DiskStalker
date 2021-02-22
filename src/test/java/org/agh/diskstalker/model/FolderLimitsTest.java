package org.agh.diskstalker.model;

import org.agh.diskstalker.model.limits.FolderLimits;
import org.agh.diskstalker.model.limits.LimitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FolderLimitsTest {
    private static final long folderSize = 1024L;
    private static final long folderBiggestFile = 512L;
    private static final long folderFilesAmount = 3L;

    private FolderLimits limits;

    @BeforeEach
    public void setUp() {
        var folder = mock(ObservedFolder.class);
        when(folder.getSize()).thenReturn(folderSize);
        when(folder.getBiggestFileSize()).thenReturn(folderBiggestFile);
        when(folder.getFilesAmount()).thenReturn(folderFilesAmount);
        limits = new FolderLimits(folder);
    }

    @Test
    public void totalSizeLimit(){
        limits.setMaxTotalSize(folderSize / 2);
        assertTrue(limits.isLimitExceeded(LimitType.TOTAL_SIZE));

        limits.setMaxTotalSize(folderSize * 2);
        assertFalse(limits.isLimitExceeded(LimitType.TOTAL_SIZE));
    }

    @Test
    public void biggestFileLimit(){
        limits.setBiggestFileLimit(folderBiggestFile / 2);
        assertTrue(limits.isLimitExceeded(LimitType.BIGGEST_FILE));

        limits.setBiggestFileLimit(folderBiggestFile * 2);
        assertFalse(limits.isLimitExceeded(LimitType.BIGGEST_FILE));
    }

    @Test
    public void filesAmountLimit(){
        limits.setMaxFilesAmount(folderFilesAmount / 2);
        assertTrue(limits.isLimitExceeded(LimitType.FILES_AMOUNT));

        limits.setMaxFilesAmount(folderFilesAmount * 2);
        assertFalse(limits.isLimitExceeded(LimitType.FILES_AMOUNT));
    }

    @Test
    public void anyLimit(){
        limits.setMaxFilesAmount(folderFilesAmount / 2);
        assertTrue(limits.isAnyLimitExceeded());

        limits.setMaxFilesAmount(folderFilesAmount * 2);
        assertFalse(limits.isAnyLimitExceeded());
    }
}
