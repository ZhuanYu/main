package seedu.parking.commons.core;

/**
 * Container for user visible messages.
 */
public class Messages {

    // CarparkFinderParser
    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! \n%1$s";
    public static final String MESSAGE_UNCERTAIN_FIND_OR_FILTER_COMMAND =
        "Ambiguous command detected. You can type 'fin' (find [KEYWORD]) or 'fil' (filter [FLAG/ PARAMETER])";
    public static final String MESSAGE_UNCERTAIN_HELP_OR_HISTORY_COMMAND =
        "Ambiguous command detected. You can type 'he' (help) or 'hi'(history)";
    public static final String MESSAGE_UNCERTAIN_CLEAR_OR_CALCULATE_COMMAND =
        "Ambiguous command. You can try 'cl' (clear) or 'ca' (calculate)";

    public static final String MESSAGE_INVALID_CARPARK_DISPLAYED_INDEX = "The selected index is out of range";
    public static final String MESSAGE_CARPARKS_LISTED_OVERVIEW = "%1$d car park(s) found!";

    // filter command
    public static final String MESSAGE_FINDCOMMAND_NEEDS_TO_BE_EXECUTED_FIRST = "Please use the find command to find a "
            + "list of car parks within your preferred location first.";
    public static final String MESSAGE_FREEPARKING_HAS_INVALID_PARAMETERS = "Filtering by free parking needs to have "
            + "the following parameters: f/ [day] [start time] [end time]\n     Example: filter f/ sun 7.30am 8.30pm";
    public static final String MESSAGE_START_OR_END_TIME_HAS_INCORRECT_FORMAT = "Please input a valid start and end "
            + "time.\n     Example: 7.30am or 11.30pm";
    public static final String MESSAGE_DAY_IS_INVALID = "Please input a valid day.\n     Example: mon";
    public static final String MESSAGE_CARPARK_TYPE_IS_INVALID = "Please input a valid car park type: SURFACE, BASEMENT"
            + ", MULTISTOREY, MECHANISED, COVERED\n     Example: filter ct/ surface";
    public static final String MESSAGE_PARKINGSYSTEM_TYPE_IS_INVALID = "Please input a valid parking system type: "
            + "COUPON, ELECTRONIC\n     Example: filter ps/ coupon";

    // calculate command
    public static final String MESSAGE_INVALID_CARPARK_NAME = "The car park number input is invalid.";
    public static final String MESSAGE_ERROR_PARSING_CARPARK_INFO = "Error occurred while parsing car park information";
    public static final String MESSAGE_NO_SHORT_TERM_PARKING = "This car park has no short term parking.";
    public static final String MESSAGE_INVALID_START_OR_END_TIME = "Start and end of parking time must be between the "
            + "stipulated car park operation time.";
    public static final String MESSAGE_COST_OF_PARKING = "Cost of parking is $%1$.2f.";

    // auto-complete
    public static final String MESSAGE_INVALID_COMMAND_FOR_AUTOCOMPLETE =
        "Auto completion failed. Invalid command for auto-complete feature.";
    public static final String MESSAGE_ALREADY_FULL_COMMAND_FORMAT =
        "Your command is already in full command format.";
    public static final String MESSAGE_AUTO_COMPLETE_SUCCESS = "Your command is auto-completed.";
    public static final String MESSAGE_SELECT_NEXT_HOLDER_SUCCESS =
        "Next parameter holder is selected for you to replace.";
}
