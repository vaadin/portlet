To run the Address book demo
1. install vaadin-portlet (either `mvn install -DskipTests` in root or in the vaadin-portlet module)
2. for the first run in vaadin-portlet-demo/address-book run `mvn pacakage cargo:run -Pautosetup`
       - Pluto portlet will start on 127.0.0.1:8080/pluto/portal/
    for consecutive runs use `mvn package cargo:run -Pautocopy`