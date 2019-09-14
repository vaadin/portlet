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
package com.vaadin.flow.portal.addressbook.grid;

import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.addressbook.form.FormPortlet;

/**
 * @author Vaadin Ltd
 *
 */
public class GridView extends Grid<Contact> {

    private ListDataProvider<Contact> dataProvider;

    public GridView() {
        super(Contact.class);
        dataProvider = new ListDataProvider<>(
                ContactService.getDemoService().findAll(""));
        setDataProvider(dataProvider);
        removeColumnByKey("id");
        setSelectionMode(SelectionMode.NONE);
        addItemClickListener(this::notifyForm);
        setWidth("300px");
        setColumns("firstName", "lastName", "phoneNumber", "email",
                "birthDate");
    }

    @ClientCallable
    private void refresh(int id) {
        Optional<Contact> contact = dataProvider.getItems().stream()
                .filter(item -> item.getId().equals(id)).findFirst();
        if (contact.isPresent()) {
            Contact newContact = ContactService.getDemoService().findById(id)
                    .get();
            contact.get().setFirstName(newContact.getFirstName());
            contact.get().setLastName(newContact.getLastName());
            contact.get().setEmail(newContact.getEmail());
            contact.get().setPhoneNumber(newContact.getPhoneNumber());
            contact.get().setBirthDate(newContact.getBirthDate());
            dataProvider.refreshItem(contact.get());
        }
    }

    private void notifyForm(ItemClickEvent<Contact> event) {
        getUI().get().getPage().executeJs(
                "var form = document.querySelector($0).firstChild;"
                        + "form.$server.show($1);",
                FormPortlet.TAG, event.getItem().getId());
    }

}
