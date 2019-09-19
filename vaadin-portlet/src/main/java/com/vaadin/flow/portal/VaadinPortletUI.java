package com.vaadin.flow.portal;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderState;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.googlecode.gentyref.GenericTypeReflector;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.ReflectTools;

/**
 * A component representing a portlet instance. The actual component shown
 * depends on the mode given in the render request {@link RenderState#getPortletMode}.
 *
 * @param <VIEW> the view component type
 * @param <EDIT> the edit component type
 * @param <HELP> the help component type
 */
public abstract class VaadinPortletUI<VIEW extends Component,
        EDIT extends Component, HELP extends Component> extends Component {

    private VIEW view = null;
    private EDIT edit = null;
    private HELP help = null;
    private Component current = null;

    public VaadinPortletUI() {
        super(null);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        updateContentFromMode();
    }

    @Override
    public Element getElement() {
        return getContent().getElement();
    }

    @Override
    public Stream<Component> getChildren() {
        return Stream.of(getContent());
    }

    /**
     * Currently displayed component. Initializes the component if required.
     */
    public Component getContent() {
        if (current == null) {
            updateContentFromMode();
        }
        return current;
    }

    /**
     * The view component. Initializes the component if required.
     */
    public VIEW getViewComponent() {
        if (view == null) {
            view = createViewComponent();
        }
        return view;
    }

    /**
     * The edit component. Initializes the component if required.
     */
    public EDIT getEditComponent() {
        if (edit == null) {
            edit = createEditComponent();
        }
        return edit;
    }

    /**
     * The help component. Initializes the component if required.
     */
    public HELP getHelpComponent() {
        if (help == null) {
            help = createHelpComponent();
        }
        return help;
    }

    /**
     * Creates the component for view mode. By default creates an instance of
     * the first type parameter. Override for custom creation.
     */
    protected VIEW createViewComponent() {
        return (VIEW) createInstance(getNthTypeParameter(0));
    }

    /**
     * Creates the component for edit mode. By default creates an instance of
     * the second type parameter. Override for custom creation.
     */
    protected EDIT createEditComponent() {
        return (EDIT) createInstance(getNthTypeParameter(1));
    }

    /**
     * Creates the component for view mode. By default creates an instance of
     * the third type parameter. Override for custom creation.
     */
    protected HELP createHelpComponent() {
        return (HELP) createInstance(getNthTypeParameter(2));
    }

    private void updateContentFromMode() {
        final PortletRequest pr = VaadinPortletRequest.getCurrent()
                .getPortletRequest();
        if (PortletMode.HELP.equals(pr.getPortletMode())) {
            setContent(getHelpComponent());
        } else if (PortletMode.EDIT.equals(pr.getPortletMode())) {
            setContent(getEditComponent());
        } else {
            setContent(getViewComponent());
        }
    }

    private void setContent(Component newCurrent) {
        if (this.current != newCurrent) {
            Element parent = null;
            if (current != null && current.getElement() != null) {
                parent = current.getElement().getParent();
                current.getElement().removeFromParent();
            }
            this.current = newCurrent;
            Element element = current.getElement();
            element.getStateProvider().setComponent(element.getNode(), this);
            if (parent != null) {
                element.removeFromTree();
                parent.setChild(0, element);
            }
        }
    }

    private Component createInstance(Type type) {
        if (type instanceof Class || type instanceof ParameterizedType) {
            final Class<? extends Component> componentClass =
                    GenericTypeReflector.erase(type).asSubclass(Component.class);
            return ReflectTools.createInstance(componentClass);
        }
        throw new IllegalArgumentException("Unable to instantiate component " +
                "of type " + type);
    }

    private Type getNthTypeParameter(int n) {
        return GenericTypeReflector.getTypeParameter(
                getClass().getGenericSuperclass(),
                VaadinPortletUI.class.getTypeParameters()[n]);
    }
}
