package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.ProgressBar;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@UiController("TicketReservation")
@UiDescriptor("ticket-reservation.xml")
public class TicketReservation extends Screen {
	@Autowired
	private DateField<LocalDate> departureDateFilter;
	@Autowired
	private EntityComboBox<Airport> airportToFilter;
	@Autowired
	private TicketService ticketService;
	@Autowired
	private CollectionLoader<Flight> flightsDl;
	@Autowired
	private EntityComboBox<Airport> airportFromFilter;
	@Autowired
	private Notifications notifications;
	@Autowired
	private CollectionContainer<Flight> flightsDc;
	@Autowired
	protected BackgroundWorker backgroundWorker;
	@Autowired
	private ProgressBar searchProgress;

	@Subscribe("searchBtn")
	public void onSearchBtnClick(final Button.ClickEvent event) {
		if (airportFromFilter.isEmpty() &
				airportToFilter.isEmpty() &
				departureDateFilter.isEmpty()) {
			notifications.create()
					.withType(Notifications.NotificationType.WARNING)
					.withCaption("Please fill at least one filter field").show();
			return;
		}

		executeBackGroundTask();
	}


	private void executeBackGroundTask() {
		BackgroundTask<Integer, List<Flight>> task = new BackgroundTask<>(10) {
			@Override
			public List<Flight> run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {

				taskLifeCycle.publish(0);
				var flights = ticketService.searchFlights(airportFromFilter.getValue(),
						airportToFilter.getValue(),
						departureDateFilter.getValue());
				taskLifeCycle.publish(1);
				return flights;
			}

			@Override
			public void progress(List<Integer> changes) {
				searchProgress.setVisible(changes.get(changes.size() - 1).equals(0));
			}

			@Override
			public void done(List<Flight> flights) {
				flightsDc.setItems(flights);
				flightsDl.load();
			}

		};

		var taskHandler = backgroundWorker.handle(task);
		taskHandler.execute();


	}


}
