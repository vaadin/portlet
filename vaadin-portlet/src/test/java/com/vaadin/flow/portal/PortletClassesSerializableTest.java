package com.vaadin.flow.portal;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class PortletClassesSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        final Stream<String> portletExcludes = Stream.of(
                "com\\.vaadin\\.flow\\.portal\\.PortletConstants",
                // these 2 can be made serializable, if we introduce a
                // serializable wrapper for the wrapped portlet request and
                // response
                "com\\.vaadin\\.flow\\.portal\\.VaadinPortletRequest",
                "com\\.vaadin\\.flow\\.portal\\.VaadinPortletResponse",
                // this can be made serializable once VaadinPortletRequest is
                // serializable
                "com\\.vaadin\\.flow\\.portal\\.internal\\." +
                        "PortletStreamReceiverHandler\\$StreamRequestContext"

        );
        return Stream.concat(super.getExcludedPatterns(), portletExcludes);
    }

    @Override
    protected Pattern getJarPattern() {
        return Pattern.compile("(.*vaadin.*)/(.*flow.*)/(.*portal.*)\\.jar");

    }

    @Override
    protected Stream<String> getBasePackages() {
        return Stream.of("com.vaadin.flow.portal");
    }
}
