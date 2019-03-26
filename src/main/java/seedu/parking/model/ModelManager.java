package seedu.parking.model;

import static java.util.Objects.requireNonNull;
import static seedu.parking.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.parking.commons.core.ComponentManager;
import seedu.parking.commons.core.LogsCenter;
import seedu.parking.commons.events.model.CarparkFinderChangedEvent;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.CarparkContainsKeywordsPredicate;

/**
 * Represents the in-memory model of the car park finder data.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedCarparkFinder versionedCarparkFinder;
    private final FilteredList<Carpark> filteredCarparks;

    private CarparkContainsKeywordsPredicate lastPredicateUsedByFindCommand;

    /**
     * Initializes a ModelManager with the given carparkFinder and userPrefs.
     */
    public ModelManager(ReadOnlyCarparkFinder carparkFinder, UserPrefs userPrefs) {
        super();
        requireAllNonNull(carparkFinder, userPrefs);

        logger.fine("Initializing with car park finder: " + carparkFinder + " and user prefs " + userPrefs);

        versionedCarparkFinder = new VersionedCarparkFinder(carparkFinder);
        filteredCarparks = new FilteredList<>(versionedCarparkFinder.getCarparkList());
    }

    public ModelManager() {
        this(new CarparkFinder(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyCarparkFinder newData) {
        versionedCarparkFinder.resetData(newData);
        indicateCarparkFinderChanged();
    }

    @Override
    public ReadOnlyCarparkFinder getCarparkFinder() {
        return versionedCarparkFinder;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateCarparkFinderChanged() {
        raise(new CarparkFinderChangedEvent(versionedCarparkFinder));
    }

    @Override
    public boolean hasCarpark(Carpark carpark) {
        requireNonNull(carpark);
        return versionedCarparkFinder.hasCarpark(carpark);
    }

    @Override
    public void deleteCarpark(Carpark target) {
        versionedCarparkFinder.removeCarpark(target);
        indicateCarparkFinderChanged();
    }

    @Override
    public void addCarpark(Carpark carpark) {
        versionedCarparkFinder.addCarpark(carpark);
        updateFilteredCarparkList(PREDICATE_SHOW_ALL_CARPARK);
        indicateCarparkFinderChanged();
    }

    @Override
    public void updateCarpark(Carpark target, Carpark editedCarpark) {
        requireAllNonNull(target, editedCarpark);

        versionedCarparkFinder.updateCarpark(target, editedCarpark);
        indicateCarparkFinderChanged();
    }

    @Override
    public void loadCarpark(List<Carpark> listCarkpark) {
        versionedCarparkFinder.setCarparks(listCarkpark);
        updateFilteredCarparkList(PREDICATE_SHOW_ALL_CARPARK);
        indicateCarparkFinderChanged();
    }

    // Todo: Calculate command
    @Override
    public Carpark getCarparkFromFilteredList(int index) {
        return filteredCarparks.get(index);
    }

    //=========== Filtered Car Park List Accessors ===========================================================

    /**
     * Returns an unmodifiable view of the list of {@code Carpark} backed by the internal list of
     * {@code versionedCarparkFinder}
     */
    @Override
    public ObservableList<Carpark> getFilteredCarparkList() {
        return FXCollections.unmodifiableObservableList(filteredCarparks);
    }

    @Override
    public void updateFilteredCarparkList(Predicate<Carpark> predicate) {
        requireNonNull(predicate);
        filteredCarparks.setPredicate(predicate);
    }

    //=========== Last Predicate Used by FindCommand =========================================================

    @Override
    public void updateLastPredicateUsedByFindCommand(CarparkContainsKeywordsPredicate predicate) {
        lastPredicateUsedByFindCommand = predicate;
    }

    @Override
    public CarparkContainsKeywordsPredicate getLastPredicateUsedByFindCommand() {
        return lastPredicateUsedByFindCommand;
    }

    //=========== Undo/Redo =================================================================================

    @Override
    public boolean canUndoCarparkFinder() {
        return versionedCarparkFinder.canUndo();
    }

    @Override
    public boolean canRedoCarparkFinder() {
        return versionedCarparkFinder.canRedo();
    }

    @Override
    public void undoCarparkFinder() {
        versionedCarparkFinder.undo();
        indicateCarparkFinderChanged();
    }

    @Override
    public void redoCarparkFinder() {
        versionedCarparkFinder.redo();
        indicateCarparkFinderChanged();
    }

    @Override
    public void commitCarparkFinder() {
        versionedCarparkFinder.commit();
    }

    @Override
    public int compareCarparkFinder() {
        return versionedCarparkFinder.compare();
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return versionedCarparkFinder.equals(other.versionedCarparkFinder)
                && filteredCarparks.equals(other.filteredCarparks);
    }

}
