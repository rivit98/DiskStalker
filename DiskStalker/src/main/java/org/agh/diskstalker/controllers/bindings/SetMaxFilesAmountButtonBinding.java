package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.NodeData;

public class SetMaxFilesAmountButtonBinding extends AbstractButtonBinding {
    public SetMaxFilesAmountButtonBinding(
            MainController mainController,
            TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel
    ) {
        super(
                () -> (!mainController.isMainFolder(selectionModel.getSelectedItem()) || mainController.getMaxFilesAmountField().getText().isEmpty()),
                selectionModel.selectedItemProperty(),
                mainController.getMaxFilesAmountField().textProperty()
        );
    }
}
