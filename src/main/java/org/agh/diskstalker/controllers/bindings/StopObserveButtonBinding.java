package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.NodeData;

public class StopObserveButtonBinding extends AbstractButtonBooleanBinding {
    public StopObserveButtonBinding(MainController mainController, TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel) {
        super(
                () -> !mainController.isMainFolder(selectionModel.getSelectedItem()),
                selectionModel.selectedItemProperty()
        );
    }
}
