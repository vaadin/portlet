package com.vaadin.flow.portal;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import org.apache.commons.io.IOUtils;

import javax.portlet.PortletMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FormPortletView extends FormLayout implements
        WindowStateHandler, PortletModeHandler {

    private Binder<Contact> binder;
    private Button save;
    private Button cancel;
    private Button normalize;
    private TextField firstName;
    private Image image;

    public FormPortletView() {
        registerHub();

        PortletMode portletMode = VaadinPortletRequest.getCurrent()
                .getPortletMode();

        firstName = new TextField();
        addFormItem(firstName, "First name");

        TextField lastName = new TextField();
        addFormItem(lastName, "Last name");

        TextField phone = new TextField();
        addFormItem(phone, "Phone number");

        EmailField email = new EmailField();
        addFormItem(email, "Email");

        binder = new Binder<>(Contact.class);
        binder.bind(firstName, "firstName");
        binder.bind(lastName, "lastName");
        binder.bind(email, "email");
        binder.bind(phone, "phoneNumber");
        // Set the state of form depending on portlet mode.
        binder.setReadOnly(PortletMode.VIEW.equals(portletMode));

        image = new Image();
        add(image, new Span());

        save = new Button("Save", event -> save());
        save.setEnabled(false);
        cancel = new Button("Cancel", event -> cancel());
        normalize = new Button("normalize", event -> normalize());
        normalize.setVisible(false);

        add(save, cancel, normalize);
    }

    private void normalize() {

    }

    @ClientCallable
    private void select(int contactId) {
        Optional<Contact> contact = ContactService.getInstance()
                .findById(contactId);
        if (contact.isPresent()) {
            binder.setBean(contact.get());

            firstName.setValue(contact.get().getFirstName());
            image.setSrc(contact.get().getImage().toString());
            save.setEnabled(true);
        } else {
            cancel();
        }
    }

    private void cancel() {
        binder.setBean(null);
        save.setEnabled(false);
    }

    private void save() {
        Contact contact = binder.getBean();

        if (true) {
            ContactService.getInstance().save(contact);
            getUI().get().getPage().executeJs(
                    "var grid = document.querySelector($0).firstChild;"
                            + "grid.$server.refresh($1);",
                    GridPortlet.TAG, contact.getId());
        }
    }

    @Override
    public void portletModeChange(PortletModeEvent event) {
        binder.setReadOnly(PortletMode.VIEW.equals(event.getPortletMode()));
    }

    @Override
    public void windowStateChange(WindowStateEvent event) {
        normalize.setVisible(event.isMaximized());
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
            getElement().executeJs("var hub = window.Vaadin.Flow.Portlets.$0;" +
                            "hub.addEventListener('selection', fucntion(id) {$1.$server.select(id);});", portletRegistryName,
                    getElement());
        } catch (IOException e) {
        }
    }
}
