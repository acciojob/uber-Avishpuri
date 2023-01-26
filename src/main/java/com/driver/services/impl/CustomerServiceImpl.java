package com.driver.services.impl;


import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.*;


import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.model.TripStatus;




@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception {
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		Collections.sort(driverList, Comparator.comparingInt(Driver::getDriverId));
		Driver driverAvailable = null;
		for (Driver driver : driverList) {
			if (driver.getCab().isAvailable()) {
				driverAvailable = driver;
				break;
			}
		}
		if (driverAvailable == null) {
			throw new Exception("No cab available!");
		}
		TripBooking tripBooking = new TripBooking();
		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driverAvailable);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		int bill = driverAvailable.getCab().getPerKmRate() * distanceInKm;
		tripBooking.setBill(bill);
		tripBooking.setTripStatus(TripStatus.CONFIRMED);
		customer.getTripBookingList().add(tripBooking);
		driverAvailable.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);
		driverRepository2.save(driverAvailable);
		//tripBookingRepository2.save(tripBooking);
		return tripBooking;

	}



	@Override
	public void cancelTrip(Integer tripId){

		TripBooking tripBooking =tripBookingRepository2.findById(tripId).get();
		   tripBooking.setBill(0);
		   tripBooking.setDistanceInKm(0);
		   tripBooking.setTripStatus(TripStatus.CANCELED);
		   tripBooking.setToLocation(null);
		   tripBooking.setFromLocation(null);
		   tripBooking.getDriver().getCab().setAvailable(true);
		   tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
      TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
	  tripBooking.setTripStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);

	}
}
