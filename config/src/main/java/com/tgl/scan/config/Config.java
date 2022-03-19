package com.tgl.scan.config;

import static java.lang.System.Logger.Level.INFO;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.OS;


public class Config {
	private static final System.Logger logger = System.getLogger(Config.class.getSimpleName());

	private static final String SCAN_MAVEN_URL = "com.tgl.scan.maven.url";
    private static final String PROP_SCAN_MAVEN_URL = System.getProperty(SCAN_MAVEN_URL);
    private static final String VAR_SCAN_MAVEN_URL = "${" + SCAN_MAVEN_URL + "}";
    private static final String VAR_SCAN_APP_URL = "${com.tgl.scan.app.url}";
    private static final String VAR_SCAN_CONFIG_URL = "${com.tgl.scan.config.url}";
    private static final String FILENAME_SETUP_XML = "updater-starter.xml";
    private static final String FILENAME_CONFIG_XML = "updater-scanapp.xml";
    private static final String FILENAME_RELEASE_NOTES_TXT = "ReleaseNotes.txt";
    private static final String FILENAME_SERVERS_XML = "servers.xml";

    private static final String TARGET_FXCACHE_DIR = System.getProperty("fxcache.location");
    private static final String TARGET_LIB_DIR = System.getProperty("lib.location");
    public static final String LIB_DIR = "lib";
    public static final String CONFIG_DIR = "config";
    private static final String REVISION_STARTER_MAIN = System.getProperty("scan-starter.revision");
    private static final String REVISION_SCAN_MAIN = System.getProperty("scan-main.revision");
    private static final String VERSION_ASPRISE_SCAN = System.getProperty("asprise-scan.version");
    //private static final String VERSION_UPDATE4J = System.getProperty("update4j.version");
    private static final String VERSION_JFOENIX = System.getProperty("jfoenix.version");
    private static final String VERSION_HTTP_CORE = System.getProperty("httpcore.version");
    private static final String VERSION_HTTP_CLIENT = System.getProperty("httpclient.version");
    private static final String VERSION_HTTP_MIME = System.getProperty("httpmime.version");
    private static final String VERSION_COMMONS_LOGGING = System.getProperty("commons-logging.version");
    private static final String VERSION_COMMONS_CODEC = System.getProperty("commons-codec.version");
    private static final String VERSION_JSOUP = System.getProperty("jsoup.version");
    private static final String VERSION_JAXB_API = System.getProperty("jakarta.xml.bind-api.version");
    private static final String VERSION_ACTIVATION = System.getProperty("jakarta.activation.version");
    private static final String VERSION_JAXB_RUNTIME = System.getProperty("jaxb-runtime.version");
    private static final String VERSION_JAXB_CORE = System.getProperty("jaxb-core.version");
    private static final String VERSION_TXW2 = System.getProperty("txw2.version");
    private static final String VERSION_ISTACK_COMMONS = System.getProperty("istack-commons.version");
    private static final String VERSION_LOG4J_API = System.getProperty("log4j-api.version");
    private static final String VERSION_LOG4J_CORE = System.getProperty("log4j-core.version");
    private static final String VERSION_JNA = System.getProperty("jna.version");
    private static final String VERSION_JNA_PLATFORM = System.getProperty("jna-platform.version");

    public static void main(String[] args) throws IOException {
    	showContext();
        cacheJavafx();
    	generateConfigXml();
        generateSetupXml();
    }

