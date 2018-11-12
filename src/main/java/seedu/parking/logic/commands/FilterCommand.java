package seedu.parking.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.sun.istack.Nullable;

import seedu.parking.commons.core.EventsCenter;
import seedu.parking.commons.core.Messages;
import seedu.parking.commons.events.ui.FilterResultChangedEvent;
import seedu.parking.logic.CommandHistory;
import seedu.parking.logic.commands.exceptions.CommandException;
import seedu.parking.logic.parser.CarparkTypeParameter;
import seedu.parking.logic.parser.FreeParkingParameter;
import seedu.parking.logic.parser.ParkingSystemTypeParameter;
import seedu.parking.model.Model;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.CarparkContainsKeywordsPredicate;
import seedu.parking.model.carpark.CarparkFilteringPredicate;

/**
 * Filters the list of car parks returned by the previous find command with the use of flags
 */
public class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";
    public static final String FORMAT = "filter f/ DAY S_TIME E_TIME ct/ CP_TYPE ps/ S_TYPE a/ n/ st/";
    public static final String FREEPARKING_FIRST_ARG = "DAY";
    public static final String FREEPARKING_SECOND_ARG = "S_TIME";
    public static final String FREEPARKING_THIRD_ARG = "E_TIME";
    public static final String CARPARKTYPE_ARG = "CP_TYPE";
    public static final String SYSTEMTYPE_ARG = "S_TYPE";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Filters the list of car parks returned by the previous find command with the use of flags.\n"
            + "Multiple flags can be used in any order.\n"
            + "Valid flags:\n"
            + "> Available: a/ \n"
            + "> Night Parking: n/ \n"
            + "> Short-term Parking: st/ \n"
            + "> Free Parking: f/ [day] [start time] [end time]     Example: filter f/ sun 7.30am 8.30pm\n"
            + "> Car Park Type: ct/ [car park type]     Example: filter ct/ basement\n"
            + "> Parking System Type: ps/ [parking system type]     Example: filter ps/ coupon\n";

    private Predicate predicate;
    private final List<String> flagList;
    private final FreeParkingParameter freeParkingParameter;
    private final CarparkTypeParameter carparkTypeParameter;
    private final ParkingSystemTypeParameter parkingSystemTypeParameter;

    /**
     * Creates a FilterCommand with the relevant flags
     */
    public FilterCommand(List<String> flagList, @Nullable FreeParkingParameter freeParkingParameter,
        @Nullable CarparkTypeParameter carparkTypeParameter,
        @Nullable ParkingSystemTypeParameter parkingSystemTypeParameter) {
        this.predicate = null;
        this.flagList = flagList;
        this.freeParkingParameter = freeParkingParameter;
        this.carparkTypeParameter = carparkTypeParameter;
        this.parkingSystemTypeParameter = parkingSystemTypeParameter;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);

        CarparkContainsKeywordsPredicate locationPredicate = model.getLastPredicateUsedByFindCommand();
        if (locationPredicate == null) {
            throw new CommandException(Messages.MESSAGE_FINDCOMMAND_NEEDS_TO_BE_EXECUTED_FIRST);
        }

        List<String> locationKeywords = locationPredicate.getKeywords();
        predicate = new CarparkFilteringPredicate(locationKeywords, flagList, freeParkingParameter,
                carparkTypeParameter, parkingSystemTypeParameter);

        model.updateFilteredCarparkList(predicate);

        EventsCenter.getInstance().post(new FilterResultChangedEvent(
                model.getFilteredCarparkList().toArray(new Carpark[]{})));
        return new CommandResult(
                String.format(Messages.MESSAGE_CARPARKS_LISTED_OVERVIEW, model.getFilteredCarparkList().size()));
    }

    // Please fix the last line, it will give NULLPOINTEREXCEPTION because predicate is initialized as null value.
    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof FilterCommand)) {
            return false;
        }

        FilterCommand otherFilterCommand = (FilterCommand) other;

        Collections.sort(flagList);
        Collections.sort(otherFilterCommand.flagList);
        boolean checkFlagList = flagList.equals(otherFilterCommand.flagList); // flagList is always present

        boolean checkPredicate = ((predicate == null && otherFilterCommand.predicate == null)
                || predicate.equals(otherFilterCommand.predicate));

        boolean checkFreeParkingParameter = (freeParkingParameter == null
                && otherFilterCommand.freeParkingParameter == null)
                || (freeParkingParameter.getDay().equals(otherFilterCommand.freeParkingParameter.getDay())
                && freeParkingParameter.getStartTime().equals(otherFilterCommand.freeParkingParameter.getStartTime())
                && freeParkingParameter.getEndTime().equals(otherFilterCommand.freeParkingParameter.getEndTime()));

        boolean checkCarparkTypeParameter = (carparkTypeParameter == null
                && otherFilterCommand.carparkTypeParameter == null)
                || carparkTypeParameter.getCarparkType()
                .equals(otherFilterCommand.carparkTypeParameter.getCarparkType());

        boolean checkParkingSystemTypeParameter = (parkingSystemTypeParameter == null
                && otherFilterCommand.parkingSystemTypeParameter == null)
                || parkingSystemTypeParameter.getParkingSystemType()
                .equals(otherFilterCommand.parkingSystemTypeParameter.getParkingSystemType());

        return checkFlagList && checkPredicate && checkFreeParkingParameter && checkCarparkTypeParameter
                && checkParkingSystemTypeParameter;
    }
}
