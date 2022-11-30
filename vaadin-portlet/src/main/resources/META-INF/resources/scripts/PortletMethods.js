/*
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
//<![CDATA[
// Liferay parses this file as XML, so make it appear as a CDATA section
window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
// <liferay>
// 7.2.1-ga2 should create and populate these for us.
// Forcing object generation for hub registration later on.
window.portlet = window.portlet || {};
window.portlet.data = window.portlet.data || {};
window.portlet.data.pageRenderState = window.portlet.data.pageRenderState || {};
window.portlet.data.pageRenderState.portlets = window.portlet.data.pageRenderState.portlets ||{};
window.portlet.data.pageRenderState.encodedCurrentURL = window.portlet.data.pageRenderState.encodedCurrentURL || encodeURIComponent(window.location.origin);
// </liferay>

if (!window.Vaadin.Flow.Portlets) {

    window.Vaadin.Flow.Portlets = {};

    window.Vaadin.Flow.Portlets.executeWhenHubIdle = function (hub, task) {
        var poller = function () {
            if (hub.isInProgress()) {
                setTimeout(poller, 10);
            } else {
                task(hub);
            }
        };
        poller();
    };

    window.Vaadin.Flow.Portlets.getHubRegistartion = function (portletRegistryName) {
        return window.Vaadin.Flow.Portlets[portletRegistryName].hub;
    };

    window.Vaadin.Flow.Portlets.setPortletState = function (portletRegistryName, windowState, portletMode, reloadAfterChange) {
        const hub = window.Vaadin.Flow.Portlets.getHubRegistartion(portletRegistryName);

        window.Vaadin.Flow.Portlets.executeWhenHubIdle(hub, function(hub) {
            const state = hub.newState();
            state.windowState = windowState;
            state.portletMode = portletMode;
            hub.setRenderState(state);

            if (reloadAfterChange) {
                window.Vaadin.Flow.Portlets.executeWhenHubIdle(hub, function (hub) { location.reload() });
            }
        });
    }

    window.Vaadin.Flow.Portlets.fireEvent = function (portletRegistryName, event, parameters) {
        var hub = window.Vaadin.Flow.Portlets.getHubRegistartion(portletRegistryName);

        var params = hub.newParameters();
        Object.getOwnPropertyNames(parameters).forEach(
            function (prop) {
                params[prop] = parameters[prop];
            });

        hub.dispatchClientEvent(event, params);
    };

    window.Vaadin.Flow.Portlets.registerElement = function (tag, portletRegistryName, windowStates, portletModes, actionUrl) {
        // <liferay>
        // Force objects, urls and arrays for liferay portlet data to enable hub registration and hub usage
        window.portlet.data.pageRenderState.portlets[portletRegistryName] = window.portlet.data.pageRenderState.portlets[portletRegistryName] || {};
        window.portlet.data.pageRenderState.portlets[portletRegistryName].allowedPM = portletModes;
        window.portlet.data.pageRenderState.portlets[portletRegistryName].allowedWS = windowStates;
        window.portlet.data.pageRenderState.portlets[portletRegistryName].encodedActionURL = encodeURIComponent(actionUrl);
        // liferay 7.3 does not always check if the renderData is there
        window.portlet.data.pageRenderState.portlets[portletRegistryName].renderData =
            window.portlet.data.pageRenderState.portlets[portletRegistryName].renderData || { content: null, mimeType: "text/html" };
        // </liferay>
        customElements.whenDefined(tag).then(function () {
            var elem = document.querySelector(tag);
            elem.constructor._getClientStrategy = function (portletComponent) {
                var clients = elem.constructor._getClients();
                if (!clients) {
                    return undefined;
                }
                var portlet = window.Vaadin.Flow.Portlets[portletComponent.getAttribute('data-portlet-id')];
                return clients[portlet.appId];
            };
            window.Vaadin.Flow.Portlets.registerHub(tag, portletRegistryName, elem);
        });
    };

    window.Vaadin.Flow.Portlets.registerHub = function (tag, portletRegistryName, elem) {
        var targetElem;
        var allPortletElems = document.querySelectorAll(tag);
        for (var i = 0; i !== allPortletElems.length; i++) {
            if (allPortletElems[i].getAttribute('data-portlet-id') === portletRegistryName) {
                targetElem = allPortletElems[i];
                break;
            }
        }
        var afterServerUpdate = targetElem.afterServerUpdate;
        targetElem.afterServerUpdate = function () {
            if (afterServerUpdate) {
                afterServerUpdate();
            }

            window.Vaadin.Flow.Portlets[portletRegistryName] = window.Vaadin.Flow.Portlets[portletRegistryName] || {};

            var portletObj = window.Vaadin.Flow.Portlets[portletRegistryName];
            if (!portletObj.hub) {
                if (portlet) {
                    portlet.register(portletRegistryName).then(function (hub) {
                        portletObj.hub = hub;

                        hub.addEventListener('portlet.onStateChange', function (type, state) {
                        });
                        portletObj.eventPoller = window.Vaadin.Flow.Portlets.eventPoller;
                        if (portletObj.listeners) {
                            Object.getOwnPropertyNames(portletObj.listeners).forEach(
                              function (uid) {
                                  portletObj.registerListener(portletObj.listeners[uid], uid);
                              }
                            );
                            delete portletObj.listeners;
                        }
                    });
                    targetElem.afterServerUpdate = afterServerUpdate;
                }
            }
        };
        window.Vaadin.Flow.Portlets.initListenerRegistration(portletRegistryName, elem);
    };

    window.Vaadin.Flow.Portlets.eventPoller = function (portletObj, type, payload, uid, elem) {
        var hub = portletObj.hub;
        if (hub.isInProgress()) {
            setTimeout(function () {
                portletObj.eventPoller(portletObj, type, payload, uid, elem);
            }, 10);
        } else {
            var params = hub.newParameters();
            params['vaadin.ev'] = [];
            params['vaadin.ev'][0] = type;
            params['vaadin.uid'] = [];
            params['vaadin.uid'][0] = uid;
            params['vaadin.wn'] = [];
            params['vaadin.wn'][0] = window.name;
            if (payload) {
                Object.getOwnPropertyNames(payload).forEach(
                    function (prop) {
                        params[prop] = payload[prop];
                    });
            }
            hub.action(params).then(function () {
                /* call {@code action} method on the hub is not enough: it won't
                 * be an UIDL request. We need to make a fake UIDL request so
                 * that the client state is updated according to the server side
                 * state.
                 */
                var clients = elem.constructor._getClients();
                clients[portletObj.appId].poll();
            });
        }
    };

    window.Vaadin.Flow.Portlets.initListenerRegistration = function (portletRegistryName, elem) {
        window.Vaadin.Flow.Portlets[portletRegistryName] = window.Vaadin.Flow.Portlets[portletRegistryName] || {};

        var portletObj = window.Vaadin.Flow.Portlets[portletRegistryName];
        portletObj._regListener = function (eventType, uid) {
            let poller = portletObj.eventPoller;
            let handle = portletObj.hub.addEventListener(eventType, function (type, payload) {
                poller(portletObj, type, payload, uid, elem);
            });
            portletObj.eventHandles = portletObj.eventHandles || {};
            portletObj.eventHandles[uid] = handle;
        };
        portletObj.registerListener = function (eventType, uid) {
            if (portletObj.hub) {
                portletObj._regListener(eventType, uid);
            } else {
                portletObj.listeners = portletObj.listeners || {};
                portletObj.listeners[uid] = eventType;
            }
        };
        portletObj._removeListener = function (uid) {
            if (portletObj.eventHandles) {
                if(portletObj.eventHandles[uid]) {
                    portletObj.hub.removeEventListener(portletObj.eventHandles[uid]);
                    delete portletObj.eventHandles[uid];
                }
            }
        };
        portletObj.unregisterListener = function (uid) {
            if (portletObj.hub) {
                portletObj._removeListener(uid);
            } else {
                delete portletObj.listeners[uid];
            }
        }
    };
}
//]]>
