package org.agh.diskstalker.persistence.command;

import lombok.Getter;
import lombok.Setter;
import org.agh.diskstalker.model.interfaces.IObservedFolder;

import java.util.List;

@Getter
@Setter
public class CommandResult {
    private List<IObservedFolder> folderList;

    public static CommandResult empty(){
        return new CommandResult();
    }
}
