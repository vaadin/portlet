package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.WindowState;
import javax.xml.namespace.QName;

import com.vaadin.flow.component.UI;

public class GridPortlet extends TheseInVaadinPortlet<GridPortletView> {

    public static final String TAG = "grid-portlet";
    private static final String SELECTION_EVENT = "Selection";
    private String EVENT_NAMESPACE = "";

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

    public static GridPortlet getCurrent() {
        return (GridPortlet) VaadinPortlet.getCurrent();
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) {
        if (request.getActionParameters().getValue("selection") != null) {
            String selection = request.getActionParameters()
                    .getValue("selection");
            if (WindowState.MAXIMIZED.toString()
                    .equals(request.getActionParameters()
                            .getValue("windowState"))) {
                response.getRenderParameters().setValue("windowState",
                        WindowState.MAXIMIZED.toString());
            }
            response.getRenderParameters().setValue("contactId", selection);
            QName qn = new QName(EVENT_NAMESPACE, SELECTION_EVENT);
            response.setEvent(qn, "Selection event");
        }
    }

    protected void registerEventListener(String eventType) {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
        StringBuilder register = new StringBuilder();
        register.append(String.format("var hub = window.Vaadin.Flow.Portlets['%s'];",
                portletRegistryName));

        register.append("const poller = () => {");
        register.append("  if(hub.isInProgress()) {");
        register.append("    setTimeout(poller, 10);");
        register.append("  } else {");
        register.append(String.format("hub.addEventListener('%s', function (payload) {window.alert('EVENT');});", eventType));
        register.append("  }");
        register.append("};");
        register.append("poller();");
        UI.getCurrent().getElement().executeJs(register.toString());
    }
}
