package com.vaadin.flow.portal;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;

public class PortletContentExporter extends WebComponentExporter<PortletContentExporter.PortletComponentHost> {
    public PortletContentExporter() {
        super("vaadin-portlet");
        // used to check that the portlet is what we expect. @see VaadinPortlet
        addProperty("portlet", "").onChange(PortletComponentHost::setPortletName);
    }

    @Override
    protected void configureInstance(WebComponent<PortletContentExporter.PortletComponentHost> webComponent,
                                     PortletContentExporter.PortletComponentHost component) {
    }

    @Tag("inner-component")
    public static class PortletComponentHost extends Component {

        public void setPortletName(String portletName) {
            VaadinPortlet portlet = VaadinPortlet.getCurrent();
            assert portlet != null;
            if (portlet.getClass().getCanonicalName().equals(portletName)) {
                Component source = portlet.getComponent();
                getElement().appendChild(source.getElement());
            }
            else {
                LoggerFactory.getLogger(getClass()).info("Portlet name '{}' " +
                        "did not match the received name '{}'!",
                        portlet.getClass().getCanonicalName(), portletName);
            }
        }
    }
}
