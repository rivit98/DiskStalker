package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.model.ObservedFolder;

import java.util.List;

public class CommandResult {
    private List<ObservedFolder> folderList;

    public List<ObservedFolder> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<ObservedFolder> folderList) {
        this.folderList = folderList;
    }

    public static CommandResult empty(){
        return new CommandResult();
    }
}
