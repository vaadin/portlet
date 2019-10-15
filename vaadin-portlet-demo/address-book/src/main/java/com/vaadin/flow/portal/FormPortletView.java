package com.vaadin.flow.portal;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class FormPortletView extends VerticalLayout
        implements WindowStateHandler, PortletModeHandler, EventHandler {

    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_SAVE = "Save";
    public static final String WINDOW_MAXIMIZE = "Maximize";
    public static final String WINDOW_NORMALIZE = "Normalize";
    private Binder<Contact> binder;
    private Button action;
    private Button cancel;
    private Button windowState;
    private TextField firstName;
    private Image image;

    public FormPortletView() {
        FormPortlet portlet = FormPortlet.getCurrent();

        Map<String, String> events = new HashMap<>();
        events.put(GridPortletView.SELECTION, getItemSelectFunction());
        portlet.registerHub();

        PortletMode portletMode = portlet.getPortletMode();
        portlet.setPortletView(this);

        FormLayout formLayout = populateFormLayout(portletMode);
        setupButtons(portlet);

        HorizontalLayout actionButtons = new HorizontalLayout(action, cancel);
        add(windowState, formLayout, actionButtons);
        setHorizontalComponentAlignment(Alignment.END, windowState,
                actionButtons);
    }

    private String getItemSelectFunction() {
        StringBuilder selectAction = new StringBuilder();

        selectAction.append("const poller = () => {");
        selectAction.append(" if(hub.isInProgress()) {");
        selectAction.append("  setTimeout(poller, 10);");
        selectAction.append(" } else {");
        selectAction.append("  hub.action(state);");
        selectAction.append(" }");
        selectAction.append("};");
        selectAction.append("poller();");

        return selectAction.toString();
    }

    private FormLayout populateFormLayout(PortletMode portletMode) {
        FormLayout formLayout = new FormLayout();
        firstName = new TextField();
        formLayout.addFormItem(firstName, "First name");

        TextField lastName = new TextField();
        formLayout.addFormItem(lastName, "Last name");

        TextField phone = new TextField();
        formLayout.addFormItem(phone, "Phone number");

        EmailField email = new EmailField();
        formLayout.addFormItem(email, "Email");

        binder = new Binder<>(Contact.class);
        binder.bind(firstName, "firstName");
        binder.bind(lastName, "lastName");
        binder.bind(email, "email");
        binder.bind(phone, "phoneNumber");
        // Set the state of form depending on portlet mode.
        binder.setReadOnly(PortletMode.VIEW.equals(portletMode));

        image = new Image();
        formLayout.add(image);
        return formLayout;
    }

    private void setupButtons(FormPortlet portlet) {
        action = new Button(PortletMode.EDIT
                .equals(FormPortlet.getCurrent().getPortletMode()) ?
                ACTION_SAVE :
                ACTION_EDIT, event -> {
            if (PortletMode.EDIT.equals(portlet.getPortletMode())) {
                save();
            } else {
                portlet.setPortletMode(PortletMode.EDIT);
            }
        });

        cancel = new Button("Cancel", event -> cancel());

        windowState = new Button(
                WindowState.NORMAL.equals(portlet.getWindowState()) ?
                        WINDOW_MAXIMIZE :
                        WINDOW_NORMALIZE, event -> switchWindowState());
    }

    private void switchWindowState() {
        FormPortlet portlet = FormPortlet.getCurrent();
        if (WindowState.NORMAL.equals(portlet.getWindowState())) {
            portlet.setWindowState(WindowState.MAXIMIZED);
            windowState.setText(WINDOW_NORMALIZE);
        } else if (WindowState.MAXIMIZED.equals(portlet.getWindowState())) {
            portlet.setWindowState(WindowState.NORMAL);
            windowState.setText(WINDOW_MAXIMIZE);
        }
    }

    public void handleEvent(PortletEvent event) {
        Integer contactId = Integer
                .parseInt(event.getParameters().get("contactId")[0]);
        Optional<Contact> contact = ContactService.getInstance()
                .findById(contactId);
        if (contact.isPresent()) {
            binder.setBean(contact.get());

            firstName.setValue(contact.get().getFirstName());
            image.setSrc(contact.get().getImage().toString());
        } else {
            cancel();
        }
    }

    private void cancel() {
        if (binder.getBean() != null) {
            binder.setBean(null);
            FormPortlet.getCurrent().setPortletMode(PortletMode.VIEW);
            action.setText(ACTION_EDIT);
        }
    }

    private void save() {
        Contact contact = binder.getBean();

        if (contact != null) {
            ContactService.getInstance().save(contact);
        }

        FormPortlet.getCurrent().setPortletMode(PortletMode.VIEW);
    }

    @Override
    public void portletModeChange(PortletModeEvent event) {
        binder.setReadOnly(PortletMode.VIEW.equals(event.getPortletMode()));
        if (event.isEditMode()) {
            action.setText(ACTION_SAVE);
        } else {
            action.setText(ACTION_EDIT);
        }
    }

    @Override
    public void windowStateChange(WindowStateEvent event) {

    }

}
