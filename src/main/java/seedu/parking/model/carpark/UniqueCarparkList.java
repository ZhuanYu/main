package seedu.parking.model.carpark;

import static java.util.Objects.requireNonNull;
import static seedu.parking.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.parking.model.carpark.exceptions.CarparkNotFoundException;
import seedu.parking.model.carpark.exceptions.DuplicateCarparkException;

/**
 * A list of car parks that enforces uniqueness between its elements and does not allow nulls.
 * A car park is considered unique by comparing using {@code Carpark#isSameCarpark(Carpark)}.
 * As such, adding and updating of car parks uses Carpark#isSameCarpark(Carpark) for equality so as to ensure
 * that the car park being added or updated is unique in terms of identity in the UniqueCarparkList.
 * However, the removal of a car park uses Carpark#equals(Object) so as to ensure that the car park with
 * exactly the same fields will be removed.
 *
 * Supports a minimal set of list operations.
 *
 * @see Carpark#isSameCarpark(Carpark)
 */
public class UniqueCarparkList implements Iterable<Carpark> {
    private final ObservableList<Carpark> internalList = FXCollections.observableArrayList();

    /**
     * Returns true if the list contains an equivalent car park as the given argument.
     */
    public boolean contains(Carpark toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameCarpark);
    }

    /**
     * Adds a car park to the list.
     * The car park must not already exist in the list.
     */
    public void add(Carpark toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateCarparkException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the car park {@code target} in the list with {@code editedCarpark}.
     * {@code target} must exist in the list.
     */
    public void setCarpark(Carpark target, Carpark editedCarpark) {
        requireAllNonNull(target, editedCarpark);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new CarparkNotFoundException();
        }

        internalList.set(index, editedCarpark);
    }

    /**
     * Removes the equivalent car park from the list.
     * The car park must exist in the list.
     */
    public void remove(Carpark toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new CarparkNotFoundException();
        }
    }

    public void setCarparks(UniqueCarparkList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code car parks}.
     * {@code car parks} must not contain duplicate car parks.
     */
    public void setCarparks(List<Carpark> carparks) {
        requireAllNonNull(carparks);
        if (!carparksAreUnique(carparks)) {
            throw new DuplicateCarparkException();
        }
        internalList.setAll(carparks);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Carpark> asUnmodifiableObservableList() {
        return FXCollections.unmodifiableObservableList(internalList);
    }

    @Override
    public Iterator<Carpark> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueCarparkList // instanceof handles nulls
                && internalList.equals(((UniqueCarparkList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    /**
     * Returns true if {@code carparks} contains only unique car parks.
     */
    private boolean carparksAreUnique(List<Carpark> carparks) {
        for (int i = 0; i < carparks.size() - 1; i++) {
            for (int j = i + 1; j < carparks.size(); j++) {
                if (carparks.get(i).isSameCarpark(carparks.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
