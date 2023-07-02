package se.sodalabs.hub.views.dashboard;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import se.sodalabs.hub.domain.Participant;
import se.sodalabs.hub.repository.ConnectedParticipantsRepository;

@PageTitle("dashboard")
@Route(value = "/")
@PreserveOnRefresh
public class DashboardView extends Div implements AfterNavigationObserver {

  ConnectedParticipantsRepository connectedParticipantsRepository;
  ParticipantListDataProvider participantListDataProvider;
  final Grid<Participant> grid = new Grid<>();

  public DashboardView(
      ConnectedParticipantsRepository connectedParticipantsRepository,
      ParticipantListDataProvider participantListDataProvider) {
    this.connectedParticipantsRepository = connectedParticipantsRepository;
    this.participantListDataProvider = participantListDataProvider;

    addClassName("dashboard-view");
    setSizeFull();
    grid.setHeight("100%");
    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
    grid.addComponentColumn(this::createCard);

    grid.setDataProvider(participantListDataProvider);
    participantListDataProvider.addDataProviderListener(
        (DataProviderListener)
            event -> {
              UI ui;
              if (grid.getUI().isPresent()) {
                ui = grid.getUI().get();
                ui.access(
                    () -> {
                      grid.setItems(connectedParticipantsRepository.findAll());
                    });
              } else {
                throw new UIDetachedException("No UI found for DashboardView Component");
              }
            });

    add(grid);
  }

  private HorizontalLayout createCard(Participant participant) {
    HorizontalLayout participantCard = new HorizontalLayout();
    participantCard.addClassName("card");
    participantCard.setSpacing(true);
    participantCard.getThemeList().add("spacing-m");

    // left column
    VerticalLayout avatarColumn = new VerticalLayout();
    avatarColumn.setAlignItems(Alignment.CENTER);
    avatarColumn.setWidth("38%");

    Span name = new Span(participant.getName());
    name.addClassName("name");

    String participantAvatarImg = participant.getAvatarImg();
    StreamResource avatarImageResource =
        new StreamResource(
            participantAvatarImg,
            () -> getClass().getResourceAsStream("/img/avatars/" + participantAvatarImg));
    Image avatarImage = new Image(avatarImageResource, participant.getName());
    avatarImage.setHeight("160px");
    avatarImage.setWidth("160px");

    Span id = new Span(participant.getId());
    id.addClassName("id");

    avatarColumn.add(name, avatarImage, id);
    // end of left column

    // right column
    VerticalLayout participantInfoPanel = new VerticalLayout();
    participantInfoPanel.setJustifyContentMode(JustifyContentMode.BETWEEN);
    participantInfoPanel.setSpacing(false);
    participantInfoPanel.setPadding(false);

    VerticalLayout availabilityPanel = new VerticalLayout();
    Span availabilityHeading = new Span("Availability");
    availabilityHeading.addClassName("heading");
    Span availability = new Span("Today: " + participant.getAvailability() + "!");
    availability.addClassName("availability");
    availabilityPanel.add(availabilityHeading, new Hr(), availability);

    VerticalLayout lastUpdatePanel = new VerticalLayout();

    String okOrNok = participant.getLastHttpResponse() > 299 ? "NOT OK" : "ok";
    Span lastResponseInformation =
        new Span(
            "Last message received "
                + okOrNok
                + " ("
                + participant.getLastHttpResponse()
                + ") at "
                + participant.getLastUpdatedAt());
    lastResponseInformation.addClassName("lastResponse");
    lastUpdatePanel.add(lastResponseInformation);

    participantInfoPanel.add(availabilityPanel, lastUpdatePanel);
    // end of right column

    participantCard.add(avatarColumn, participantInfoPanel);

    if (participant.getLastHttpResponse() > 299) {
      participantCard.addClassName("failure");
    }

    return participantCard;
  }

  private HorizontalLayout createAttribution() {
    HorizontalLayout footer = new HorizontalLayout();
    Span imgAttribution =
        new Span(
            "<a href=\"https://www.freepik.com/free-vector/animal-hipster-set_3975549.htm#query=animal%20avatars&position=11&from_view=keyword&track=ais\">Image by macrovector</a> on Freepik");
    footer.add(imgAttribution);
    return footer;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    grid.setItems(connectedParticipantsRepository.findAll());
  }
}
