window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};

if (!window.Vaadin.Flow.Portlets) {
    window.Vaadin.Flow.Portlets = {};

    window.Vaadin.Flow.Portlets.reload = function (hub) {
        var poller = function () {
            if (hub.isInProgress()) {
                setTimeout(poller, 10);
            } else {
                location.reload();
            }
        };
        poller();
    }

    window.Vaadin.Flow.Portlets.getHubRegistartion = function (portletRegistryName) {
        return window.Vaadin.Flow.Portlets[portletRegistryName].hub;
    }

    window.Vaadin.Flow.Portlets.setPortletState = function (portletRegistryName, windowState, portletMode) {
        var hub = window.Vaadin.Flow.Portlets.getHubRegistartion(portletRegistryName);

        var state = hub.newState();
        state.windowState = windowState;
        state.portletMode = portletMode;
        hub.setRenderState(state);

        reload(hub);
    }

    window.Vaadin.Flow.Portlets.fireEvent = function (portletRegistryName, event, parameters) {
        var hub = window.Vaadin.Flow.Portlets.getHubRegistartion(portletRegistryName);

        var params = hub.newParameters();
        Object.getOwnPropertyNames(parameters).forEach(
            function (prop) {
                params[prop] = parameters[prop];
            });

        hub.dispatchClientEvent(event, params);
    }

    window.Vaadin.Flow.Portlets.registerHub = function (tag, portletRegistryName, elem) {
        var targetElem;
        var allPortletElems = document.querySelectorAll(tag);
        for (i = 0; i < allPortletElems.length; i++) {
            if (allPortletElems[i].getAttribute('data-portlet-id') == portletRegistryName) {
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
            if (!portletObj.hub && portlet) {
                portlet.register(portletRegistryName).then(function (hub) {
                    portletObj.hub = hub;

                    hub.addEventListener('portlet.onStateChange', function (type, state) {});
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
        };
        window.Vaadin.Flow.Portlets.initListenerRegistration(portletRegistryName, elem);
    }

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
    }

    window.Vaadin.Flow.Portlets.initListenerRegistration = function (portletRegistryName, elem) {
        window.Vaadin.Flow.Portlets[portletRegistryName] = window.Vaadin.Flow.Portlets[portletRegistryName] || {};

        var portletObj = window.Vaadin.Flow.Portlets[portletRegistryName];
        portletObj._regListener = function (eventType, uid) {
            var poller = portletObj.eventPoller;
            var handle = portletObj.hub.addEventListener(eventType, function (type, payload) {
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
            if (portletObj.eventHandles && portletObj.eventHandles[uid]) {
                portletObj.hub.removeEventListener(portletObj.eventHandles[uid]);
                delete portletObj.eventHandles[uid];
            }
        };
        portletObj.unregisterListener = function (uid) {
            if (portletObj.hub) {
                portletObj._removeListener(uid);
            } else {
                delete portletObj.listeners[uid];
            }
        }
    }
}
