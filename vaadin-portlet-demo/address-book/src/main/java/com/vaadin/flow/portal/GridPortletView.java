package com.vaadin.flow.portal;

import javax.portlet.WindowState;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.portal.util.PortletHubUtil;

public class GridPortletView extends VerticalLayout {

    public static final String SELECTION = "selection";
    private ListDataProvider<Contact> dataProvider;

    private Button windowState;

    public GridPortletView() {
        setWidthFull();
        GridPortlet portlet = GridPortlet.getCurrent();
        PortletHubUtil.registerHub();

        dataProvider = new ListDataProvider<>(
                ContactService.getInstance().getContacts());

        Grid<Contact> grid = new Grid<>(Contact.class);
        grid.setDataProvider(dataProvider);
        grid.removeColumnByKey("id");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addItemClickListener(this::fireSelectionEvent);
        grid.setColumns("firstName", "lastName", "phoneNumber", "email",
                "birthDate");
        setMinWidth("450px");

        windowState = new Button(
                WindowState.NORMAL.equals(portlet.getWindowState()) ?
                        "Maximize" :
                        "Normalize", event -> switchWindowState());

        add(windowState, grid);
        setHorizontalComponentAlignment(Alignment.END, windowState);
    }

    private void fireSelectionEvent(
            ItemClickEvent<Contact> contactItemClickEvent) {
        Integer contactId = contactItemClickEvent.getItem().getId();

        Map<String, String> param = new HashMap<>();
        param.put("selection", Integer.toString(contactId));

        GridPortlet portlet = GridPortlet.getCurrent();
        param.put("windowState", portlet.getWindowState().toString());

        // NOT Implemented see TheseInVaaadinPortlet::sendEvent
//        portlet.sendEvent(SELECTION, param);
    }

    private void switchWindowState() {
        GridPortlet portlet = GridPortlet.getCurrent();
        if (WindowState.NORMAL.equals(portlet.getWindowState())) {
            portlet.setWindowState(WindowState.MAXIMIZED);
            windowState.setText("Normalize");
        } else if (WindowState.MAXIMIZED.equals(portlet.getWindowState())) {
            portlet.setWindowState(WindowState.NORMAL);
            windowState.setText("Maximize");
        }
    }
}