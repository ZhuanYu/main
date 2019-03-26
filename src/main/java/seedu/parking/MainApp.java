package seedu.parking;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.parking.commons.core.Config;
import seedu.parking.commons.core.EventsCenter;
import seedu.parking.commons.core.LogsCenter;
import seedu.parking.commons.core.Version;
import seedu.parking.commons.events.ui.ExitAppRequestEvent;
import seedu.parking.commons.exceptions.DataConversionException;
import seedu.parking.commons.util.ConfigUtil;
import seedu.parking.commons.util.StringUtil;
import seedu.parking.logic.Logic;
import seedu.parking.logic.LogicManager;
import seedu.parking.model.CarparkFinder;
import seedu.parking.model.Model;
import seedu.parking.model.ModelManager;
import seedu.parking.model.ReadOnlyCarparkFinder;
import seedu.parking.model.UserPrefs;
import seedu.parking.storage.CarparkFinderStorage;
import seedu.parking.storage.JsonUserPrefsStorage;
import seedu.parking.storage.Storage;
import seedu.parking.storage.StorageManager;
import seedu.parking.storage.UserPrefsStorage;
import seedu.parking.storage.XmlCarparkFinderStorage;
import seedu.parking.ui.Ui;
import seedu.parking.ui.UiManager;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 4, 0, false);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;
    protected UserPrefs userPrefs;


    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing Car Park Finder ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        userPrefs = initPrefs(userPrefsStorage);
        CarparkFinderStorage carparkFinderStorage = new XmlCarparkFinderStorage(userPrefs.getCarparkFinderFilePath());
        storage = new StorageManager(carparkFinderStorage, userPrefsStorage);

        initLogging(config);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model);

        ui = new UiManager(logic, config, userPrefs);

        initEventsCenter();
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s car park finder and {@code userPrefs}. <br>
     * The data from the sample car park finder will be used instead if {@code storage}'s car park finder is not found,
     * or an empty car park finder will be used instead if errors occur when reading {@code storage}'s car park finder.
     */
    private Model initModelManager(Storage storage, UserPrefs userPrefs) {
        Optional<ReadOnlyCarparkFinder> carparkFinderOptional;
        ReadOnlyCarparkFinder initialData;
        try {
            carparkFinderOptional = storage.readCarparkFinder();
            if (!carparkFinderOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with an empty Car Park Finder");
                initialData = new CarparkFinder();
            } else {
                logger.info("Data file found. Loading from saved data");
                initialData = carparkFinderOptional.get();
            }
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty Car Park Finder");
            initialData = new CarparkFinder();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty Car Park Finder");
            initialData = new CarparkFinder();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                    + "Using default config properties");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                    + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty CarparkFinder");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    private void initEventsCenter() {
        EventsCenter.getInstance().registerHandler(this);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Car Park Finder " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Car Park Finder ] =============================");
        ui.stop();
        try {
            storage.saveCarparkFinder(model.getCarparkFinder());
            storage.saveUserPrefs(userPrefs);
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
        Platform.exit();
        System.exit(0);
    }

    @Subscribe
    public void handleExitAppRequestEvent(ExitAppRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
