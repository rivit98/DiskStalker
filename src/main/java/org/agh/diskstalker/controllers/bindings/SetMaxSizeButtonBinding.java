package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.NodeData;

public class SetMaxSizeButtonBinding extends AbstractButtonBooleanBinding {
    public SetMaxSizeButtonBinding(
            MainController mainController,
            TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel
    ) {
        super(
                () -> (
                        !mainController.isMainFolder(selectionModel.getSelectedItem())
                                || mainController.getMaxSizeField().getText().isEmpty()
                ),
                selectionModel.selectedItemProperty(),
                mainController.getMaxSizeField().textProperty()
        );
    }
}
