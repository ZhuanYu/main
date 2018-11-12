package seedu.parking.ui;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static seedu.parking.commons.core.Messages.MESSAGE_ALREADY_FULL_COMMAND_FORMAT;
import static seedu.parking.commons.core.Messages.MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE;
import static seedu.parking.commons.core.Messages.MESSAGE_UNCERTAIN_CLEAR_OR_CALCULATE_COMMAND;
import static seedu.parking.commons.core.Messages.MESSAGE_UNCERTAIN_FIND_OR_FILTER_COMMAND;
import static seedu.parking.logic.commands.CalculateCommand.FIRST_ARG;
import static seedu.parking.logic.commands.FilterCommand.CARPARKTYPE_ARG;
import static seedu.parking.logic.commands.FilterCommand.FREEPARKING_FIRST_ARG;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import guitests.guihandles.CommandBoxHandle;
import guitests.guihandles.ResultDisplayHandle;
import javafx.scene.input.KeyCode;
import seedu.parking.logic.Logic;
import seedu.parking.logic.LogicManager;
import seedu.parking.logic.commands.CalculateCommand;
import seedu.parking.logic.commands.FilterCommand;
import seedu.parking.logic.commands.FindCommand;
import seedu.parking.logic.commands.HelpCommand;
import seedu.parking.logic.commands.ListCommand;
import seedu.parking.logic.commands.NotifyCommand;
import seedu.parking.logic.commands.SelectCommand;
import seedu.parking.logic.parser.exceptions.ParseException;
import seedu.parking.model.Model;
import seedu.parking.model.ModelManager;



public class CommandBoxTest extends GuiUnitTest {

    private static final String COMMAND_THAT_SUCCEEDS = ListCommand.COMMAND_WORD;
    private static final String COMMAND_THAT_FAILS = "invalid command";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ArrayList<String> defaultStyleOfCommandBox;
    private ArrayList<String> errorStyleOfCommandBox;

    private CommandBoxHandle commandBoxHandle;
    private ResultDisplayHandle resultDisplayHandle;
    private CommandBox commandBox;


    @Before
    public void setUp() {
        Model model = new ModelManager();
        Logic logic = new LogicManager(model);

        ResultDisplay resultDisplay = new ResultDisplay();
        uiPartRule.setUiPart(resultDisplay);

        resultDisplayHandle = new ResultDisplayHandle(getChildNode(resultDisplay.getRoot(),
            ResultDisplayHandle.RESULT_DISPLAY_ID));

        commandBox = new CommandBox(logic);
        commandBoxHandle = new CommandBoxHandle(getChildNode(commandBox.getRoot(),
                CommandBoxHandle.COMMAND_INPUT_FIELD_ID));
        uiPartRule.setUiPart(commandBox);

        defaultStyleOfCommandBox = new ArrayList<>(commandBoxHandle.getStyleClass());

        errorStyleOfCommandBox = new ArrayList<>(defaultStyleOfCommandBox);
        errorStyleOfCommandBox.add(CommandBox.ERROR_STYLE_CLASS);
    }

    @Test
    public void commandBox_startingWithSuccessfulCommand() {
        assertBehaviorForSuccessfulCommand();
        assertBehaviorForFailedCommand();
    }

    @Test
    public void commandBox_startingWithFailedCommand() {
        assertBehaviorForFailedCommand();
        assertBehaviorForSuccessfulCommand();

        // verify that style is changed correctly even after multiple consecutive failed commands
        assertBehaviorForSuccessfulCommand();
        assertBehaviorForFailedCommand();
        assertBehaviorForFailedCommand();
    }

    @Test
    public void commandBox_handleKeyPress() {
        commandBoxHandle.run(COMMAND_THAT_FAILS);
        assertEquals(errorStyleOfCommandBox, commandBoxHandle.getStyleClass());
        guiRobot.push(KeyCode.ESCAPE);
        assertEquals(errorStyleOfCommandBox, commandBoxHandle.getStyleClass());

        guiRobot.push(KeyCode.A);
        assertEquals(defaultStyleOfCommandBox, commandBoxHandle.getStyleClass());
    }

