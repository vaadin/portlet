package com.vaadin.flow.portal.modedemo;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ModeDemoHelp extends VerticalLayout {

    public ModeDemoHelp() {
        add(new Label("VIEW: Click the button to show a message."));
        add(new Label("EDIT: If you are an 'administrator', edit the message shown in view mode ."));
        add(new Label("HELP: You are reading it."));
    }
}
