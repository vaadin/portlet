/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.portal.addressbook.form;

import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.addressbook.grid.GridPortlet;

/**
 * @author Vaadin Ltd
 *
 */
public class Form extends FormLayout {

    private Binder<Contact> binder;
    private Button save;
    private Button cancel;
    TextField firstName;

    public Form() {
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

        save = new Button("Save", event -> save());
        add(save);
        save.setEnabled(false);

        cancel = new Button("Cancel", event -> cancel());
        add(cancel);
    }

    @ClientCallable
    private void show(int contactId) {
        Optional<Contact> contact = ContactService.getDemoService()
                .findById(contactId);
        if (contact.isPresent()) {
            binder.setBean(contact.get());

            firstName.setValue(contact.get().getFirstName());

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
            ContactService.getDemoService().save(contact);
            getUI().get().getPage().executeJs(
                    "var grid = document.querySelector($0).firstChild;"
                            + "grid.$server.refresh($1);",
                    GridPortlet.TAG, contact.getId());
        }
    }
}
