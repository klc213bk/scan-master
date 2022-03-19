package com.tgl.scan.bootstrap;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

import java.io.BufferedReader;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.update4j.service.Delegate;
import org.update4j.service.Service;

public class ScanBootstrap {

	private static final System.Logger logger = System.getLogger(ScanBootstrap.class.getSimpleName());

    private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
    private static final String PROPS_FILE_NAME = "tgl-scan.properties";
    private static final String PROP_MAVEN_URL = "update.maven.url";
    private static final String PROP_SCAN_APP_URL = "update.scan.app.url";
    private static final String PROP_SCAN_CONFIG_URL = "update.scan.config.url";
    private static final String PROP_TIMEOUT = "update.connRead.timeout";
    private static final String ATTR_MAVEN_URL = "com.tgl.scan.maven.url";
    private static final String ATTR_SCAN_APP_URL = "com.tgl.scan.app.url";
    private static final String ATTR_SCAN_CONFIG_URL = "com.tgl.scan.config.url";
    private static final String ATTR_CONFIG_CONN_TIMEOUT = "com.tgl.config.conn.timeout";
	private static final String ATTR_APP_SCHEME_ID = "com.tgl.scan.app.scheme.id";
    private static final String FILENAME_SETUP_XML = "updater-starter.xml";
    private static final String DELEGATE_NAME = "com.tgl.scan.bootstrap.ScanDelegate";

    public ScanBootstrap() {
    }

    public static void main(String[] args) throws Throwable {
    	List<String> argsList = getArgumentList(args);
        Properties properties = loadProperties();
        List<String> propertyList = getPropertyList(properties);
        propertyList.addAll(argsList);
        start(propertyList);
    }

    public static List<String> getArgumentList(String[] args) {
        logger.log(INFO, "args="+(args==null ? "" : (String.join(",", args))));

    	List<String> argsList = new LinkedList<String>();
    	String tglScanUri = null;
    	for (String arg : args) {
    		String schemeId = System.getProperty(ATTR_APP_SCHEME_ID, "tgl-scan");
    		if (arg.startsWith(schemeId + ":///")) {
    			tglScanUri = arg;
    		} else {
    			argsList.add(arg);
    		}
    	}

    	if (tglScanUri!=null) {
        	java.net.URI uri = null;
        	try {
				uri = new java.net.URI(tglScanUri);
			} catch (URISyntaxException e) {
		        logger.log(ERROR, e);
			}
        	if (uri!=null) {
        		String scheme = uri.getScheme();
                String authority = uri.getAuthority();
                String path = uri.getPath();
                String query = uri.getQuery();
                String fragment = uri.getFragment();
                logger.log(INFO, "scheme="+scheme);
                logger.log(INFO, "authority="+authority);
                logger.log(INFO, "path="+path);
                logger.log(INFO, "query="+query);
                logger.log(INFO, "fragment="+fragment);
                if (query!=null) {
                	String[] parameters = query.split("&");
                	for (String parameter : parameters) {
                		argsList.add("--com.tgl.scan.parameter." + parameter);
                	}
                }
        	}
    	}
    	return argsList;
    }

    public static Properties loadProperties() throws Throwable {
		Properties properties = new Properties();
		Path propertiesPath = Paths.get(CONFIG_DIR + File.separator + PROPS_FILE_NAME);

        logger.log(INFO, "Load properties file " + propertiesPath.toString());

		try (BufferedReader reader = Files.newBufferedReader(propertiesPath, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}

		return properties;
	}

	private static List<String> getPropertyList(Properties properties) throws Throwable {
        String mavenUrl = properties.getProperty(PROP_MAVEN_URL, "").trim();
        if ( mavenUrl==null || mavenUrl.length()==0 ) {
            throw new IllegalArgumentException("Missing property [" + PROP_MAVEN_URL + "].");
        }

        String scanAppUrl = properties.getProperty(PROP_SCAN_APP_URL, "").trim();
        if ( scanAppUrl==null || scanAppUrl.length()==0 ) {
            throw new IllegalArgumentException("Missing property [" + PROP_SCAN_APP_URL + "].");
        }

        String scanConfigUrl = properties.getProperty(PROP_SCAN_CONFIG_URL, "").trim();
        if ( scanConfigUrl==null || scanConfigUrl.length()==0 ) {
            throw new IllegalArgumentException("Missing property [" + PROP_SCAN_CONFIG_URL + "].");
        }

        String timeoutStr = properties.getProperty(PROP_TIMEOUT, "").trim();
        int timeout = 5;
        try {
        	timeout = Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            logger.log(ERROR, "Property [" + PROP_TIMEOUT + "] must be numeric! Set to default[" + timeout + "].", e);
        }

        logger.log(INFO, "mavenUrl=" + mavenUrl);
        logger.log(INFO, "scanAppUrl=" + scanAppUrl);
        logger.log(INFO, "scanConfigUrl=" + scanConfigUrl);
        logger.log(INFO, "timeout=" + timeout);

        List<String> argsList = new LinkedList<String>();
        argsList.add("--local=config/" + FILENAME_SETUP_XML);
        argsList.add("--remote=" + scanConfigUrl + "/" + FILENAME_SETUP_XML);
        argsList.add("--" + ATTR_MAVEN_URL + "=" + mavenUrl);
        argsList.add("--" + ATTR_SCAN_APP_URL + "=" + scanAppUrl);
        argsList.add("--" + ATTR_SCAN_CONFIG_URL + "=" + scanConfigUrl);
        argsList.add("--" + ATTR_CONFIG_CONN_TIMEOUT + "=" + timeout);

        return argsList;
	}

    public static void start(List<String> args) throws Throwable {
        Delegate delegate = Service.loadService(Delegate.class, DELEGATE_NAME);
        delegate.main(args);
    }

}
