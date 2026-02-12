/*
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
//<![CDATA[
// Liferay parses this file as XML, so make it appear as a CDATA section
// Liferay's portlet hub (register.es.js) references 'global' (Node.js convention)
if (typeof global === 'undefined') {
    globalThis.global = globalThis;
}
globalThis.Vaadin = globalThis.Vaadin || {};
globalThis.Vaadin.Flow = globalThis.Vaadin.Flow || {};
// <liferay>
// 7.2.1-ga2 should create and populate these for us.
// Forcing object generation for hub registration later on.
globalThis.portlet = globalThis.portlet || {};
globalThis.portlet.data = globalThis.portlet.data || {};
globalThis.portlet.data.pageRenderState = globalThis.portlet.data.pageRenderState || {};
globalThis.portlet.data.pageRenderState.portlets = globalThis.portlet.data.pageRenderState.portlets ||{};
globalThis.portlet.data.pageRenderState.encodedCurrentURL = globalThis.portlet.data.pageRenderState.encodedCurrentURL || encodeURIComponent(globalThis.location.origin);
// </liferay>

if (!globalThis.Vaadin.Flow.Portlets) {

    globalThis.Vaadin.Flow.Portlets = {};

    globalThis.Vaadin.Flow.Portlets.executeWhenHubIdle = function (hub, task) {
        let poller = function () {
            if (hub.isInProgress()) {
                setTimeout(poller, 10);
            } else {
                task(hub);
            }
        };
        poller();
    };

    globalThis.Vaadin.Flow.Portlets.getHubRegistartion = function (portletRegistryName) {
        return globalThis.Vaadin.Flow.Portlets[portletRegistryName].hub;
    };

    globalThis.Vaadin.Flow.Portlets.setPortletState = function (portletRegistryName, windowState, portletMode, reloadAfterChange) {
        let waitForHub = function() {
            let portletObj = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
            if (!portletObj || !portletObj.hub) {
                setTimeout(waitForHub, 10);
                return;
            }
            let hub = portletObj.hub;
            globalThis.Vaadin.Flow.Portlets.executeWhenHubIdle(hub, function(hub) {
                const state = hub.newState();
                state.windowState = windowState;
                state.portletMode = portletMode;
                hub.setRenderState(state);

                if (reloadAfterChange) {
                    globalThis.Vaadin.Flow.Portlets.executeWhenHubIdle(hub, function (hub) { location.reload() });
                }
            });
        };
        waitForHub();
    }

    globalThis.Vaadin.Flow.Portlets.fireEvent = function (portletRegistryName, event, parameters) {
        const waitForHub = function() {
            let portletObj = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
            if (!portletObj || !portletObj.hub) {
                setTimeout(waitForHub, 10);
                return;
            }
            let hub = portletObj.hub;
            let params = hub.newParameters();
            Object.getOwnPropertyNames(parameters).forEach(
                function (prop) {
                    params[prop] = parameters[prop];
                });

            hub.dispatchClientEvent(event, params);
        };
        waitForHub();
    };

    // Store Vaadin portlet data in a location Liferay won't overwrite
    globalThis.Vaadin.Flow.Portlets._liferayData = globalThis.Vaadin.Flow.Portlets._liferayData || {};

    globalThis.Vaadin.Flow.Portlets.registerElement = function (tag, portletRegistryName, windowStates, portletModes, actionUrl) {
        // <liferay>
        // Force objects, urls and arrays for liferay portlet data to enable hub registration and hub usage
        // IMPORTANT: Liferay's <aui:script> may replace the entire pageRenderState object after this runs,
        // so we store a backup copy in _liferayData and re-inject before portlet.register() is called.
        try {
            const portletData = {
                allowedPM: portletModes,
                allowedWS: windowStates,
                encodedActionURL: encodeURIComponent(actionUrl),
                renderData: { content: null, mimeType: "text/html" },
                state: {
                    parameters: {},
                    portletMode: portletModes && portletModes.length > 0 ? portletModes[0] : 'view',
                    windowState: windowStates && windowStates.length > 0 ? windowStates[0] : 'normal'
                }
            };

            // Store backup copy that Liferay won't touch
            globalThis.Vaadin.Flow.Portlets._liferayData[portletRegistryName] = portletData;

            // Also write to pageRenderState (may be overwritten by Liferay later)
            globalThis.portlet.data.pageRenderState.portlets[portletRegistryName] =
                globalThis.portlet.data.pageRenderState.portlets[portletRegistryName] || {};
            Object.assign(globalThis.portlet.data.pageRenderState.portlets[portletRegistryName], portletData);
        } catch (e) {
            console.warn('Vaadin Portlet: Could not initialize Liferay pageRenderState for ' + portletRegistryName, e);
        }
        // </liferay>
        customElements.whenDefined(tag).then(function () {
            let elem = document.querySelector(tag + "[data-portlet-id='" + portletRegistryName + "']");
            if (!elem) {
                // Fallback: try to find any element with this tag
                elem = document.querySelector(tag);
            }
            if (!elem) {
                console.error('Vaadin Portlet: Could not find element with tag ' + tag + ' for portlet ' + portletRegistryName);
                return;
            }
            elem.constructor._getClientStrategy = function (portletComponent) {
                const clients = elem.constructor._getClients();
                if (!clients) {
                    return undefined;
                }
                const portletObj = globalThis.Vaadin.Flow.Portlets[portletComponent.getAttribute('data-portlet-id')];
                if (!portletObj) {
                    return undefined;
                }
                return clients[portletObj.appId];
            };
            globalThis.Vaadin.Flow.Portlets.registerHub(tag, portletRegistryName, elem);
        }).catch(function(error) {
            console.error('Vaadin Portlet: Error waiting for custom element ' + tag + ' to be defined', error);
        });
    };

    globalThis.Vaadin.Flow.Portlets.registerHub = function (tag, portletRegistryName, elem) {
        let targetElem;
        let allPortletElems = document.querySelectorAll(tag);
        for (let i = 0; i !== allPortletElems.length; i++) {
            if (allPortletElems[i].getAttribute('data-portlet-id') === portletRegistryName) {
                targetElem = allPortletElems[i];
                break;
            }
        }

        if (!targetElem) {
            console.error('Vaadin Portlet: Could not find element with tag ' + tag + ' and data-portlet-id ' + portletRegistryName);
            return;
        }

        let doHubRegistration = function() {
            globalThis.Vaadin.Flow.Portlets[portletRegistryName] = globalThis.Vaadin.Flow.Portlets[portletRegistryName] || {};

            let portletObj = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
            if (!portletObj.hub) {
                if (typeof portlet !== 'undefined' && portlet && typeof portlet.register === 'function') {
                    // <liferay>
                    // Re-inject Vaadin portlet data if Liferay's <aui:script> replaced pageRenderState
                    // This happens when Liferay fires its deferred scripts between registerElement and now
                    try {
                        let liferayData = globalThis.Vaadin.Flow.Portlets._liferayData;
                        if (liferayData && liferayData[portletRegistryName]) {
                            let portlets = globalThis.portlet.data.pageRenderState.portlets;
                            if (!portlets[portletRegistryName]) {
                                // Data was wiped by Liferay, re-inject from backup
                                portlets[portletRegistryName] = liferayData[portletRegistryName];
                            }
                        }
                    } catch (e) {
                        console.warn('Vaadin Portlet: Could not re-inject pageRenderState for ' + portletRegistryName, e);
                    }
                    // </liferay>

                    portlet.register(portletRegistryName).then(function (hub) {
                        portletObj.hub = hub;

                        hub.addEventListener('portlet.onStateChange', function (type, state) {
                        });
                        portletObj.eventPoller = globalThis.Vaadin.Flow.Portlets.eventPoller;
                        if (portletObj.listeners) {
                            Object.getOwnPropertyNames(portletObj.listeners).forEach(
                                function (uid) {
                                    portletObj.registerListener(portletObj.listeners[uid], uid);
                                }
                            );
                            delete portletObj.listeners;
                        }
                    }).catch(function(error) {
                        console.error('Vaadin Portlet: Failed to register with Portlet Hub for ' + portletRegistryName, error);
                    });
                } else {
                    console.warn('Vaadin Portlet: Portlet Hub (globalThis.portlet.register) not available for ' + portletRegistryName + '. IPC features will not work.');
                }
            }
        };

        let afterServerUpdate = targetElem.afterServerUpdate;
        let hookInstalled = false;

        targetElem.afterServerUpdate = function () {
            hookInstalled = true;
            if (afterServerUpdate) {
                afterServerUpdate();
            }
            doHubRegistration();
            targetElem.afterServerUpdate = afterServerUpdate;
        };

        // If the element is already connected and has a client, try registering immediately
        // This handles the case where afterServerUpdate was already called before we hooked in
        if (targetElem.isConnected) {
            // Check if the element has already been initialized by Vaadin
            let clients = elem.constructor._getClients && elem.constructor._getClients();
            let portletData = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
            if (clients && portletData && portletData.appId && clients[portletData.appId]) {
                // Element is already initialized, register immediately
                doHubRegistration();
            } else {
                // Element is connected but not yet initialized, wait a bit and check again
                setTimeout(function() {
                    if (!hookInstalled) {
                        // afterServerUpdate wasn't called yet, but element might be ready
                        let retryClients = elem.constructor._getClients && elem.constructor._getClients();
                        let retryPortletData = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
                        if (retryClients && retryPortletData && retryPortletData.appId && retryClients[retryPortletData.appId]) {
                            doHubRegistration();
                        }
                    }
                }, 100);
            }
        }

        globalThis.Vaadin.Flow.Portlets.initListenerRegistration(portletRegistryName, elem);
    };

    globalThis.Vaadin.Flow.Portlets.eventPoller = function (portletObj, type, payload, uid, elem) {
        let hub = portletObj.hub;
        if (hub.isInProgress()) {
            setTimeout(function () {
                portletObj.eventPoller(portletObj, type, payload, uid, elem);
            }, 10);
        } else {
            let params = hub.newParameters();
            params['vaadin.ev'] = [];
            params['vaadin.ev'][0] = type;
            params['vaadin.uid'] = [];
            params['vaadin.uid'][0] = uid;
            params['vaadin.wn'] = [];
            params['vaadin.wn'][0] = globalThis.name;
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
                let clients = elem.constructor._getClients();
                clients[portletObj.appId].poll();
            });
        }
    };

    globalThis.Vaadin.Flow.Portlets.initListenerRegistration = function (portletRegistryName, elem) {
        globalThis.Vaadin.Flow.Portlets[portletRegistryName] = globalThis.Vaadin.Flow.Portlets[portletRegistryName] || {};

        let portletObj = globalThis.Vaadin.Flow.Portlets[portletRegistryName];
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
