package seedu.parking.model.carpark;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import seedu.parking.commons.core.EventsCenter;
import seedu.parking.commons.core.Messages;
import seedu.parking.commons.events.ui.NewResultAvailableEvent;
import seedu.parking.commons.util.StringUtil;
import seedu.parking.logic.parser.CarparkTypeParameter;
import seedu.parking.logic.parser.FreeParkingParameter;
import seedu.parking.logic.parser.ParkingSystemTypeParameter;

/**
 * Tests that a {@code Carpark} met all the filtering criteria.
 */
public class CarparkFilteringPredicate implements Predicate<Carpark> {

    private final List<String> locationKeywords;
    private final List<String> flagList;
    private final FreeParkingParameter freeParkingParameter;
    private final CarparkTypeParameter carparkTypeParameter;
    private final ParkingSystemTypeParameter parkingSystemTypeParameter;

    public CarparkFilteringPredicate(List<String> locationKeywords, List<String> flagList,
                                     FreeParkingParameter freeParkingParameter,
                                     CarparkTypeParameter carparkTypeParameter,
                                     ParkingSystemTypeParameter parkingSystemTypeParameter) {
        this.locationKeywords = locationKeywords;
        this.flagList = flagList;
        this.freeParkingParameter = freeParkingParameter;
        this.carparkTypeParameter = carparkTypeParameter;
        this.parkingSystemTypeParameter = parkingSystemTypeParameter;
    }

    /**
     * Checks if the car park has free parking from the given starting to ending time on the specified day.
     */
    private boolean checkFreeParking(String day, Date inputStart, Date inputEnd, String timePeriod) {
        boolean hasFreeParkingTiming = !timePeriod.equals("NO");
        boolean hasDay = false;
        boolean inputStartBeforeStart = false;
        boolean inputStartAfterEnd = false;
        boolean inputEndAfterEnd = false;
        boolean inputEndBeforeStart = false;

        try {
            if (hasFreeParkingTiming) {

                // Check if the specified day has free-parking
                hasDay = timePeriod.contains(day);

                // Parse car park free-parking info
                String[] timePeriodArray = timePeriod.split("\\s+");
                String time = timePeriodArray[timePeriodArray.length - 1];
                String[] startAndEndTime = time.split("-");

                SimpleDateFormat dateFormat1 = new SimpleDateFormat("hhaa");
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh.mmaa");

                Date start = dateFormat1.parse(startAndEndTime[0]);
                Date end = dateFormat2.parse(startAndEndTime[1]);

                // Check if the input end time is referring to the next day
                if (!inputStart.before(inputEnd)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(inputEnd);
                    cal.add(Calendar.HOUR_OF_DAY, 24);
                    inputEnd = cal.getTime();
                }

                inputStartBeforeStart = inputStart.before(start);
                inputStartAfterEnd = inputStart.after(end);
                inputEndAfterEnd = inputEnd.after(end);
                inputEndBeforeStart = inputEnd.before(start);
            }
        } catch (ParseException e) {
            EventsCenter.getInstance().post(new NewResultAvailableEvent(Messages.MESSAGE_ERROR_PARSING_CARPARK_INFO));
        }

        return hasFreeParkingTiming && hasDay
                && !((inputStartBeforeStart && inputEndBeforeStart) || (inputStartAfterEnd && inputEndAfterEnd));
    }

    /**
     * Checks if the car park is of the specified car park type.
     */
    private boolean checkCarParkType(String selectedCarparkType, String carparkType) {
        switch (selectedCarparkType) {
        case "SURFACE":
            return carparkType.contains("SURFACE");

        case "MULTISTOREY":
            return carparkType.contains("MULTI-STOREY");

        case "BASEMENT":
            return carparkType.contains("BASEMENT");

        case "MECHANISED":
            return carparkType.contains("MECHANISED");

        case "COVERED":
            return carparkType.contains("COVERED");

        default:
            return carparkType.contains("CAR PARK");
        }
    }

    /**
     * Checks if the car park has the specified parking system.
     */
    private boolean checkParkingSystemType(String selectedParkingSystemType, String parkingSystemType) {
        switch (selectedParkingSystemType) {
        case "COUPON":
            return parkingSystemType.contains("COUPON");

        case "ELECTRONIC":
            return parkingSystemType.contains("ELECTRONIC");

        default:
            return parkingSystemType.contains("PARKING");
        }
    }

