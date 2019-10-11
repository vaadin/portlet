package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class GridPortletView extends VerticalLayout implements
        WindowStateHandler {

    private ListDataProvider<Contact> dataProvider;

    private Button windowState;

    public GridPortletView() {
        setWidthFull();
        GridPortlet portlet = GridPortlet.getCurrent();
        portlet.registerHub(getElement());

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
        GridPortlet portlet = GridPortlet.getCurrent();
        portlet
                .sendContactSelectionEvent(contactItemClickEvent.getItem(),
                        getElement());
        // Normalize the maximized window
        if(WindowState.MAXIMIZED.equals(portlet.getWindowState())) {
            switchWindowState();
        }
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

    @Override
    public void windowStateChange(WindowStateEvent event) {

    }
}