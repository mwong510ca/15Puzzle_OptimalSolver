package mwong.myprojects.fifteenpuzzle.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ReferenceServerCreatePropertiesFile {
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			properties.setProperty("remoteServiceName", ReferenceServerProperties.getRemoteServiceName());
			properties.setProperty("remoteHost", ReferenceServerProperties.getRemoteHost());
			properties.setProperty("remotePort", Integer.toString(ReferenceServerProperties.getRemotePort()));

			File file = new File("resources/remote.properties");
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Reference connection settings");
			fileOut.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}