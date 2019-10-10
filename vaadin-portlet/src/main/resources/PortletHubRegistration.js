if (!window.Vaadin.Flow.Portlets) {
    window.Vaadin.Flow["Portlets"] = {};
}
if (!window.Vaadin.Flow.Portlets.$0) {
    if (portlet) {
        portlet.register($0).then(function (hub) {
            window.Vaadin.Flow.Portlets[$0] = hub;
            hub.addEventListener('portlet.onStateChange', function () {

            });
        });
    }
}
