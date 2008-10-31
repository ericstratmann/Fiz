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

    public void test_getDataManager_clearCaches() {
        // First attempt: loads cache and succeeds.
        DataManager.destroyAll();
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "m1:\n" +
                "  class: org.fiz.DataManagerFixture\n" +
                "m2:\n" +
                "  class: org.fiz.DataManagerFixture\n"));
        DataManager.getDataManager("m1");
        DataManager.getDataManager("m2");
        assertEquals("# cached managers before clearCache", 2,
                DataManager.cache.size());
        DataManager.clearCaches();
        assertEquals("# cached managers after clearCache", 0,
                DataManager.cache.size());
        assertEquals("log from DataManagerFixture",
                "m1 clearCache; m2 clearCache",
                DataManagerFixture.getLogs());
    }

    public void test_getDataManager_basics() {
        // First attempt: loads cache and succeeds.
        DataManager.destroyAll();
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

        // Second attempt: finds value in cache.  Erase the configuration
        // dataset so the attempt will fail if it tries to reinstantiate
        // the data manager.
        Config.setDataset("dataManagers", new Dataset());
        manager = (DataManagerFixture)
                DataManager.getDataManager("testManager");
        assertEquals("argument to constructor", "923",
                manager.constructorArgs.get("value2"));
    }
    public void test_getDataManager_defaultConfigurationInfo() {
        DataManager.destroyAll();
        Config.setDataset("main", YamlDataset.newStringInstance(
                "searchPackages: org.fiz\n"));
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "bogus:\n" +
                "  value1: 478\n"));
        DataManager manager = DataManager.getDataManager("raw");
        assertEquals("class of data manager", "RawDataManager",
                manager.getClass().getSimpleName());
    }
    public void test_getDataManager_defaultClass() {
        DataManager.destroyAll();
        Config.setDataset("main", YamlDataset.newStringInstance(
                "searchPackages: org.fiz\n"));
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "raw:\n" +
                "  value1: 478\n"));
        DataManager manager = DataManager.getDataManager("raw");
        assertEquals("class of data manager", "RawDataManager",
                manager.getClass().getSimpleName());
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