    @Test
    public void handleKeyPress_startingWithUp() {
        // empty history
        assertInputHistory(KeyCode.UP, "");
        assertInputHistory(KeyCode.DOWN, "");

        // one command
        commandBoxHandle.run(COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.UP, COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.DOWN, "");

        // two commands (latest command is failure)
        commandBoxHandle.run(COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.UP, COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.UP, COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.DOWN, COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.UP, COMMAND_THAT_FAILS);

        // insert command in the middle of retrieving previous commands
        guiRobot.push(KeyCode.UP);
        String thirdCommand = "list";
        commandBoxHandle.run(thirdCommand);
        assertInputHistory(KeyCode.UP, thirdCommand);
        assertInputHistory(KeyCode.UP, COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.UP, COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.DOWN, COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.DOWN, thirdCommand);
        assertInputHistory(KeyCode.DOWN, "");
    }

    @Test
    public void handleKeyPress_startingWithDown() {
        // empty history
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.UP, "");

        // one command
        commandBoxHandle.run(COMMAND_THAT_SUCCEEDS);
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.UP, COMMAND_THAT_SUCCEEDS);

        // two commands
        commandBoxHandle.run(COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.UP, COMMAND_THAT_FAILS);

        // insert command in the middle of retrieving previous commands
        guiRobot.push(KeyCode.UP);
        String thirdCommand = "list";
        commandBoxHandle.run(thirdCommand);
        assertInputHistory(KeyCode.DOWN, "");
        assertInputHistory(KeyCode.UP, thirdCommand);
    }

    @Test
    public void handleKeyPress_startingWithTab() throws Exception {
        // valid commands that have format auto-completed
        assertAutoComplete_valid_commands(SelectCommand.COMMAND_WORD, SelectCommand.FORMAT);
        assertAutoComplete_valid_commands(FindCommand.COMMAND_WORD, FindCommand.FORMAT);
        assertAutoComplete_valid_commands(NotifyCommand.COMMAND_WORD, NotifyCommand.FORMAT);
        assertAutoComplete_valid_commands(FilterCommand.COMMAND_WORD, FilterCommand.FORMAT);
        assertAutoComplete_valid_commands(CalculateCommand.COMMAND_WORD, CalculateCommand.FORMAT);

        // valid commands already in complete format
        //assertAutoComplete_already_complete(SelectCommand.COMMAND_WORD + " 1");
        //assertAutoComplete_already_complete(FindCommand.COMMAND_WORD + " hougang");
        //assertAutoComplete_already_complete(NotifyCommand.COMMAND_WORD + " 60");

        // valid commands that can select next parameter automatically
        assertAutoCompleteFilter_substitute_first();
        assertAutoCompleteFilter_substitute_fourth();
        assertAutoCompleteCalculate_substitute_first();
    }

    @Test
    public void autoComplete_ambiguous_commandC() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(String.format("%s\n%s",
            MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE, MESSAGE_UNCERTAIN_CLEAR_OR_CALCULATE_COMMAND));
        commandBoxHandle.load("c");
        commandBox.autoComplete();
    }

    @Test
    public void autoComplete_ambiguous_commandF() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(String.format("%s\n%s",
            MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE, MESSAGE_UNCERTAIN_FIND_OR_FILTER_COMMAND));
        commandBoxHandle.load("f");
        commandBox.autoComplete();
    }

    @Test
    public void autoComplete_ambiguous_commandFi() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(String.format("%s\n%s",
            MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE, MESSAGE_UNCERTAIN_FIND_OR_FILTER_COMMAND));
        commandBoxHandle.load("fi");
        commandBox.autoComplete();
    }


    @Test
    public void autoComplete_invalid_commandNull() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE);
        commandBoxHandle.load("");
        commandBox.autoComplete();
    }

    @Test
    public void autoComplete_invalid_commandSpace() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE);
        commandBoxHandle.load(" ");
        commandBox.autoComplete();
    }

    @Test
    public void autoComplete_invalid_commandHe() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE);
        commandBoxHandle.load(HelpCommand.COMMAND_WORD.substring(0, 2));
        commandBox.autoComplete();
    }

    @Test
    public void autoComplete_invalid_commandList() throws Exception {
        thrown.expect(ParseException.class);
        thrown.expectMessage(MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE);
        commandBoxHandle.load(ListCommand.COMMAND_WORD);
        commandBox.autoComplete();
    }

    /**
     * Runs a command that fails, then verifies that <br>
     *      - the text remains <br>
     *      - the command box's style is the same as {@code errorStyleOfCommandBox}.
     */
    private void assertBehaviorForFailedCommand() {
        commandBoxHandle.run(COMMAND_THAT_FAILS);
        assertEquals(COMMAND_THAT_FAILS, commandBoxHandle.getInput());
        assertEquals(errorStyleOfCommandBox, commandBoxHandle.getStyleClass());
    }

    /**
     * Runs a command that succeeds, then verifies that <br>
     *      - the text is cleared <br>
     *      - the command box's style is the same as {@code defaultStyleOfCommandBox}.
     */
    private void assertBehaviorForSuccessfulCommand() {
        commandBoxHandle.run(COMMAND_THAT_SUCCEEDS);
        assertEquals("", commandBoxHandle.getInput());
        assertEquals(defaultStyleOfCommandBox, commandBoxHandle.getStyleClass());
    }

    /**
     * Pushes {@code keycode} and checks that the input in the {@code commandBox} equals to {@code expectedCommand}.
     */
    private void assertInputHistory(KeyCode keycode, String expectedCommand) {
        guiRobot.push(keycode);
        assertEquals(expectedCommand, commandBoxHandle.getInput());
    }

    /**
     * Checks if a {@code givenCommand} in the {@code commandBox} matches the {@code expectedCommand}.
     */
    private void assertAutoComplete_valid_commands(String givenCommand, String expectedCommand) throws Exception {
        commandBoxHandle.load(givenCommand);
        commandBox.autoComplete();
        assertEquals(commandBoxHandle.getInput(), expectedCommand);
    }

    /**
     * Checks if a {@code givenCommand} in the {@code commandBox} is already a completed command.
     */
    private void assertAutoComplete_already_complete(String givenCommand) throws Exception {
        commandBoxHandle.load(givenCommand);
        commandBox.autoComplete();
        assertEquals(resultDisplayHandle.getText(), MESSAGE_ALREADY_FULL_COMMAND_FORMAT);
    }

    /**
     * Checks if the first parameter holder of the filter command in the {@code commandBox}
     * is substituted with the given argument and it is at the correct position
     */
    private void assertAutoCompleteFilter_substitute_first() throws Exception {
        commandBoxHandle.load(FilterCommand.COMMAND_WORD);
        commandBox.autoComplete();
        guiRobot.push(KeyCode.S);
        guiRobot.push(KeyCode.U);
        guiRobot.push(KeyCode.N);
        commandBox.autoComplete();
        assertTrue(commandBoxHandle.getInput().contains("sun"));
        assertEquals(commandBoxHandle.getInput().indexOf("sun"),
            FilterCommand.FORMAT.indexOf(FREEPARKING_FIRST_ARG));
    }

    /**
     * Checks if the fourth parameter holder of the filter command in the {@code commandBox}
     * is substituted with the given argument and it is at the correct position
     */
    private void assertAutoCompleteFilter_substitute_fourth() throws Exception {
        commandBoxHandle.load(FilterCommand.COMMAND_WORD);
        commandBox.autoComplete();
        guiRobot.push(KeyCode.TAB);
        guiRobot.push(KeyCode.TAB);
        guiRobot.push(KeyCode.TAB);
        guiRobot.push(KeyCode.TAB);
        guiRobot.push(KeyCode.B);
        guiRobot.push(KeyCode.A);
        guiRobot.push(KeyCode.S);
        guiRobot.push(KeyCode.E);
        guiRobot.push(KeyCode.M);
        guiRobot.push(KeyCode.E);
        guiRobot.push(KeyCode.N);
        guiRobot.push(KeyCode.T);
        commandBox.autoComplete();
        assertTrue(commandBoxHandle.getInput().contains("basement"));
        assertEquals(commandBoxHandle.getInput().indexOf("basement"),
            FilterCommand.FORMAT.indexOf(CARPARKTYPE_ARG));
    }

    /**
     * Checks if the first parameter holder of the calculate command in the {@code commandBox}
     * is substituted with the given argument and it is at the correct position
     */
    private void assertAutoCompleteCalculate_substitute_first() throws Exception {
        commandBoxHandle.load(CalculateCommand.COMMAND_WORD);
        commandBox.autoComplete();
        guiRobot.push(KeyCode.T);
        guiRobot.push(KeyCode.J);
        guiRobot.push(KeyCode.DIGIT3);
        guiRobot.push(KeyCode.DIGIT9);
        commandBox.autoComplete();
        assertTrue(commandBoxHandle.getInput().contains("tj39"));
        assertEquals(commandBoxHandle.getInput().indexOf("tj39"),
            CalculateCommand.FORMAT.indexOf(FIRST_ARG));
    }
}
