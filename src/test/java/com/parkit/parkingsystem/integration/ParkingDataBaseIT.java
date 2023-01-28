package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();

    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() throws SQLException, ClassNotFoundException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

        try {
            Connection con = dataBaseTestConfig.getConnection();
            String query = "SELECT * FROM ticket INNER JOIN parking ON ticket.PARKING_NUMBER = parking.PARKING_NUMBER WHERE ticket.VEHICLE_REG_NUMBER = \"ABCDEF\"";
            // create the java statement
            Statement st = con.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("ID");
                int parkingNumber = rs.getInt("PARKING_NUMBER");
                String vehicleRegNumber = rs.getString("VEHICLE_REG_NUMBER");
                boolean available = rs.getBoolean("AVAILABLE");


                System.out.format("\nTEST PARKING A CAR \n id : %s, parking number : %s, : vehicle reg number : %s, available : %s \n\n", id, parkingNumber, vehicleRegNumber, available);

                // vehicle in parking spot : available false
                assertFalse(available);
            }
            st.close();
            dataBaseTestConfig.closeConnection(con);
        } catch (Exception e) {
            System.err.println("Exception : " + e.getMessage());
        }


    }

    @Test
    public void testParkingLotExit() throws SQLException, ClassNotFoundException, InterruptedException {
        testParkingACar();
        // out 1 sec after entering, can be modified but beware of the assertEquals on fare if stayed more than 0.5 hours
        Thread.sleep(1000);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        try {
            Connection con = dataBaseTestConfig.getConnection();
            String query = "SELECT * FROM ticket INNER JOIN parking ON ticket.PARKING_NUMBER = parking.PARKING_NUMBER WHERE ticket.VEHICLE_REG_NUMBER = \"ABCDEF\"";
            // create the java statement
            Statement st = con.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("ID");
                int parkingNumber = rs.getInt("PARKING_NUMBER");
                String vehicleRegNumber = rs.getString("VEHICLE_REG_NUMBER");
                boolean available = rs.getBoolean("AVAILABLE");
                Date inDate = rs.getDate("IN_TIME");
                Time inTime = rs.getTime("IN_TIME");
                Date outDate = rs.getDate("OUT_TIME");
                Time outTime = rs.getTime("OUT_TIME");
                int fare = rs.getInt("PRICE");


                System.out.format("\nTEST PARKING LOT EXIT : \n id : %s, parking number : %s, vehicle reg number : %s, available : %s, in time: %s %s out time : %s %s, fare : %s \n\n", id, parkingNumber, vehicleRegNumber, available, inDate, inTime, outDate, outTime, fare);
                // vehicle is out : available true
                assertTrue(available);
                // stayed less than 0.5 hours : fare = 0
                assertEquals(0, fare);
            }
            st.close();
            dataBaseTestConfig.closeConnection(con);
        } catch (Exception e) {
            System.err.println("Exception : " + e.getMessage());
        }
    }

}
