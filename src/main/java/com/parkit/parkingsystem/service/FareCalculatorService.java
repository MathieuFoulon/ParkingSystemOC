package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        // starting with milliseconds, then seconds, minutes, hours ( a bit long but more understandable )
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();
        long difOutAndInSec = ((outHour - inHour )/ 1000 );
        float difOutAndInMin = (float)difOutAndInSec / 60;

        //TODO: Some tests are failing here. Need to check if this logic is correct

        // duration is now in hour and is a float, that's why tests failed before ( couldn't proceed 0.5 hours )

        // For free ticket, we can simply say that if vehicle stay less than 30 minutes, it's like it is 0 minute: so 0 * fare = 0
        float duration = difOutAndInMin / 60 < 0.5 ? 0 : difOutAndInMin /60;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}