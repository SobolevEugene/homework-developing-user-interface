package com.sample.airtickets.screen.ticket;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlIdSerializer;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@UiController("Ticket.info")
@UiDescriptor("ticket-info.xml")
@Route(value = "tickets/view", parentPrefix = "tickets", root = true)
public class TicketInfo extends Screen {
	@Autowired
	private InstanceContainer<Ticket> ticketDc;
	@Autowired
	private InstanceLoader<Ticket> ticketDl;
	private Ticket ticket;
	@Autowired
	private UrlRouting urlRouting;
	@Autowired
	private DataManager dataManager;

	public TicketInfo withTicket(Ticket ticket) {
		this.ticket = ticket;
		return this;
	}

	@Subscribe
	public void onAfterShow(final AfterShowEvent event) {
		String serializedId = UrlIdSerializer.serializeId(ticket.getId());
		urlRouting.replaceState(this, ImmutableMap.of("id", serializedId));
	}

	@Subscribe
	public void onUrlParamsChanged(final UrlParamsChangedEvent event) {
		String serializedId = event.getParams().get("id");
		UUID ticketId = (UUID) UrlIdSerializer.deserializeId(UUID.class, serializedId);
		ticket = dataManager.load(Ticket.class).id(ticketId).one();
	}


	@Subscribe
	public void onBeforeShow(final BeforeShowEvent event) {
		ticketDc.setItem(ticket);
		ticketDl.load();
	}

	@Subscribe("closeBtn")
	public void onCloseBtnClick(final Button.ClickEvent event) {
		close(StandardOutcome.CLOSE);
	}

}