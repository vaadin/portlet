package com.vaadin.flow.portal.modedemo;

import javax.portlet.PortletRequest;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.portal.VaadinPortletRequest;

public class ModeDemoEdit extends VerticalLayout {

    TextArea textArea = new TextArea("Edit popup message");

    Button applyButton = new Button("Apply", ce -> {
        final ModeDemoPortlet portlet = (ModeDemoPortlet) VaadinPortlet.getCurrent();
        portlet.setMessage(textArea.getValue());
        resetTextArea();
        Notification.show("Message successfully updated for portlet.");
    });

    Button resetButton = new Button("Reset", ce -> resetTextArea());

    public ModeDemoEdit() {
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        final PortletRequest portletRequest =
                VaadinPortletRequest.getCurrent().getPortletRequest();
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        textArea.setValueChangeMode(ValueChangeMode.EAGER);
        textArea.addValueChangeListener(vce -> {
            applyButton.setEnabled(true);
            resetButton.setEnabled(true);
        });
        if (portletRequest.isUserInRole("administrator")) {
            add(textArea, new HorizontalLayout(resetButton, applyButton));
            resetTextArea();
        } else {
            add(new Label("View N/A. Try logging in as 'tomcat', pw 'tomcat'"));
        }
    }

    private void resetTextArea() {
        final ModeDemoPortlet portlet = (ModeDemoPortlet) VaadinPortlet.getCurrent();
        textArea.setValue(portlet.getMessage());
        applyButton.setEnabled(false);
        resetButton.setEnabled(false);
    }
}
