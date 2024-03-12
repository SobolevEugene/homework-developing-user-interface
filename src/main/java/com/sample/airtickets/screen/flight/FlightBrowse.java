package com.sample.airtickets.screen.flight;

import com.sample.airtickets.entity.Airport;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Flight;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.OffsetDateTime;

@UiController("Flight.browse")
@UiDescriptor("flight-browse.xml")
@LookupComponent("flightsTable")
public class FlightBrowse extends StandardLookup<Flight> {
	@Autowired
	private CollectionLoader<Flight> flightsDl;

	@Subscribe("airportFilter")
	public void onAirportFilterValueChange(final HasValue.ValueChangeEvent<Airport> event) {
		if (event.getValue() == null) {
			flightsDl.removeParameter("airportId");
		} else {
			flightsDl.setParameter("airportId", event.getValue().getId());
		}
		flightsDl.load();
	}

	@Subscribe("takeOffFromFilter")
	public void onTakeOffFromFilterValueChange(final HasValue.ValueChangeEvent<?> event) {
		if (event.getValue() == null) {
			flightsDl.removeParameter("takeOffFrom");
		} else {
			flightsDl.setParameter("takeOffFrom", parseDate(event.getValue().toString()));
		}
		flightsDl.load();
	}

	@Subscribe("takeOffToFilter")
	public void onTakeOffToFilterValueChange(final HasValue.ValueChangeEvent<?> event) {
		if (event.getValue() == null) {
			flightsDl.removeParameter("takeOffTo");
		} else {
			flightsDl.setParameter("takeOffTo", parseDate(event.getValue().toString()));
		}
		flightsDl.load();
	}

	private OffsetDateTime parseDate(String dateString) {
		return OffsetDateTime.parse(dateString);
	}
}