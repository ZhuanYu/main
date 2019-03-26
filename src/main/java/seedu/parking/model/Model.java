package seedu.parking.model;

import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.CarparkContainsKeywordsPredicate;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluate to true */
    Predicate<Carpark> PREDICATE_SHOW_ALL_CARPARK = unused -> true;

    /** Clears existing backing model and replaces with the provided new data. */
    void resetData(ReadOnlyCarparkFinder newData);

    /** Returns the CarparkFinder */
    ReadOnlyCarparkFinder getCarparkFinder();

    /**
     * Returns true if a car park with the same identity as {@code carpark} exists in the car park finder.
     */
    boolean hasCarpark(Carpark carpark);

    /**
     * Deletes the given car park.
     * The car park must exist in the car park finder.
     */
    void deleteCarpark(Carpark target);

    /**
     * Adds the given car park.
     * {@code carpark} must not already exist in the car park finder.
     */
    void addCarpark(Carpark carpark);

    /**
     * Replaces the given car park {@code target} with {@code editedCarpark}.
     * {@code target} must exist in the car park finder.
     */
    void updateCarpark(Carpark target, Carpark editedCarpark);

    /**
     * Updates with a list of car parks {@code listCarpark}.
     */
    void loadCarpark(List<Carpark> listCarpark);

    /** Returns an unmodifiable view of the filtered car park list */
    ObservableList<Carpark> getFilteredCarparkList();

    /**
     * Updates the filter of the filtered car park list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredCarparkList(Predicate<Carpark> predicate);


    void updateLastPredicateUsedByFindCommand(CarparkContainsKeywordsPredicate predicate);

    CarparkContainsKeywordsPredicate getLastPredicateUsedByFindCommand();

    // Todo: Calculate command add java comment
    Carpark getCarparkFromFilteredList(int index);

    /**
     * Returns true if the model has previous car park finder states to restore.
     */
    boolean canUndoCarparkFinder();

    /**
     * Returns true if the model has undone car park finder states to restore.
     */
    boolean canRedoCarparkFinder();

    /**
     * Restores the model's car park finder to its previous state.
     */
    void undoCarparkFinder();

    /**
     * Restores the model's car park finder to its previously undone state.
     */
    void redoCarparkFinder();

    /**
     * Saves the current car park finder state for undo/redo.
     */
    void commitCarparkFinder();

    /**
     * Compares the current car park finder state to its previous state.
     */
    int compareCarparkFinder();
}
