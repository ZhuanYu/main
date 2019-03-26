package seedu.parking.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.UniqueCarparkList;

/**
 * Wraps all data at the parking-book level
 * Duplicates are not allowed (by .isSameCarpark comparison)
 */
public class CarparkFinder implements ReadOnlyCarparkFinder {

    private final UniqueCarparkList carparks;

    /*
     * The 'unusual' code block below is an non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        carparks = new UniqueCarparkList();
    }

    public CarparkFinder() {}

    /**
     * Creates an CarparkFinder using the car parks in the {@code toBeCopied}
     */
    public CarparkFinder(ReadOnlyCarparkFinder toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the car park list with {@code carparks}.
     * {@code carparks} must not contain duplicate car parks.
     */
    public void setCarparks(List<Carpark> listCarparks) {
        carparks.setCarparks(listCarparks);
    }

    /**
     * Resets the existing data of this {@code CarparkFinder} with {@code newData}.
     */
    public void resetData(ReadOnlyCarparkFinder newData) {
        requireNonNull(newData);

        setCarparks(newData.getCarparkList());
    }

    //// car park-level operations

    /**
     * Returns true if a car park with the same identity as {@code carpark} exists in the car park finder.
     */
    public boolean hasCarpark(Carpark carpark) {
        requireNonNull(carpark);
        return carparks.contains(carpark);
    }

    /**
     * Adds a car park to the car park finder.
     * The car park must not already exist in the car park finder.
     */
    public void addCarpark(Carpark c) {
        carparks.add(c);
    }

    /**
     * Replaces the given car park {@code target} in the list with {@code editedCarpark}.
     * {@code target} must exist in the car park finder.
     */
    public void updateCarpark(Carpark target, Carpark editedCarpark) {
        requireNonNull(editedCarpark);

        carparks.setCarpark(target, editedCarpark);
    }

    /**
     * Removes {@code key} from this {@code CarparkFinder}.
     * {@code key} must exist in the car park finder.
     */
    public void removeCarpark(Carpark key) {
        carparks.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return carparks.asUnmodifiableObservableList().size() + " car parks";
        // TODO: refine later
    }

    @Override
    public ObservableList<Carpark> getCarparkList() {
        return carparks.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof CarparkFinder // instanceof handles nulls
                && carparks.equals(((CarparkFinder) other).carparks));
    }

    @Override
    public int hashCode() {
        return carparks.hashCode();
    }
}
