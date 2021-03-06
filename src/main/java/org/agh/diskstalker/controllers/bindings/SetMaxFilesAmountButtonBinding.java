package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.tree.NodeData;

public class SetMaxFilesAmountButtonBinding extends AbstractButtonBooleanBinding {
    public SetMaxFilesAmountButtonBinding(
            MainController mainController,
            TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel
    ) {
        super(
                () -> (
                        !mainController.canSetLimitOnNode(selectionModel.getSelectedItem())
                                || mainController.getMaxFilesAmountField().getText().isEmpty()
                ),
                selectionModel.selectedItemProperty(),
                mainController.getMaxFilesAmountField().textProperty()
        );
    }
}
