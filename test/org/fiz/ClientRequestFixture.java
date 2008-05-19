package org.fiz;
import org.apache.log4j.*;

/**
 * This class provides a dummy implementation of the ClientRequest class
 * for use in tests.
 */
public class ClientRequestFixture extends ClientRequest {
    public ClientRequestFixture() {
        super(new ServletFixture(new ServletConfigFixture(
                new ServletContextFixture())),
                new ServletRequestFixture(),
                new ServletResponseFixture());
        Config.init("test/testData/WEB-INF/config", "web/WEB-INF/config");
        Css.init("web/WEB-INF/css");
        DataManager.destroyAll();
        DataManagerFixture.clearLogs();
        DataManager.logger.setLevel(Level.ERROR);

        // Modify the Html object so it will find Javascript files.
        getHtml().jsDirectory = "javascript/";

        // Provide some initial query values in the main dataset.
        Dataset main = getMainDataset();
        main.set("name", "Alice");
        main.set("age", "36");
        main.set("height", "66");
        main.set("state", "California");
    }
}
