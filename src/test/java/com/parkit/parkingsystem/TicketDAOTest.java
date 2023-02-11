package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;

import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAOTest {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @BeforeEach
    private void setUpPerTest() throws Exception {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        // as we are testing counting ticket and creating, we need to clear the entries in db to avoid false results
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void saveAndGetTicketTest() {
        // create a ticket, create a copy that we will send in DB
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("GHIJKL");
        ticket.setPrice(50);
        ticket.setInTime(new Date(System.currentTimeMillis() - (1000 * 60 * 60)));

        // save this ticket in db
        ticketDAO.saveTicket(ticket);

        // retrieve the ticket and comparing assertions
        Ticket ticketSendInDB = ticketDAO.getTicket("GHIJKL");
        assertNotNull(ticketSendInDB);
        assertEquals(ticket.getParkingSpot(), ticketSendInDB.getParkingSpot());
        assertEquals(ticket.getVehicleRegNumber(), ticketSendInDB.getVehicleRegNumber());

    }

    @Test
    public void countTicketZeroOccurrencesTest() {
        // Remember that the DB is cleared before each test, so we just need to enter a random vehicle_reg_number
        String vehicleRegNumber = "QSDFGH";
        int countedInDB = ticketDAO.countTicket(vehicleRegNumber);

        assertEquals(0, countedInDB);
    }

    @Test
    public void countTicketWithOccurrencesTest(){
        // kind of same test of the last one, but we need to create a ticket and save it in db in the test
        String vehicleRegNumber = "BCDEFG";
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(0);

        ticket.setInTime(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 3)));

        ticketDAO.saveTicket(ticket);

        ticket.setInTime(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 2)));

        ticketDAO.saveTicket(ticket);

        ticket.setInTime(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 1)));

        ticketDAO.saveTicket(ticket);

        // basically : 3 times saved, so 3 times count
        int occurrencesShould = 3;
        // the db occurrences
        int occurrenceWould = ticketDAO.countTicket(vehicleRegNumber);

        assertEquals(occurrencesShould, occurrenceWould);
    }
}