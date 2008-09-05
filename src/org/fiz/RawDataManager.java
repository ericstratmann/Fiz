package org.fiz;

import java.lang.reflect.*;
import java.util.*;

/**
 * RawDataManager is a DataManager that responds to each request with a
 * dataset contained in the request itself.  It allows precomputed data
 * to be used in situations that receive data via DataRequests.  It is
 * also used for testing.
 * <p>
 * RawDataManager does not use any information in the configuration
 * dataset for the data manager.  It supports the following arguments
 * in DataRequests:
 *   result:    (optional) A nested dataset, which will be returned as the
 *              result of the request.
 *   error:     (optional) One or more nested data sets, which will be
 *              returned as errors for the request.  If {@code result} is
 *              not specified then {@code error} must be specified; if
 *              {@code result} is specified then {@code error} is ignored.
 */
public class RawDataManager extends DataManager {
    /**
     * Construct a RawDataManager from a configuration dataset.
     * @param config               Ignored.
     */
    public RawDataManager(Dataset config) {
        // Nothing to do here.
    }

    /**
     * This method is invoked might DataRequest to initiate processing of
     * one or more requests for this data manager.  This method completes
     * each request by returning the data associated with it.
     * @param requests             DataRequest objects describing the
     *                             requests to be processed.
     */
    @Override
    public void startRequests(Collection<DataRequest> requests) {
        for (DataRequest request : requests) {
            Dataset result = request.getRequestData().checkChild("result");
            if (result != null) {
                request.setComplete(result);
            } else {
                ArrayList<Dataset> errors =
                        request.getRequestData().getChildren("error");
                if (errors.size() > 0) {
                    request.setError(errors);
                } else {
                    request.setError(new Dataset("message", "no \"result\" " +
                            "or \"error\" argument provided in request",
                            "culprit", "result"));
                }
            }
        }
    }

    /**
     * Arrange for a DataRequest in a property dataset to return a given
     * dataset as its result.  A typical use for this method would be for a
     * TableSection where the data to be displayed has already been computed.
     * Given a dataset {@code properties} containing configuration information
     * for that TableSection and another dataset {@code data} containing the
     * data to be displayed in the table, the following statement will create
     * a TableSection that will display {@code data}:
     * <pre>
     * TableSection table = new TableSection(
     *         rawDataManager.setRequest(properties, "request", data),
     *         column1Info, column2Info, ...);
     * </pre>
     * @param properties           Existing dataset, one of whose members
     *                             (indicated by {@code name}) is supposed
     *                             to specify a DataRequest.
     * @param name                 Name of an element in {@code properties}.
     *                             The element need not exist at the dataset
     *                             at the time this method is invoked.
     * @param result               Dataset that should be returned as the
     *                             result of the DataRequest specified by
     *                             the {@code name} property.
     * @return                     A dataset that is identical to
     *                             {@code properties} except that its
     *                             {@code name} property specifies a
     *                             DataRequest that will return {@code result}.
     *                             The result is a CompoundDataset containing
     *                             {@code properties} as one element; this
     *                             method does not modify {@code properties}.
     */
    public static Dataset setRequest(Dataset properties, String name,
            Dataset result) {
        Dataset requestArgs = new Dataset("manager", "raw");
        requestArgs.addChild("result", result);
        Dataset newProperties = new Dataset();
        newProperties.addChild(name, requestArgs);
        return new CompoundDataset(newProperties, properties);
    }
}
