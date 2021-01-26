package org.agh.diskstalker.model;

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
        assertTrue(limits.isTotalSizeExceeded());

        limits.setMaxTotalSize(folderSize * 2);
        assertFalse(limits.isTotalSizeExceeded());
    }

    @Test
    public void biggestFileLimit(){
        limits.setBiggestFileLimit(folderBiggestFile / 2);
        assertTrue(limits.isBiggestFileLimitExceeded());

        limits.setBiggestFileLimit(folderBiggestFile * 2);
        assertFalse(limits.isBiggestFileLimitExceeded());
    }

    @Test
    public void filesAmountLimit(){
        limits.setMaxFilesAmount(folderFilesAmount / 2);
        assertTrue(limits.isFilesAmountExceeded());

        limits.setMaxFilesAmount(folderFilesAmount * 2);
        assertFalse(limits.isFilesAmountExceeded());
    }
}
