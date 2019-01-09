/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import com.dynatrace.openkit.DynatraceOpenKitBuilder;
import com.dynatrace.openkit.api.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * The SimpleSample includes a basic example that provides an overview of the features supported by OpenKit.
 * For more detailed information, please refer to the documentation that is available on GitHub.
 *
 * Warning: For simplicity no exception handling is performed in this example!
 */
public class SimpleSample {

    public static void main(String[] args) throws Exception {

        final String endpointURL = "";      // the endpointURL can be found in the Dynatrace UI
        final String applicationID = "";    // the application id can be found in the Dynatrace UI
        final long deviceID = 42L;          // an ID that uniquely identifies the device

        // create an OpenKit instance
        OpenKit openKit = new DynatraceOpenKitBuilder(endpointURL, applicationID, deviceID)
            .withApplicationName("SimpleSampleApp")
            .withApplicationVersion("1.0")
            .withOperatingSystem(System.getProperty("os.name"))
            .build();

        // we wait for OpenKit to be initialized
        // if you skip the line, OpenKit will be initialize asynchronously
        openKit.waitForInitCompletion();

        // create a new session
        Session session = openKit.createSession("127.0.0.1");

        // identify the user
        session.identifyUser("openKitExampleUser");

        // create a root action
        RootAction rootAction = session.enterAction("talk to postman");

        // execute a GET request to the postman echo API and trace request time and size
        executeAndTraceWebRequest(rootAction, "https://postman-echo.com/get?query=users", null);

        // wait a bit
        Thread.sleep(1000);

        // execute a POST request to the postman echo API and trace request time and size
        executeAndTraceWebRequest(rootAction, "https://postman-echo.com/post",
            "This is content that we want to be processed by the server");

        // report a value representing the desired sleep time
        rootAction.reportValue("sleepTime", 2000);

        // create a child action
        Action childAction = rootAction.enterAction("sleeping");

        // wait again
        Thread.sleep(2000);

        childAction.reportEvent("I have a nightmare!");

        // report an event indicating that we finished sleeping
        rootAction.reportEvent("finished sleeping");

        // leave both actions
        childAction.leaveAction();
        rootAction.leaveAction();

        // end the session
        session.end();

        // shutdown OpenKit
        openKit.shutdown();
    }

    /**
     * Performs a web request and traces the execution time using a {@code WebRequestTracer}. The result is reported
     * as a child of the provided {@code Action}.
     *
     * If the payload is null or empty a GET request is performed. Otherwise, a POST request is performed
     *
     * @param action the parent action for the web request tracer
     * @param endpoint the entpoint
     * @param payload the payload
     * @throws Exception
     */
    private static void executeAndTraceWebRequest(Action action, String endpoint, String payload) throws Exception {
        // prepare web request
        URL url = new URL(endpoint);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // write output if available
        if (payload != null && !payload.isEmpty()) {
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(payload);
            wr.flush();
        }

        // get the tracer
        WebRequestTracer tracer = action.traceWebRequest(urlConnection);

        // start timing for web request
        tracer.start();

        // process the result and store the bytes received
        int bytesReceived = processRequestResult(urlConnection);

        // set bytesSent, bytesReceived and response code
        tracer.setBytesSent(payload != null ? payload.getBytes().length : 0)    // we assume default encoding here
              .setBytesReceived(bytesReceived)                                  // bytes processed
              .setResponseCode(urlConnection.getResponseCode());

        // stop the tracer
        tracer.stop();
    }

    /**
     * Helper method that reads the response and returns the number of bytes read
     *
     * @param urlConnection
     * @return returns the number of bytes read
     * @throws IOException
     */
    private static int processRequestResult(URLConnection urlConnection) throws IOException {
        byte[] buffer = new byte[4096];
        int numBytes, totalBytes = 0;
        while ( (numBytes = urlConnection.getInputStream().read(buffer)) > 0 ) {
            totalBytes += numBytes;
            // we should do something meaningful with the read bytes ...
        }
        return totalBytes;
    }
}