package com.tgl.scan.bootstrap;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;

import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.UpdateContext;
import org.update4j.UpdateOptions;
import org.update4j.inject.InjectSource;
import org.update4j.service.Delegate;
import org.update4j.service.UpdateHandler;

public class ScanDelegate implements Delegate {

	private static final System.Logger logger = System.getLogger(ScanDelegate.class.getName());

    private static final String ZIP_LOCATION = "update.zip";
    private static final String ATTR_CONFIG_UPDATE_STATUS = "com.tgl.config.updatestatus";
    private static final String ATTR_CONFIG_CONN_TIMEOUT = "com.tgl.config.conn.timeout";
    private static final String FILENAME_CONFIG_XML = "updater-scanapp.xml";

    private String remote;
    private String local;
    private String cert;

    private PublicKey pk = null;

    @InjectSource(target = "args")
    private List<String> businessArgs;

    @Override
    public long version() {
        return Long.MIN_VALUE;
    }

    public String getRemote() {
        return remote;
    }

    public String getLocal() {
        return local;
    }

    public String getCert() {
        return cert;
    }

    public PublicKey getPublicKey() {
        return pk;
    }

    public List<String> getBusinessArgs() {
        return businessArgs;
    }

    @Override
    public void main(List<String> args) throws Throwable {
        logger.log(INFO, "args="+(args==null ? "" : (String.join(",", args))));

        parseArgs(ArgUtils.beforeSeparator(args));

        if (remote == null || local == null) {
            throw new IllegalArgumentException("--remote and --local must be supplied.");
        }
        if (cert != null) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream in = Files.newInputStream(Paths.get(cert))) {
                pk = cf.generateCertificate(in).getPublicKey();
            }
        }

        businessArgs = ArgUtils.afterSeparator(args);

        updateAndLaunch();
    }

    protected void parseArgs(List<String> bootArgs) {
        logger.log(INFO, "bootArgs="+(bootArgs==null ? "" : (String.join(",", bootArgs))));

        Map<String, String> parsed = ArgUtils.parseArgs(bootArgs);
        for (Map.Entry<String, String> entry : parsed.entrySet()) {
            String arg = entry.getKey();

            if ("remote".equals(arg)) {
            	ArgUtils.validateHasValue(entry);
                remote = entry.getValue();
                logger.log(INFO, "remote=" + remote);
            } else if ("local".equals(arg)) {
            	ArgUtils.validateHasValue(entry);
                local = entry.getValue();
                logger.log(INFO, "local=" + local);
            } else if ("cert".equals(arg)) {
            	ArgUtils.validateHasValue(entry);
                cert = entry.getValue();
                logger.log(INFO, "cert=" + cert);
            } else if (arg.startsWith("com.tgl")) { 
            	String value = entry.getValue();
                System.setProperty(arg, value);
                logger.log(INFO, arg + "=" + value);
            } else {
                throw new IllegalArgumentException("Unknown option \"" + arg + "\". Separate ScanApp arguments with '--'.");
            }
        }
    }

    protected void updateAndLaunch() throws Throwable {
        logger.log(INFO, "Start to update and launch...");

        Configuration remoteConfig = null;
        Configuration localConfig = null;

        if (remote != null) {
            remoteConfig = getRemoteConfig();
        }

        if (local != null) {
            localConfig = getLocalConfig(remoteConfig != null);
        }

        if (remoteConfig == null && localConfig == null) {
            logger.log(ERROR, "Failed to load remote and local config! Can not launch starter.");
            return;
        }

        if (remoteConfig != null) {
            Path zip = Paths.get(ZIP_LOCATION);

            logger.log(INFO, "zip=" + ZIP_LOCATION);

            UpdateOptions.ArchiveUpdateOptions options = UpdateOptions.archive(zip).publicKey(pk);
            options.updateHandler(new MyUpdateHandler());

            Throwable exception = remoteConfig.update(options).getException();
            if (exception == null) {
                logger.log(INFO, "Update starter success!");
            } else {
                logger.log(ERROR, exception);
                return;
            }

            if (Files.exists(zip)) {
            	logger.log(INFO, "Read zip.");
            	Archive archive = Archive.read(zip);
            	backupScanConfig(archive);
            	archive.install();
                logger.log(INFO, "Installed.");
            }
            syncLocalConfig(remoteConfig);
            if (localConfig != null) {
            	logger.log(INFO, "Delete old files.");
                remoteConfig.deleteOldFiles(localConfig);
            }

            logger.log(INFO, "Starter updated! Launch now.");

            System.setProperty(ATTR_CONFIG_UPDATE_STATUS, "StarterUpdated");
            remoteConfig.launch(this);
        } else {
        	if (localConfig.requiresUpdate()) {
                logger.log(INFO, "zip=" + ZIP_LOCATION);
                Path zip = Paths.get(ZIP_LOCATION);
                UpdateOptions.ArchiveUpdateOptions options = UpdateOptions.archive(zip).publicKey(pk);
                options.updateHandler(new MyUpdateHandler());

                Throwable exception = localConfig.update(options).getException();
                if (exception == null) {
                    logger.log(INFO, "Update starter success!");
                } else {
                    logger.log(ERROR, exception);
                    return;
                }

                if (Files.exists(zip)) {
                	logger.log(INFO, "Read zip.");
                    Archive.read(zip).install();
                    logger.log(INFO, "Installed.");
                }
        	}
        	
        	logger.log(INFO, "Launch starter.");

            System.setProperty(ATTR_CONFIG_UPDATE_STATUS, "StarterNotUpdate");
        	localConfig.launch(this);
        }
    }

    protected Reader openConnection(URL url) throws IOException {
    	int timeout = 5;
    	String timeoutStr = System.getProperty(ATTR_CONFIG_CONN_TIMEOUT);
    	try {
            timeout = Integer.parseInt(timeoutStr);
    	} catch(Exception e) {
    	}
    	logger.log(INFO, "url=" + url.toString() + ", timeout=" + timeout);

        URLConnection connection = url.openConnection();

        // Some downloads may fail with HTTP/403, this may solve it
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(timeout * 1000);
        connection.setReadTimeout(timeout * 1000);

        return new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    protected Configuration getLocalConfig(boolean ignoreFileNotFound) {
    	logger.log(INFO, "ignoreFileNotFound=" + ignoreFileNotFound + ", local=" + local);
        try (Reader in = Files.newBufferedReader(Paths.get(local))) {
            if (pk == null) {
                return Configuration.read(in);
            } else {
                return Configuration.read(in, pk);
            }
        } catch (NoSuchFileException e) {
            if (!ignoreFileNotFound) {
            	logger.log(ERROR, "Local config file not found!", e);
            }
        } catch (Exception e) {
        	logger.log(ERROR, "Failed to read local config!\n{0}", e.getMessage());
        }

        return null;
    }

    protected Configuration getRemoteConfig() {
    	logger.log(INFO, "remote=" + remote);
        try (Reader in = openConnection(new URL(remote))) {
            if (pk == null) {
                return Configuration.read(in);
            } else {
                return Configuration.read(in, pk);
            }
        } catch (Exception e) {
        	logger.log(ERROR, "Failed to load remote config!\n{0}", e.getMessage());
        }

        return null;
    }

    protected void syncLocalConfig(Configuration remoteConfig) {
    	logger.log(INFO, "Write remoteConfig to " + local);
        Path localPath = Paths.get(local);
        try {
            if (localPath.getParent() != null)
                Files.createDirectories(localPath.getParent());

            try (Writer out = Files.newBufferedWriter(localPath)) {
                remoteConfig.write(out);
            }
        } catch (IOException e) {
        	logger.log(ERROR, "Sync local config Error!\n{0}", e.getMessage());
        }
    }

    private void backupScanConfig(Archive archive) {
		Path configFile = null;
		List<FileMetadata> files = archive.getFiles();
		for (FileMetadata file : files) {
        	logger.log(INFO, "File url={0}, path={1}, fileName={2}", file.getUri().toString(), file.getPath().toString(), file.getPath().getFileName().toString());
			if (FILENAME_CONFIG_XML.equals(file.getPath().getFileName().toString())) {
				configFile = file.getPath();
				break;
			}
		}
		if (configFile!=null) {
			Path newFile = Paths.get(configFile.toString()+".old");
			try {
	        	logger.log(INFO, "Backup local config {0} to {1}", configFile.toString(), newFile.toString());
				Files.copy(configFile, newFile, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
			} catch (IOException e) {
	        	logger.log(ERROR, "Backup local config Error!\n{0}", e.getMessage());
			}
		}
    }

    private class MyUpdateHandler implements UpdateHandler {

		public MyUpdateHandler() {
			super();
		}
    	
		@Override
		public void init(UpdateContext context) throws Throwable {
			logger.log(INFO, "updateHandler.init()");
			UpdateHandler.super.init(context);
		}

		@Override
		public void startCheckUpdates() throws Throwable {
			logger.log(INFO, "updateHandler.startCheckUpdates()");
			UpdateHandler.super.startCheckUpdates();
		}

		@Override
		public boolean shouldCheckForUpdate(FileMetadata file) {
			boolean tmp = UpdateHandler.super.shouldCheckForUpdate(file);
			logger.log(INFO, "updateHandler.shouldCheckForUpdate(), file="+file.getPath().getFileName()+", result="+tmp);
			return tmp;
		}

		@Override
		public void startCheckUpdateFile(FileMetadata file) throws Throwable {
			logger.log(INFO, "updateHandler.startCheckUpdateFile(), file="+file.getPath().getFileName());
			UpdateHandler.super.startCheckUpdateFile(file);
		}

		@Override
		public void doneCheckUpdateFile(FileMetadata file, boolean requires) throws Throwable {
			logger.log(INFO, "updateHandler.doneCheckUpdateFile(), file="+file.getPath().getFileName() + ", requires="+requires);
			UpdateHandler.super.doneCheckUpdateFile(file, requires);
		}

		@Override
		public void updateCheckUpdatesProgress(float frac) throws Throwable {
			// TODO Auto-generated method stub
			UpdateHandler.super.updateCheckUpdatesProgress(frac);
		}

		@Override
		public void doneCheckUpdates() throws Throwable {
			logger.log(INFO, "updateHandler.doneCheckUpdates()");
			UpdateHandler.super.doneCheckUpdates();
		}

		@Override
		public void startDownloads() throws Throwable {
			logger.log(INFO, "updateHandler.startDownloads()");
			UpdateHandler.super.startDownloads();
		}

		@Override
		public InputStream openDownloadStream(FileMetadata file) throws Throwable {
			logger.log(INFO, "updateHandler.openDownloadStream(), file="+file.getPath().getFileName().toString()+", uri="+file.getUri().toString());
			return UpdateHandler.super.openDownloadStream(file);
		}

		@Override
		public void startDownloadFile(FileMetadata file) throws Throwable {
			logger.log(INFO, "updateHandler.startDownloadFile(), file="+file.getPath().getFileName().toString()+", uri="+file.getUri().toString());
			UpdateHandler.super.startDownloadFile(file);
		}

		@Override
		public void updateDownloadFileProgress(FileMetadata file, float frac) throws Throwable {
			// TODO Auto-generated method stub
			UpdateHandler.super.updateDownloadFileProgress(file, frac);
		}

		@Override
		public void updateDownloadProgress(float frac) throws Throwable {
			// TODO Auto-generated method stub
			UpdateHandler.super.updateDownloadProgress(frac);
		}

		@Override
		public void validatingFile(FileMetadata file, Path path) throws Throwable {
			logger.log(INFO, "updateHandler.validatingFile(), file="+file.getPath().getFileName().toString()+", path="+path.toString());
			UpdateHandler.super.validatingFile(file, path);
		}

		@Override
		public void doneDownloadFile(FileMetadata file, Path path) throws Throwable {
			logger.log(INFO, "updateHandler.doneDownloadFile(), file="+file.getPath().getFileName().toString()+", path="+path.toString());
			UpdateHandler.super.doneDownloadFile(file, path);
		}

		@Override
		public void doneDownloads() throws Throwable {
			logger.log(INFO, "updateHandler.doneDownloads()");
			UpdateHandler.super.doneDownloads();
		}

		@Override
		public void failed(Throwable t) {
			logger.log(INFO, "updateHandler.failed(), Failed:"+t.getMessage());
			UpdateHandler.super.failed(t);
		}

		@Override
		public void succeeded() {
			logger.log(INFO, "updateHandler.succeeded()");
			UpdateHandler.super.succeeded();
		}

		@Override
		public void stop() {
			logger.log(INFO, "updateHandler.stop()");
			UpdateHandler.super.stop();
		}

		@Override
		public Object getResult() {
			// TODO Auto-generated method stub
			return UpdateHandler.super.getResult();
		}

    }

}
