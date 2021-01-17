package org.agh.diskstalker.controllers.bindings;

import com.sun.javafx.binding.Logging;
import com.sun.javafx.collections.ImmutableObservableList;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.Callable;

public class AbstractButtonBinding extends BooleanBinding {
    private final Observable[] dependencies;
    private final Callable<Boolean> func;

    public AbstractButtonBinding(Callable<Boolean> func, Observable... dependencies) {
        bind(dependencies);
        this.dependencies = dependencies;
        this.func = func;
    }

    @Override
    protected boolean computeValue() {
        try {
            return func.call();
        } catch (Exception e) {
            Logging.getLogger().warning("Exception while evaluating binding", e);
            return false;
        }
    }

    @Override
    public void dispose() {
        super.unbind(dependencies);
    }

    @Override
    public ObservableList<?> getDependencies() {
        return ((dependencies == null) || (dependencies.length == 0)) ?
                FXCollections.emptyObservableList()
                : (dependencies.length == 1) ?
                FXCollections.singletonObservableList(dependencies[0])
                : new ImmutableObservableList<Observable>(dependencies);
    }
}
