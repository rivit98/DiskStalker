package persistence;

import model.ObservedFolder;

public class DatabaseCommandExecutor implements Runnable {
    ObservedFolder observedFolder;
    DatabaseCommand command;

   public DatabaseCommandExecutor(ObservedFolder observedFolder, DatabaseCommand command) {
       this.observedFolder = observedFolder;
       this.command = command;
   }

   @Override
    public void run() {
       switch(command) {
           case SAVE -> ObservedFolderDao.save(observedFolder);
           case DELETE -> ObservedFolderDao.delete(observedFolder);
           case UPDATE -> ObservedFolderDao.update(observedFolder);
       }
   }
}