    @Override
    public boolean test(Carpark carpark) {

        // Location filtering
        boolean correctLocation = locationKeywords.stream()
                .anyMatch(keyword ->
                        StringUtil.containsWordIgnoreCase(carpark.getCarparkNumber().toString(), keyword)
                                || StringUtil.containsWordIgnoreCase(carpark.getAddress().toString(), keyword)
                                || StringUtil.containsPartialWordIgnoreCase(carpark.getCarparkNumber().toString(),
                                keyword)
                                || StringUtil.containsPartialWordIgnoreCase(carpark.getAddress().toString(), keyword)
                );

        // Filtering by various flags
        boolean collective = true;

        if (flagList.contains("n/")) {
            boolean hasNightParking = carpark.getNightParking().toString().equals("YES");

            collective = hasNightParking;
        }
        if (flagList.contains("a/")) {
            boolean hasAvailableSlots = !carpark.getLotsAvailable().value.equals("0");

            collective = collective && hasAvailableSlots;
        }
        if (flagList.contains("s/")) {
            boolean hasShortTermParking = !carpark.getShortTerm().value.equals("NO");

            collective = collective && hasShortTermParking;
        }
        if (flagList.contains("f/")) {

            String timePeriod = carpark.getFreeParking().value;
            String day = freeParkingParameter.getDay();
            Date inputStart = freeParkingParameter.getStartTime();
            Date inputEnd = freeParkingParameter.getEndTime();

            boolean hasFreeParking = checkFreeParking(day, inputStart, inputEnd, timePeriod);

            collective = collective && hasFreeParking;
        }
        if (flagList.contains("ct/")) {

            String carparkType = carpark.getCarparkType().value;
            String selectedCarparkType = carparkTypeParameter.getCarparkType();

            boolean isCorrectType = checkCarParkType(selectedCarparkType, carparkType);

            collective = collective && isCorrectType;
        }
        if (flagList.contains("ps/")) {

            String parkingSystemType = carpark.getTypeOfParking().value;
            String selectedParkingSystemType = parkingSystemTypeParameter.getParkingSystemType();

            boolean isCorrectSystem = checkParkingSystemType(selectedParkingSystemType, parkingSystemType);

            collective = collective && isCorrectSystem;
        }

        return correctLocation && collective;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof CarparkFilteringPredicate)) {
            return false;
        }

        CarparkFilteringPredicate otherPredicate = (CarparkFilteringPredicate) other;

        Collections.sort(flagList);
        Collections.sort(otherPredicate.flagList);
        boolean checkFlagList = flagList.equals(otherPredicate.flagList); // flagList is always present

        Collections.sort(locationKeywords);
        Collections.sort(otherPredicate.locationKeywords);
        boolean checkLocationKeywords = locationKeywords.equals(otherPredicate.locationKeywords);

        boolean checkFreeParkingParameter = (freeParkingParameter == null
                && otherPredicate.freeParkingParameter == null)
                || (freeParkingParameter.getDay().equals(otherPredicate.freeParkingParameter.getDay())
                && freeParkingParameter.getStartTime().equals(otherPredicate.freeParkingParameter.getStartTime())
                && freeParkingParameter.getEndTime().equals(otherPredicate.freeParkingParameter.getEndTime()));

        boolean checkCarparkTypeParameter = (carparkTypeParameter == null
                && otherPredicate.carparkTypeParameter == null)
                || carparkTypeParameter.getCarparkType().equals(otherPredicate.carparkTypeParameter.getCarparkType());

        boolean checkParkingSystemTypeParameter = (parkingSystemTypeParameter == null
                && otherPredicate.parkingSystemTypeParameter == null)
                || parkingSystemTypeParameter.getParkingSystemType()
                .equals(otherPredicate.parkingSystemTypeParameter.getParkingSystemType());

        return checkFlagList && checkLocationKeywords && checkFreeParkingParameter && checkCarparkTypeParameter
                && checkParkingSystemTypeParameter;
    }
}
