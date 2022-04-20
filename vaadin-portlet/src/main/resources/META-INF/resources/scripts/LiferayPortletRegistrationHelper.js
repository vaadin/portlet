/*
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
(function( scriptUrls, callback ) {

    const alreadyLoadedScripts = [...document.querySelectorAll("script")]
        .filter(function(el) { return scriptUrls.indexOf(el.src) >= 0; })
        .map(function(el) { return el.src; });

    const createScript = function(url) {
        if (alreadyLoadedScripts.indexOf(url) >= 0) {
            // Script already present on page, no need to load it again
            return Promise.resolve(url);
        }
        return new Promise(function(resolve, reject) {
            const script = document.createElement("script")
            script.type = "text/javascript";
            script.onload = function() {
              resolve(url);
            };
            script.src = url;
            document.getElementsByTagName( "head" )[0].appendChild( script );
        });
    };

    const isRegistrationScriptInitialized = function() {
        return window.Vaadin && window.Vaadin.Flow
            && window.Vaadin.Flow.Portlets
            && window.Vaadin.Flow.Portlets.registerElement;
    };

    const poller = function (attempt) {
        if(isRegistrationScriptInitialized()) {
            callback();
        } else if (attempt < 100) {
            // PortletMethods.js not yet loaded, try again
            setTimeout(function() { poller(attempt + 1) }, 50);
        } else {
            console.log("PortletMethods.js not loaded, element cannot be registered");
        }
    };

    Promise.all(scriptUrls.map(createScript)).then(function() { poller(0); });

})
