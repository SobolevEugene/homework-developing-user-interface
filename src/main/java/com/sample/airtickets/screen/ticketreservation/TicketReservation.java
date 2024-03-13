package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import com.sample.airtickets.entity.Ticket;
import com.sample.airtickets.screen.ticket.TicketInfo;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@UiController("TicketReservation")
@UiDescriptor("ticket-reservation.xml")
public class TicketReservation extends Screen {

	private Flight selectedFlight;
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
	@Autowired
	private UiComponents uiComponents;
	@Autowired
	private Action inputDialogAction;
	@Autowired
	private DataManager dataManager;
	@Autowired
	private ScreenBuilders screenBuilders;

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

	@Install(to = "flightsTable.actionsColumn", subject = "columnGenerator")
	private Component flightsTableActionsColumnColumnGenerator(final Flight flight) {
		var reserveButton = uiComponents.create(LinkButton.class);
		reserveButton.setCaption("Reserve");
		reserveButton.setAction(inputDialogAction);
		return reserveButton;
	}

	@Install(to = "reserveTicketInputDialog", subject = "dialogResultHandler")
	private void inputDialogDialogResultHandler(InputDialog.InputDialogResult inputDialogResult) {
		if (inputDialogResult.getCloseActionType().equals(InputDialog.InputDialogResult.ActionType.OK)) {
			String passengerName = inputDialogResult.getValue("nameParam");
			String passport = inputDialogResult.getValue("passportNumberParam");
			String telephone = inputDialogResult.getValue("telephoneParam");

			Ticket newTicket = dataManager.create(Ticket.class);
			newTicket.setFlight(selectedFlight);
			newTicket.setPassengerName(passengerName);
			newTicket.setPassportNumber(passport);
			newTicket.setTelephone(telephone);
			Ticket reservedTicket = ticketService.saveTicket(newTicket);
			screenBuilders.screen(this).withScreenClass(TicketInfo.class)
					.withOpenMode(OpenMode.NEW_TAB).build()
					.withTicket(reservedTicket).show();
		}
	}

	@Subscribe(id = "flightsDc", target = Target.DATA_CONTAINER)
	public void onFlightsDcItemChange(final InstanceContainer.ItemChangeEvent<Flight> event) {
		selectedFlight = event.getItem();
	}


}
