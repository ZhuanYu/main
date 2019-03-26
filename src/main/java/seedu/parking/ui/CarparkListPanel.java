package seedu.parking.ui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.parking.commons.core.LogsCenter;
import seedu.parking.commons.events.ui.CarparkPanelSelectionChangedEvent;
import seedu.parking.commons.events.ui.JumpToListRequestEvent;
import seedu.parking.commons.events.ui.NoSelectionRequestEvent;
import seedu.parking.commons.events.ui.TimeIntervalChangeEvent;
import seedu.parking.model.carpark.Carpark;

/**
 * Panel containing the list of car parks.
 */
public class CarparkListPanel extends UiPart<Region> {
    private static final String FXML = "CarparkListPanel.fxml";
    private static int selectIndex = -1;
    private static Carpark selectedCarpark = null;
    private static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private static int timeInterval = 0;
    private final Logger logger = LogsCenter.getLogger(CarparkListPanel.class);

    @FXML
    private ListView<Carpark> carparkListView;

    public CarparkListPanel(ObservableList<Carpark> carparkList) {
        super(FXML);
        setConnections(carparkList);
        registerAsAnEventHandler(this);
        carparkListView.setFixedCellSize(270.0);
    }

    private void setConnections(ObservableList<Carpark> carparkList) {
        carparkListView.setItems(carparkList);
        carparkListView.setCellFactory(listView -> new CarparkListViewCell());
        setEventHandlerForSelectionChangeEvent();
    }

    private void setEventHandlerForSelectionChangeEvent() {
        carparkListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.fine("Selection in car park list panel changed to : '" + newValue + "'");
                        selectIndex = carparkListView.getSelectionModel().getSelectedIndex();
                        selectedCarpark = carparkListView.getSelectionModel().getSelectedItem();
                        raise(new CarparkPanelSelectionChangedEvent(newValue));
                    }
                });
    }

    /**
     * Scrolls to the {@code CarparkCard} at the {@code index} and selects it.
     */
    private void scrollTo(int index) {
        Platform.runLater(() -> {
            carparkListView.scrollTo(index);
            carparkListView.layout();
            carparkListView.getSelectionModel().clearAndSelect(index);
            carparkListView.getFocusModel().focus(index);
        });
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        scrollTo(event.targetIndex);
    }

    @Subscribe
    private void handleNoSelectionRequestEvent(NoSelectionRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        selectIndex = -1;
        selectedCarpark = null;
        timer.shutdownNow();
    }

    @Subscribe
    private void handleTimeIntervalChangeEvent(TimeIntervalChangeEvent event) {
        timeInterval = event.value;
    }

    /**
     * Get the selected car park from the list view.
     * @return the index of the selected car park.
     */
    public static int getSelectedIndex() {
        return selectIndex;
    }

    public static void setSelectIndex(int newIndex) {
        selectIndex = newIndex;
    }

    public static void setSelectedCarpark(Carpark newCarpark) {
        selectedCarpark = newCarpark;
    }

    public static Carpark getSelectedCarpark() {
        return selectedCarpark;
    }

    public static int getTimeInterval() {
        return timeInterval;
    }

    public static void setTimeInterval(int newInterval) {
        timeInterval = newInterval;
    }

    public static ScheduledExecutorService getTimer() {
        return timer;
    }

    public static void setTimer() {
        timer.shutdownNow();
        timer = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Carpark} using a {@code CarparkCard}.
     */
    class CarparkListViewCell extends ListCell<Carpark> {
        @Override
        protected void updateItem(Carpark carpark, boolean empty) {
            Platform.runLater(() -> {
                super.updateItem(carpark, empty);

                if (empty || carpark == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(new CarparkCard(carpark, getIndex() + 1).getRoot());
                }
            });
        }
    }

}
