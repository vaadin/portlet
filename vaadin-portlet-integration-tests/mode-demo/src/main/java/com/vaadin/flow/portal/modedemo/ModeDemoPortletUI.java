package com.vaadin.flow.portal.modedemo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.portal.VaadinPortletUI;

public class ModeDemoPortletUI extends
        VaadinPortletUI<ModeDemoView, ModeDemoEdit, ModeDemoHelp> {

    // Dummy declarations for the bytecode scanner to pick up the components
    // used in ModeDemo{View,Edit,Help}. They will not be needed after
    // https://github.com/vaadin/flow/issues/6492 is fixed.
    ModeDemoView view;
    ModeDemoEdit edit;
    ModeDemoHelp help;

}
