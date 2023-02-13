package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect: "+ticket.getOutTime().toString());
        }

        // retrieve the number of occurrences of the vehicle registration number in DB
        TicketDAO ticketDAO = new TicketDAO();
        int numberOfParkingAccess = ticketDAO.countTicket(ticket.getVehicleRegNumber());

        // starting with milliseconds, then seconds, minutes, hours ( a bit long but more understandable )
        long inTimestamp = ticket.getInTime().getTime();
        long outTimestamp = ticket.getOutTime().getTime();
        long difOutAndInSec = ((outTimestamp - inTimestamp )/ 1000 );
        float difOutAndInMin = (float)difOutAndInSec / 60;

        //TODO: Some tests are failing here. Need to check if this logic is correct

        // duration is now in hour and is a float, that's why tests failed before ( couldn't proceed 0.5 hours )

        // For free ticket, we can simply say that if vehicle stay less than 30 minutes, it's like it is 0 minute: so 0 * fare = 0
        float duration = difOutAndInMin / 60 < 0.5 ? 0 : difOutAndInMin /60;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (numberOfParkingAccess >= Fare.ACCESS_TO_DISCOUNT_5) {
                    ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) * Fare.DISCOUNT_5);
                } else {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                break;
            }
            case BIKE: {
                if (numberOfParkingAccess >= Fare.ACCESS_TO_DISCOUNT_5) {
                    ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) * Fare.DISCOUNT_5);
                } else {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}