	private static void showContext() {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            logger.log(INFO, "System property " + entry.getKey() + "=" + entry.getValue());
        }
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
        	logger.log(INFO, "System environment " + entry.getKey() + "=" + entry.getValue());
        }
	}

	private static void generateConfigXml() throws IOException {
        Path configPath = Paths.get(TARGET_LIB_DIR);

    	logger.log(INFO, "configPath=" + configPath.toString());

        if (!Files.isDirectory(configPath)) {
        	logger.log(INFO, "Create dir: " + configPath.toString());
            Files.createDirectory(configPath);
        }

        Configuration config = Configuration.builder()
            .baseUri(VAR_SCAN_MAVEN_URL + "/")
            .basePath("${user.dir}")
            .launcher("com.tgl.scan.main.AppLauncher")
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jfoenix-" + VERSION_JFOENIX + ".jar")
                .uri(mavenUrl("com.jfoenix", "jfoenix", VERSION_JFOENIX))
                .path(LIB_DIR + "/jfoenix-" + VERSION_JFOENIX + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/httpclient-" + VERSION_HTTP_CLIENT + ".jar")
                .uri(mavenUrl("org.apache.httpcomponents", "httpclient", VERSION_HTTP_CLIENT))
                .path(LIB_DIR + "/httpclient-" + VERSION_HTTP_CLIENT + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/httpcore-" + VERSION_HTTP_CORE + ".jar")
                .uri(mavenUrl("org.apache.httpcomponents", "httpcore", VERSION_HTTP_CORE))
                .path(LIB_DIR + "/httpcore-" + VERSION_HTTP_CORE + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/commons-logging-" + VERSION_COMMONS_LOGGING + ".jar")
                .uri(mavenUrl("commons-logging", "commons-logging", VERSION_COMMONS_LOGGING))
                .path(LIB_DIR + "/commons-logging-" + VERSION_COMMONS_LOGGING + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/commons-codec-" + VERSION_COMMONS_CODEC + ".jar")
                .uri(mavenUrl("commons-codec", "commons-codec", VERSION_COMMONS_CODEC))
                .path(LIB_DIR + "/commons-codec-" + VERSION_COMMONS_CODEC + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/httpmime-" + VERSION_HTTP_MIME + ".jar")
                .uri(mavenUrl("org.apache.httpcomponents", "httpmime", VERSION_HTTP_MIME))
                .path(LIB_DIR + "/httpmime-" + VERSION_HTTP_MIME + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jsoup-" + VERSION_JSOUP + ".jar")
                .uri(mavenUrl("org.jsoup", "jsoup", VERSION_JSOUP))
                .path(LIB_DIR + "/jsoup-" + VERSION_JSOUP + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jakarta.xml.bind-api-" + VERSION_JAXB_API + ".jar")
                .uri(mavenUrl("jakarta.xml.bind", "jakarta.xml.bind-api", VERSION_JAXB_API))
                .path(LIB_DIR + "/jakarta.xml.bind-api-" + VERSION_JAXB_API + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jakarta.activation-" + VERSION_ACTIVATION + ".jar")
		        .uri(mavenUrl("com.sun.activation", "jakarta.activation", VERSION_ACTIVATION))
		        .path(LIB_DIR + "/jakarta.activation-" + VERSION_ACTIVATION + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jaxb-runtime-" + VERSION_JAXB_RUNTIME + ".jar")
                .uri(mavenUrl("org.glassfish.jaxb", "jaxb-runtime", VERSION_JAXB_RUNTIME))
                .path(LIB_DIR + "/jaxb-runtime-" + VERSION_JAXB_RUNTIME + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jaxb-core-" + VERSION_JAXB_CORE + ".jar")
                .uri(mavenUrl("org.glassfish.jaxb", "jaxb-core", VERSION_JAXB_CORE))
                .path(LIB_DIR + "/jaxb-core-" + VERSION_JAXB_CORE + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/txw2-" + VERSION_TXW2 + ".jar")
                .uri(mavenUrl("org.glassfish.jaxb", "txw2", VERSION_TXW2))
                .path(LIB_DIR + "/txw2-" + VERSION_TXW2 + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/istack-commons-runtime-" + VERSION_ISTACK_COMMONS + ".jar")
                .uri(mavenUrl("com.sun.istack", "istack-commons-runtime", VERSION_ISTACK_COMMONS))
                .path(LIB_DIR + "/istack-commons-runtime-" + VERSION_ISTACK_COMMONS + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/log4j-core-" + VERSION_LOG4J_API + ".jar")
                .uri(mavenUrl("org.apache.logging.log4j", "log4j-core", VERSION_LOG4J_API))
                .path(LIB_DIR + "/log4j-core-" + VERSION_LOG4J_API + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/log4j-api-" + VERSION_LOG4J_CORE + ".jar")
                .uri(mavenUrl("org.apache.logging.log4j", "log4j-api", VERSION_LOG4J_CORE))
                .path(LIB_DIR + "/log4j-api-" + VERSION_LOG4J_CORE + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jna-" + VERSION_JNA_PLATFORM + ".jar")
                .uri(mavenUrl("net.java.dev.jna", "jna", VERSION_JNA_PLATFORM))
                .path(LIB_DIR + "/jna-" + VERSION_JNA_PLATFORM + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/jna-platform-" + VERSION_JNA_PLATFORM + ".jar")
                .uri(mavenUrl("net.java.dev.jna", "jna-platform", VERSION_JNA_PLATFORM))
                .path(LIB_DIR + "/jna-platform-" + VERSION_JNA_PLATFORM + ".jar")
                .classpath())
//            // Maven Repository 設定為 https://repo1.maven.org/maven2 時，則設定如下
//            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/asprise-scan-" + VERSION_ASPRISE_SCAN + ".jar")
//                .uri(VAR_SCAN_APP_URL + "/asprise-scan-" + VERSION_ASPRISE_SCAN + ".jar")
//                .path(LIB_DIR + "/asprise-scan-" + VERSION_ASPRISE_SCAN + ".jar")
//                .classpath())
            // Maven Repository 設定為 http://10.67.67.111:8081/artifactory/ebao-maven-repo 時，則設定如下
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/asprise-scan-" + VERSION_ASPRISE_SCAN + ".jar")
                .uri(mavenUrl("asprise-scan", "asprise-scan", VERSION_ASPRISE_SCAN))
                .path(LIB_DIR + "/asprise-scan-" + VERSION_ASPRISE_SCAN + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/tgl-scan-main-" + REVISION_SCAN_MAIN + ".jar")
                .uri(appUrl("com.tgl.scan", "tgl-scan-main", REVISION_SCAN_MAIN))
                .path(LIB_DIR + "/tgl-scan-main-" + REVISION_SCAN_MAIN + ".jar")
                .classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/" + FILENAME_SERVERS_XML)
                .uri(VAR_SCAN_CONFIG_URL + "/" + FILENAME_SERVERS_XML)
                .path(CONFIG_DIR + "/" + FILENAME_SERVERS_XML))
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/" + FILENAME_RELEASE_NOTES_TXT)
                .uri(VAR_SCAN_CONFIG_URL + "/" + FILENAME_RELEASE_NOTES_TXT)
                .path(FILENAME_RELEASE_NOTES_TXT))
            .build();

        try ( Writer out = Files.newBufferedWriter(Paths.get(TARGET_LIB_DIR + "/" + FILENAME_CONFIG_XML)) ) {
            config.write(out);
        }
    }

	private static void generateSetupXml() throws IOException {
        String fxCacheDir = TARGET_FXCACHE_DIR;
        logger.log(INFO, "generateSetupXml(), fxCacheDir="+fxCacheDir);

        Configuration config = Configuration.builder()
            .property("default.launcher.main.class", "org.update4j.Bootstrap")
            .baseUri(VAR_SCAN_MAVEN_URL + "/org/openjfx/")
            .basePath("${user.dir}")
            .files(FileMetadata.streamDirectory(fxCacheDir)
                .filter(fm -> fm.getSource().getFileName().toString().startsWith("javafx"))
                .peek(f -> f.classpath())
                .peek(f -> f.ignoreBootConflict()) // if run with JDK 9/10
                .peek(f -> f.osFromFilename())
                .peek(f -> f.path(LIB_DIR + "/" + f.getSource().getFileName().toString()))
                .peek(f -> f.uri(getJavafxURI(f.getSource(), f.getOs()))))
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/tgl-scan-starter-" + REVISION_STARTER_MAIN + ".jar")
                .uri(appUrl("com.tgl.scan", "tgl-scan-starter", REVISION_STARTER_MAIN))
                .path(LIB_DIR + "/tgl-scan-starter-" + REVISION_STARTER_MAIN + ".jar")
        		.classpath())
            .file(FileMetadata.readFrom(TARGET_LIB_DIR + "/" + FILENAME_CONFIG_XML) // fall back if no internet
                .uri(VAR_SCAN_CONFIG_URL + "/" + FILENAME_CONFIG_XML)
                .path(CONFIG_DIR + "/" + FILENAME_CONFIG_XML))
            .build();

        try (Writer out = Files.newBufferedWriter(Paths.get(TARGET_LIB_DIR + "/" + FILENAME_SETUP_XML))) {
            config.write(out);
        }
    }

    private static String mavenUrl(String groupId, String artifactId, String version) {
        return mavenUrl(false, groupId, artifactId, version, null);
    }

    private static String mavenUrl(boolean cacheJFX, String groupId, String artifactId, String version, OS os) {
        StringBuilder builder = new StringBuilder();
        builder.append((cacheJFX ? PROP_SCAN_MAVEN_URL : VAR_SCAN_MAVEN_URL) + '/');
        builder.append(groupId.replace('.', '/') + "/");
//        builder.append(artifactId.replace('.', '-') + "/");
//        builder.append(version + "/");
//        builder.append(artifactId.replace('.', '-') + "-" + version);
        // jakarta-activation-api、jakarta-xml-bind-api、jakarta-activation 例外處理
        String newArtifactId = artifactId.startsWith("jakarta.") ? artifactId : artifactId.replace('.', '-');
        builder.append(newArtifactId + "/");
        builder.append(version + "/");
        builder.append(newArtifactId + "-" + version);

        if (os != null) {
            builder.append('-' + os.getShortName());
        }

        builder.append(".jar");

        return builder.toString();
    }

    private static String appUrl(String groupId, String artifactId, String version) {
        return appUrl(groupId, artifactId, version, null);
    }

    private static String appUrl(String groupId, String artifactId, String version, OS os) {
        StringBuilder builder = new StringBuilder();
        builder.append(VAR_SCAN_APP_URL + '/');
        builder.append(groupId.replace('.', '/') + "/");
        String newArtifactId = artifactId.replace('.', '-');
        builder.append(newArtifactId + "/");
        builder.append(version + "/");
        builder.append(newArtifactId + "-" + version);

        if (os != null) {
            builder.append('-' + os.getShortName());
        }

        builder.append(".jar");

        return builder.toString();
    }

    private static String extractJavafxURL(Path path, OS os) {
        Pattern regex = Pattern.compile("javafx-([a-z]+)-([0-9.]+)(?:-(win|mac|linux))?\\.jar");
        Matcher match = regex.matcher(path.getFileName().toString());

        if (!match.find())
            return null;

        String module = match.group(1);
        String version = match.group(2);
        if (os == null && match.groupCount() > 2) {
            os = OS.fromShortName(match.group(3));
        }

        return mavenUrl(true, "org.openjfx", "javafx." + module, version, os);
    }

    private static String getJavafxURI(Path path, OS os) {
        Pattern regex = Pattern.compile("javafx-([a-z]+)-([0-9.]+)(?:-(win|mac|linux))?\\.jar");
        Matcher match = regex.matcher(path.getFileName().toString());

        if (!match.find())
            return null;

        String module = match.group(1);
        String version = match.group(2);
        if (os == null && match.groupCount() > 2) {
            os = OS.fromShortName(match.group(3));
        }

        String artifactId = "javafx-" + module;

        StringBuilder builder = new StringBuilder();
        builder.append(VAR_SCAN_MAVEN_URL + "/");
        builder.append("org/openjfx" + "/");
        builder.append(artifactId + "/");
        builder.append(version + "/");
        builder.append(artifactId + "-" + version);

        if (os != null) {
            builder.append('-' + os.getShortName());
        }

        builder.append(".jar");

        return builder.toString();
    }

    private static String injectOs(String file, OS os) {
        return file.replaceAll("(.+)\\.jar", "$1-" + os.getShortName() + ".jar");
    }

    private static void cacheJavafx() throws IOException {
        Path fxCacheDir = Paths.get(TARGET_FXCACHE_DIR);
        logger.log(INFO, "cacheJavafx(), fxCacheDir="+fxCacheDir.toString());

        try (
    		Stream<Path> files = Files.list(Paths.get(TARGET_LIB_DIR))
    			.filter( path -> ( path.toFile().getName().startsWith("javafx") ) )
		) {
            logger.log(INFO, "List javafx*.*");
            files.forEach(f -> {
                logger.log(INFO, "File name: "+f.getFileName().toString());
            	try {
                	if (!Files.isDirectory(fxCacheDir)) {
                        logger.log(INFO, "Create Dir: "+fxCacheDir);
                        Files.createDirectory(fxCacheDir);
                	}
                    for (OS os : EnumSet.of(OS.WINDOWS, OS.MAC, OS.LINUX)) {
                        Path file = fxCacheDir.resolve(injectOs(f.getFileName().toString(), os));
                        logger.log(INFO, "File: "+file.getFileName().toString());
                        if (Files.notExists(file)) {
                            String download = extractJavafxURL(f, os);
                            logger.log(INFO, "Download URI: "+download);
                            URI uri = URI.create(download);
                            try (InputStream in = uri.toURL().openStream()) {
                                logger.log(INFO, "Copy to "+uri.toString());
                                Files.copy(in, file);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
