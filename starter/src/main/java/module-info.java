module starter {
	requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
	requires java.logging;
	requires transitive org.update4j;

	exports com.tgl.scan.starter;

	opens com.tgl.scan.starter;
    opens fonts.notosanstc;
    opens icons;
    opens imgs;

	provides org.update4j.service.Delegate with com.tgl.scan.starter.StarterDelegate;
}

//module starter {
//	requires javafx.base;
//    requires javafx.fxml;
//    requires javafx.controls;
//    requires javafx.graphics;
//    requires javafx.swing;
//	requires transitive org.update4j;
//
////	opens com.tgl.scan.starter to javafx.fxml;
//	opens com.tgl.scan.starter;
//    opens fonts.notosanstc;
//    opens icons;
//    opens imgs;
//
//	exports com.tgl.scan.starter;
//
//	provides org.update4j.service.Delegate with com.tgl.scan.starter.StarterDelegate;
//}
