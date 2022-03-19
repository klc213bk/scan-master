module scan {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
	requires com.jfoenix;
    requires transitive org.update4j;
	requires starter;
    requires org.glassfish.jaxb.runtime;
    requires jakarta.xml.bind;
	requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
	requires org.apache.commons.codec;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.jsoup;
    requires com.asprise.imaging.scan;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.desktop;

    exports com.tgl.scan.main;
    exports com.tgl.scan.main.bean;
    exports com.tgl.scan.main.http;
    exports com.tgl.scan.main.log;
    exports com.tgl.scan.main.service;
    exports com.tgl.scan.main.util;
    exports com.tgl.scan.main.view;

    opens com.tgl.scan.main.bean;
    opens com.tgl.scan.main.view;
    opens images;

    provides org.update4j.service.Launcher with com.tgl.scan.main.AppLauncher;

//    exports javafx.base/com.sun.javafx.event=com.jfoenix
//    exports javafx.graphics/com.sun.javafx.scene=com.jfoenix
//    exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED ------> X
//    exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED ------> X
//    exports com.asprise.imaging.scan/com.sun.media.jai.codec=ALL-UNNAMED ------> X
//    opens java.base/java.lang.reflect=com.jfoenix
//    opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED ------> X
//    opens javafx.graphics/javafx.stage=ALL-UNNAMED ------> X
//    opens javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED ------> X

}
