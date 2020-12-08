package persistence;

import model.ObservedFolder;
import persistence.dao.ObservedFolderDao;

public class DatabaseCommandExecutor implements Runnable {
    ObservedFolderDao observedFolderDao = new ObservedFolderDao();
    ObservedFolder observedFolder;
    DatabaseCommand command;

   public DatabaseCommandExecutor(ObservedFolder observedFolder, DatabaseCommand command) {
       this.observedFolder = observedFolder;
       this.command = command;
   }

   @Override
    public void run() {
       switch(command) {
           case SAVE -> observedFolderDao.save(observedFolder);
           case DELETE -> observedFolderDao.delete(observedFolder);
           case UPDATE -> observedFolderDao.update(observedFolder);
       }
   }
}
