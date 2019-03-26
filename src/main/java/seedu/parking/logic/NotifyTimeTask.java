package seedu.parking.logic;

import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_DOWN_CHANGE;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_ERROR;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_ERROR_CARPARK;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_ERROR_NODATA;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_INVALID_NOTIFY_INDEX;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_NO_CHANGE;
import static seedu.parking.logic.commands.NotifyCommand.MESSAGE_UP_CHANGE;
import static seedu.parking.model.Model.PREDICATE_SHOW_ALL_CARPARK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;

import seedu.parking.commons.core.EventsCenter;
import seedu.parking.commons.core.LogsCenter;
import seedu.parking.commons.core.index.Index;
import seedu.parking.commons.events.model.DataFetchExceptionEvent;
import seedu.parking.commons.events.ui.JumpToListRequestEvent;
import seedu.parking.commons.events.ui.NewResultAvailableEvent;
import seedu.parking.commons.util.GsonUtil;
import seedu.parking.logic.commands.exceptions.CommandException;
import seedu.parking.model.Model;
import seedu.parking.model.carpark.Address;
import seedu.parking.model.carpark.Carpark;
import seedu.parking.model.carpark.CarparkNumber;
import seedu.parking.model.carpark.CarparkType;
import seedu.parking.model.carpark.Coordinate;
import seedu.parking.model.carpark.FreeParking;
import seedu.parking.model.carpark.LotsAvailable;
import seedu.parking.model.carpark.NightParking;
import seedu.parking.model.carpark.PostalCode;
import seedu.parking.model.carpark.ShortTerm;
import seedu.parking.model.carpark.TotalLots;
import seedu.parking.model.carpark.TypeOfParking;
import seedu.parking.ui.CarparkListPanel;

/**
 * A class to handle timer task but checking if its running or not.
 */
public class NotifyTimeTask extends TimerTask {
    private Model model;
    private int targetTime;

    private final Logger logger = LogsCenter.getLogger(NotifyTimeTask.class);

    public NotifyTimeTask(Model model, int targetTime) {
        this.model = model;
        this.targetTime = targetTime;
    }

    @Override
    public void run() {
        try {
            logger.info("Task started");
            int validity = CarparkListPanel.getSelectedIndex();
            if (validity < 0) {
                logger.info("Invalid index");
                throw new CommandException(MESSAGE_ERROR);
            }

            Index notifyIndex = Index.fromZeroBased(validity);
            List<Carpark> lastShownList = model.getFilteredCarparkList();

            if (notifyIndex.getZeroBased() >= lastShownList.size()) {
                logger.info("Invalid displayed index");
                throw new CommandException(MESSAGE_INVALID_NOTIFY_INDEX);
            }

            Carpark carparkToNotify = lastShownList.get(notifyIndex.getZeroBased());
            CarparkNumber selectedNumber = carparkToNotify.getCarparkNumber();
            List<String> updateData = new ArrayList<>(GsonUtil.getSelectedCarparkInfo(
                    selectedNumber.toString()));

            Carpark notifiedCarpark = createNotifyCarpark(carparkToNotify, updateData);

            if (CarparkListPanel.getTimeInterval() > 0) {
                int newValue = Integer.parseInt(updateData.get(1));
                int oldValue = Integer.parseInt(carparkToNotify.getLotsAvailable().value);
                int diffValue = newValue - oldValue;

                if (diffValue == 0) {
                    EventsCenter.getInstance().post(new JumpToListRequestEvent(notifyIndex));
                    EventsCenter.getInstance().post(new NewResultAvailableEvent(
                            String.format(MESSAGE_NO_CHANGE, selectedNumber.toString(), newValue, targetTime)));
                } else {
                    model.updateCarpark(carparkToNotify, notifiedCarpark);
                    model.updateFilteredCarparkList(PREDICATE_SHOW_ALL_CARPARK);
                    model.commitCarparkFinder();
                    EventsCenter.getInstance().post(new JumpToListRequestEvent(notifyIndex));

                    EventsCenter.getInstance().post(new NewResultAvailableEvent(
                            String.format(diffValue > 0 ? MESSAGE_UP_CHANGE : MESSAGE_DOWN_CHANGE,
                                    selectedNumber.toString(), newValue, Math.abs(diffValue),
                                    targetTime)));
                }
                logger.info("Lots Available: " + updateData.get(1) + " Total Lots: " + updateData.get(2));
            }
        } catch (CommandException e) {
            CarparkListPanel.getTimer().shutdownNow();
            if (CarparkListPanel.getTimeInterval() > 0) {
                EventsCenter.getInstance().post(new DataFetchExceptionEvent(e));
            }
        } catch (IOException e) {
            CarparkListPanel.getTimer().shutdownNow();
            if (CarparkListPanel.getTimeInterval() > 0) {
                EventsCenter.getInstance().post(new DataFetchExceptionEvent(
                        new CommandException(MESSAGE_ERROR_CARPARK)));
            }
        } catch (Exception e) {
            CarparkListPanel.getTimer().shutdownNow();
            if (CarparkListPanel.getTimeInterval() > 0) {
                EventsCenter.getInstance().post(new DataFetchExceptionEvent(
                        new CommandException(MESSAGE_ERROR_NODATA)));
            }
        }
    }

    /**
     * Creates and returns a {@code Carpark} with the details of {@code carparkToNotify}
     * edited with {@code updateData}.
     */
    private Carpark createNotifyCarpark(Carpark carparkToNotify, List<String> updateData) {
        assert carparkToNotify != null;

        return new Carpark(new Address(carparkToNotify.getAddress().toString()),
                new CarparkNumber(carparkToNotify.getCarparkNumber().toString()),
                new CarparkType(carparkToNotify.getCarparkType().toString()),
                new Coordinate(carparkToNotify.getCoordinate().toString()),
                new FreeParking(carparkToNotify.getFreeParking().toString()),
                new LotsAvailable(updateData.get(1)),
                new NightParking(carparkToNotify.getNightParking().toString()),
                new ShortTerm(carparkToNotify.getShortTerm().toString()),
                new TotalLots(updateData.get(2)),
                new TypeOfParking(carparkToNotify.getTypeOfParking().toString()),
                new PostalCode(carparkToNotify.getPostalCode().toString()),
                null);
    }
}
