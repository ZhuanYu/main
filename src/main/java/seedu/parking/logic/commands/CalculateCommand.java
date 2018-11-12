package seedu.parking.logic.commands;

import static java.util.Objects.requireNonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import seedu.parking.commons.core.Messages;
import seedu.parking.logic.CommandHistory;
import seedu.parking.logic.commands.exceptions.CommandException;
import seedu.parking.model.Model;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.CarparkIsOfNumberPredicate;

/**
 * Calculates the cost of parking at a car park.
 */
public class CalculateCommand extends Command {
    public static final String COMMAND_WORD = "calculate";
    public static final String FORMAT = "calculate CPNO DAY S_TIME E_TIME";
    public static final String FIRST_ARG = "CPNO";
    public static final String SECOND_ARG = "DAY";
    public static final String THIRD_ARG = "S_TIME";
    public static final String FOURTH_ARG = "E_TIME";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Calculates the cost of parking at a particular car park for a specified time period.\n"
            + "Parameters: CARPARK_NUMBER DAY START_TIME END_TIME\n"
            + "Example: " + COMMAND_WORD + " TJ39 " + "fri " + "7.30am " + "3.30pm ";

    private CarparkIsOfNumberPredicate predicate;
    private String carparkNumber;
    private String day;
    private Date inputStart;
    private Date inputEnd;

    /**
     * Creates a CalculateCommand with the relevant flags and predicate.
     */
    public CalculateCommand(String carparkNumber, String day, Date inputStart, Date inputEnd) {
        this.predicate = null;
        this.carparkNumber = carparkNumber;
        this.day = day;
        this.inputStart = inputStart;
        this.inputEnd = inputEnd;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {

        requireNonNull(model);

        this.predicate = new CarparkIsOfNumberPredicate(carparkNumber);
        model.updateFilteredCarparkList(predicate);

        Carpark targetCarpark;
        try {
            targetCarpark = model.getCarparkFromFilteredList(0);
        } catch (IndexOutOfBoundsException e) {
            throw new CommandException(Messages.MESSAGE_INVALID_CARPARK_NAME);
        }

        double cost = 0;
        try {
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh.mmaa");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("hhaa");

            // check that there is short term parking
            String shortTermParkingTime = targetCarpark.getShortTerm().value;
            if (shortTermParkingTime.equals("NO")) {
                throw new CommandException(Messages.MESSAGE_NO_SHORT_TERM_PARKING);
            }

            // check for invalid start/end time
            if (!shortTermParkingTime.equals("WHOLE DAY")) {
                String[] startAndEndTime = shortTermParkingTime.split("-");
                SimpleDateFormat dateFormat;
                Date opening;
                try {
                    dateFormat = dateFormat1;
                    opening = dateFormat.parse(startAndEndTime[0]);
                } catch (ParseException e) {
                    dateFormat = dateFormat2;
                    opening = dateFormat.parse(startAndEndTime[0]);
                }

                Date closing;
                try {
                    dateFormat = dateFormat1;
                    closing = dateFormat.parse(startAndEndTime[1]);
                } catch (ParseException e) {
                    dateFormat = dateFormat2;
                    closing = dateFormat.parse(startAndEndTime[1]);
                }

                if (inputStart.before(opening) || inputEnd.after(closing)) {
                    throw new CommandException(Messages.MESSAGE_INVALID_START_OR_END_TIME);
                }
            }

            // check if car park has free parking + calculate cost
            boolean noFreeParking = targetCarpark.getFreeParking().value.equals("NO") || !day.equals("SUN");
            if (noFreeParking) {
                long diff = inputEnd.getTime() - inputStart.getTime();
                long minute = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
                double halfHour = Math.ceil(minute / 30.00);
                cost = halfHour * 0.60;
            } else {
                String timePeriod = targetCarpark.getFreeParking().value;
                String[] timePeriodArray = timePeriod.split("\\s+");
                String time = timePeriodArray[timePeriodArray.length - 1];
                String[] freeParkingStartAndEndTime = time.split("-");

                Date freeParkingOpening = dateFormat2.parse(freeParkingStartAndEndTime[0]);
                Date freeParkingClosing = dateFormat1.parse(freeParkingStartAndEndTime[1]);

                double cost1 = 0;
                double cost2 = 0;

                if (inputStart.before(freeParkingOpening)) {
                    long diff1 = freeParkingOpening.getTime() - inputStart.getTime();
                    long minute1 = TimeUnit.MINUTES.convert(diff1, TimeUnit.MILLISECONDS);
                    double halfHour1 = Math.ceil(minute1 / 30.00);
                    cost1 = halfHour1 * 0.60;
                }

                if (inputEnd.after(freeParkingClosing)) {
                    long diff2 = inputEnd.getTime() - freeParkingClosing.getTime();
                    long minute2 = TimeUnit.MINUTES.convert(diff2, TimeUnit.MILLISECONDS);
                    double halfHour2 = Math.ceil(minute2 / 30.00);
                    cost2 = halfHour2 * 0.60;
                }
                cost = cost1 + cost2;
            }
        } catch (ParseException e) {
            throw new CommandException(Messages.MESSAGE_ERROR_PARSING_CARPARK_INFO);
        }

        return new CommandResult(
                String.format(Messages.MESSAGE_COST_OF_PARKING, cost));
    }

    // Please fix the last line, it will give NULLPOINTEREXCEPTION because predicate is initialized as null value.
    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof CalculateCommand)) {
            return false;
        }

        CalculateCommand otherFilterCommand = (CalculateCommand) other;
        boolean checkCarparkNumber = carparkNumber.equals(otherFilterCommand.carparkNumber);
        boolean checkDay = day.equals(otherFilterCommand.day);
        boolean checkStartTime = inputStart.equals(otherFilterCommand.inputStart);
        boolean checkEndTime = inputEnd.equals(otherFilterCommand.inputEnd);
        boolean checkPredicate = ((predicate == null && otherFilterCommand.predicate == null)
                || predicate.equals(otherFilterCommand.predicate));

        return checkCarparkNumber && checkDay && checkStartTime && checkEndTime && checkPredicate;
    }
}
