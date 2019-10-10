package com.vaadin.flow.portal;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GridPortletView extends Grid<Contact> {

    private ListDataProvider<Contact> dataProvider;

    public GridPortletView() {
        super(Contact.class);
        registerHub();
        dataProvider = new ListDataProvider<>(
                ContactService.getInstance().getContacts());
        setDataProvider(dataProvider);
        removeColumnByKey("id");
        setSelectionMode(SelectionMode.NONE);
        addItemClickListener(this::fireSelectionEvent);
        setColumns("firstName", "lastName", "phoneNumber", "email",
                "birthDate");
        setMinWidth("450px");
    }

    private void registerHub() {
        try {
            String portletRegistryName = VaadinPortletService
                    .getCurrentResponse()
                    .getPortletResponse().getNamespace();
            String registerPortlet = IOUtils.toString(
                    GridPortletView.class.getClassLoader()
                            .getResourceAsStream("PortletHubRegistration.js"),
                    StandardCharsets.UTF_8);
            getElement().executeJs(registerPortlet, portletRegistryName,
                    getElement());
        } catch (IOException e) {
        }
    }

    private void fireSelectionEvent(
            ItemClickEvent<Contact> contactItemClickEvent) {
        Integer id = contactItemClickEvent.getItem().getId();
        dispatchSelectionEvent(getFormPortletPortletId(),id);
    }

    private String getFormPortletPortletId() {
        // Get form layout somehow from VaadinPortletRequest.getCurrentPortletRequest().requestContext.url.portletIds
        // or any other way that we can get the actual registered name
        return "";
    }

    private void dispatchSelectionEvent(String target, Integer itemId) {
        // dispatch an event to given portlet target with the itemId and new render Mode EDIT

    }

}