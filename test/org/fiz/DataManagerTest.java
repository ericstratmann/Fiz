package org.fiz;
import java.util.*;

/**
 * Junit tests for the DataManager class.
 */

public class DataManagerTest extends junit.framework.TestCase {
    public void test_toString() {
        DataManagerFixture.init();
        DataManagerFixture manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        manager.name = "xyzzy";
        assertEquals("non-null name", "xyzzy data manager",
                manager.toString());
        manager.name = null;
        assertEquals("name is null", "unnamed data manager",
                manager.toString());
    }

    public void test_getDataManager() {
        // First attempt: fails because the desired data manager isn't
        // listed in the configuration dataset.
        DataManager.destroyAll();
        Config.setDataset("dataManagers", new Dataset());
        boolean gotException = false;
        try {
            DataManager.getDataManager("testManager");
        }
        catch (Error e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"testManager\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);

        // Second attempt: loads cache and succeeds.
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "testManager:\n" +
                "  class: org.fiz.DataManagerFixture\n" +
                "  value1: 478\n" +
                "  value2: 923\n"));
        DataManagerFixture manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        assertEquals("argument to constructor", "478",
                manager.constructorArgs.get("value1"));
        assertEquals("count of cached data managers", 1,
                DataManager.cache.size());

        // Third attempt: finds value in cache.  Erase the configuration
        // dataset so the attempt will fail if it tries to reinstantiate
        // the data manager.
        Config.setDataset("dataManagers", new Dataset());
        manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        assertEquals("argument to constructor", "923",
                manager.constructorArgs.get("value2"));
    }
    public void test_getDataManager_setName() {
        DataManagerFixture.init();
        DataManagerFixture manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        assertEquals("data manager name", "testManager", manager.name);
    }

    public void test_destroyAll() {
        DataManagerFixture.init();
        DataManagerFixture manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        assertEquals("destroyed field before destroying", false,
                manager.destroyed);
        DataManager.destroyAll();
        assertEquals("destroyed field after destroying", true,
                manager.destroyed);
        assertEquals("count of cached data managers", 0,
                DataManager.cache.size());
    }
}
