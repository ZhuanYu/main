package seedu.parking.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;

import seedu.parking.commons.core.EventsCenter;
import seedu.parking.commons.events.ui.TimeIntervalChangeEvent;
import seedu.parking.logic.CommandHistory;
import seedu.parking.logic.NotifyTimeTask;
import seedu.parking.logic.commands.exceptions.CommandException;
import seedu.parking.model.Model;
import seedu.parking.model.carpark.CarparkNumber;
import seedu.parking.ui.CarparkListPanel;

/**
 * Notifies when to get the car park information from the API.
 */
public class NotifyCommand extends Command {

    public static final String COMMAND_WORD = "notify";
    public static final String FORMAT = "notify SECONDS";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Set when to receive notification about the lot availability of a car park.\n"
            + "Parameters: SECONDS\n0 to disable, range is 10 to 600 seconds (decimals not allowed)\n"
            + "Example: " + COMMAND_WORD + " 10";

    public static final String MESSAGE_SUCCESS = "Notification enabled for car park %1$s"
            + "\nInterval: every %2$d seconds";
    public static final String MESSAGE_UP_CHANGE = "Car park %1$s has %2$d lot(s) available"
            + "\n%3$d lot(s) freed since last check"
            + "\nInterval: every %4$d seconds";
    public static final String MESSAGE_DOWN_CHANGE = "Car park %1$s has %2$d lot(s) available"
            + "\n%3$d lot(s) taken since last check"
            + "\nInterval: every %4$d seconds";
    public static final String MESSAGE_NO_CHANGE = "Car park %1$s has %2$s lot(s) available"
            + "\nInterval: every %3$d seconds";
    public static final String MESSAGE_ERROR = "Cannot notify without selecting a car park first";
    public static final String MESSAGE_ERROR_CARPARK = "Unable to retrieve car park information from data.gov.sg"
            + "\nPlease check your internet connection and try again"
            + "\nDisabling notification...";
    public static final String MESSAGE_ERROR_NODATA = "Unable to retrieve car park information from data.gov.sg"
            + "\nUnfortunately, the data is not available. We apologise for any inconvenience caused"
            + "\nDisabling notification...";
    public static final String MESSAGE_ERROR_OFF = "Notification already disabled";
    public static final String MESSAGE_OFF = "Notification disabled";
    public static final String MESSAGE_INVALID_NOTIFY_INDEX = "Unable to notify due to selected index out of range"
            + "\nDisabling notification...";

    private final int targetTime;

    public NotifyCommand(int targetTime) {
        this.targetTime = targetTime;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);

        if (targetTime > 0) {
            NotifyTimeTask task = new NotifyTimeTask(model, targetTime);
            CarparkListPanel.setTimer();
            CarparkListPanel.getTimer().scheduleAtFixedRate(task, targetTime * 50, targetTime * 1000,
                    TimeUnit.MILLISECONDS);
            EventsCenter.getInstance().post(new TimeIntervalChangeEvent(targetTime));
            CarparkNumber selectedNumber = CarparkListPanel.getSelectedCarpark().getCarparkNumber();
            return new CommandResult(String.format(MESSAGE_SUCCESS, selectedNumber.toString(), targetTime));
        } else {
            if (CarparkListPanel.getTimeInterval() > 0) {
                EventsCenter.getInstance().post(new TimeIntervalChangeEvent(0));
                CarparkListPanel.getTimer().shutdownNow();
                return new CommandResult(MESSAGE_OFF);
            } else {
                throw new CommandException(MESSAGE_ERROR_OFF);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof NotifyCommand // instanceof handles nulls
                && targetTime == ((NotifyCommand) other).targetTime); // state check
    }
}
