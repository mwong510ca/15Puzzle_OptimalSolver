package mwong.myprojects.fifteenpuzzle.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import mwong.myprojects.fifteenpuzzle.FileProperties;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceConstants;

/**
 * PropertiesFileCreator save a copy of server properties from ReferenceServerProperties
 * to resources/remote.properties file for client application connection.
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see ReferenceServerProperties
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class PropertiesFileCreator {
  /** private constructor, no instance. */
  private PropertiesFileCreator() {
    // Not called
  }

  /**
   * Main function start the remote server.
   *
   * @param args main function standard argument
   * @see ReferenceServerProperties
   */
  public static void main(final String[] args) {
    try {
      Properties properties = new Properties();
      properties.setProperty(ReferenceConstants.getRemoteServiceFieldName(),
          ReferenceServerProperties.getRemoteServiceName());
      properties.setProperty(ReferenceConstants.getRemoteHostFieldName(),
          ReferenceServerProperties.getRemoteHost());
      properties.setProperty(ReferenceConstants.getRemotePortFieldName(),
          Integer.toString(ReferenceServerProperties.getRemotePort()));

      File file = new File(FileProperties.getRemotePropertyFile());
      FileOutputStream fileOut = new FileOutputStream(file);
      properties.store(fileOut, "Reference connection settings");
      fileOut.close();
      System.out.println("Generate file success: " + FileProperties.getRemotePropertyFile());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
