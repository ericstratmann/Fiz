package org.fiz;

import java.util.*;

/**
 * Junit tests for the RawDataManager class.
 */

public class RawDataManagerTest extends junit.framework.TestCase {

    protected RawDataManager manager;

    public void setUp() {
        Config.setDataset("main", new Dataset());
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "raw:\n" +
                "  class: org.fiz.RawDataManager\n"));
        manager = new RawDataManager(new Dataset());
    }

    public void test_startRequests_basics() {
        DataRequest request1 = new DataRequest(YamlDataset.newStringInstance(
                "manager: raw\n" +
                "result:\n" +
                "    first: 16\n" +
                "    second: '99'\n"));
        DataRequest request2 = new DataRequest(YamlDataset.newStringInstance(
                "manager: raw\n" +
                "result:\n" +
                "    name: Alice\n"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("response from first request", "first:  16\n" +
                "second: 99\n",
                request1.getResponseData().toString());
        assertEquals("response from second request", "name: Alice\n",
                request2.getResponseData().toString());
    }
    public void test_startRequests_missingResultElement() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "raw"));
        request.start();
        assertEquals("error data", "culprit: result\n" +
                "message: no \"result\" argument provided in request\n",
                request.getErrorData()[0].toString());
    }

    public void test_setRequest() {
        Dataset properties = new Dataset("a", "1", "b", "2", "c", "3");
        Dataset modified =  RawDataManager.setRequest(properties, "b",
                new Dataset("x", "100", "y", "200"));
        assertEquals("compounded dataset", "Component #0:\n" +
                "  b:\n" +
                "      manager: raw\n" +
                "      result:\n" +
                "          x: 100\n" +
                "          y: 200\n" +
                "Component #1:\n" +
                "  a: 1\n" +
                "  b: 2\n" +
                "  c: 3\n",
                modified.toString());
        assertEquals("request arguments", "manager: raw\n" +
                "result:\n" +
                "    x: 100\n" +
                "    y: 200\n",
                modified.getChild("b").toString());
        assertEquals("property from original dataset", "3",
                modified.get("c"));
    }
}
