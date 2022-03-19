module bootstrap {
	requires org.update4j;
	requires java.logging;

	provides org.update4j.service.Delegate with com.tgl.scan.bootstrap.ScanDelegate;
//	provides java.lang.System.LoggerFinder with com.tgl.scan.bootstrap.CustomLoggerFinder;

	exports com.tgl.scan.bootstrap;

    opens com.tgl.scan.bootstrap to org.update4j;

    uses org.update4j.service.Delegate;
}
