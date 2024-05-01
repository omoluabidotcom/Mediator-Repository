/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.mirabilia.carpha;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.codec.binary.Base64;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.ExceptError;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import com.mirabilia.carpha.converter.DataProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DHISMappingAdapter extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final MediatorConfig config;
    private ActorRef requestHandler;
    private ActorRef respondTo;


    public DHISMappingAdapter(MediatorConfig config) {
        this.config = config;
    }

    private DataProcessor getProcessor() {
        return new DataProcessor(
                (Map<String, String>)config.getDynamicConfig().get("mappings-datasets"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-dataelements"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-orgunits"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-programs")
        );
    }

    private Map<String, String> copyHeaders(Map<String, String> headers) {
        Map<String, String> copy = new HashMap<>();
        copy.put("content-type", headers.get("content-type"));
        copy.put("authorization", headers.get("authorization"));
        copy.put("x-openhim-transactionid", headers.get("x-openhim-transactionid"));
        copy.put("x-forwarded-for", headers.get("x-forwarded-for"));
        copy.put("x-forwarded-host", headers.get("x-forwarded-host"));
        return copy;
    }

    private void forwardRequest(MediatorHTTPRequest originalRequest, String body) {
        MediatorHTTPRequest newRequest = new MediatorHTTPRequest(
                requestHandler,
                getSelf(),
                "Forward Request",
                originalRequest.getMethod(),
                (String)config.getDynamicConfig().get("target-scheme"),
                (String)config.getDynamicConfig().get("target-host"),
                ((Double)config.getDynamicConfig().get("target-port")).intValue(),
                originalRequest.getPath(),
                body,
                copyHeaders(originalRequest.getHeaders()),
                originalRequest.getParams()
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, getSelf());
    }

    private void forwardRetiredData(MediatorHTTPRequest originalRequest, String body) {
        MediatorHTTPRequest newRequest = new MediatorHTTPRequest(
                requestHandler,
                getSelf(),
                "Forward Request",
                "GET",
                (String)config.getDynamicConfig().get("target-scheme"),
                (String)config.getDynamicConfig().get("target-host"),
                ((Double)config.getDynamicConfig().get("target-port")).intValue(),
                "/", //TODO check to make sure this works
                body,
                copyHeaders(originalRequest.getHeaders()),
                originalRequest.getParams()
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, getSelf());
    }

    private void processRequest(MediatorHTTPRequest request) {
        try {
            String body = null;

            if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")) {
                body = getProcessor().dataProcess(request.getBody());
            }

            forwardRequest(request, body);
        } catch (XMLStreamException ex) {
            requestHandler.tell(new ExceptError(ex), getSelf());
        }
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        System.out.println("dddddddd2111111111111111111dddddddddddddddddd" );
        if (msg instanceof MediatorHTTPRequest) { //inbound request
            requestHandler = ((MediatorHTTPRequest) msg).getRequestHandler();
            respondTo = ((MediatorHTTPRequest) msg).getRespondTo();
            String method = ((MediatorHTTPRequest) msg).getMethod();
            String endpointPath = ((MediatorHTTPRequest) msg).getPath();

            if(method.equalsIgnoreCase("GET") && endpointPath.equalsIgnoreCase("/trigger")) {//change trigger to the actual endpoint set in the console.
                System.out.println("dddddddddd2222222222222222222ddddddddddddddddddddd" );
                forwardRetiredData((MediatorHTTPRequest) msg, retrieveRemoteData((String)config.getDynamicConfig().get("originator_apiendpoint")));


            } else {
                processRequest((MediatorHTTPRequest) msg);
            }
        } else if (msg instanceof MediatorHTTPResponse) { //response from target server
            System.out.println("dddddd33333333333333333333333dddddddddddddd" );
            respondTo.tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());

        } else {
            unhandled(msg);
        }
    }

    private String retrieveRemoteData(String pg_url) {

                HttpURLConnection urlConnection = null;

                String name = (String)config.getDynamicConfig().get("originator_user");
                String password = (String)config.getDynamicConfig().get("originator_passwd");
                StringBuilder sb = new StringBuilder();

                String authString = name + ":" + password;
                if (name.isEmpty()) {
                    return "";
                }
                  System.out.println("ddddddddddddddddddddddddddddddddddddddddddddd" +authString);
                byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                String authStringEnc = new String(authEncBytes);
                try {
                    URL url = new URL(pg_url);
                      System.out.println(pg_url+"ddddddddddddddddddddddddddddddddddddddddddddddddddddd");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setUseCaches(true);
                    urlConnection.setConnectTimeout(4000);
                    urlConnection.setReadTimeout(4000);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.connect();

                    int HttpResult = urlConnection.getResponseCode();
                    //debug
                    System.out.println("######cccccccccccc####Outreach Session HTTP Return Code = " + HttpResult);

                    if (HttpResult == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();

                        //debug
                        System.out.println("#########AAA###" + sb.toString());
                        if (sb.toString().indexOf("success") >= 1) {
                            //      response.setStatus(200);
                            System.err.println("FIXED: Success!");
                            return sb.toString();
                        }
                        if (sb.toString().indexOf("warning") >= 1) {
                            //        response.setStatus(300);
                            System.err.println("FIXED: Warning!");
                            return sb.toString();
                        }
                        if ((sb.toString().indexOf("warning") >= 1) || (sb.toString().indexOf("success") >= 1)) {
                            //      response.setStatus(414);
                            System.err.println("Noticable Error:\n" + sb.toString());
                            return sb.toString();
                        }
                        System.out.println("Debugger QWERTYUH.456Y.VFR567Y: " + sb.toString());
                    } else {
                        //response.setStatus(502, "DHIS2 Not there!");
                        System.out.println("####CCCCCCCCCCCCCC" + urlConnection.getInputStream().toString());
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();

                        System.out.println("Debugger DFGH.456Y.VFR567Y: " + sb.toString());
                        System.out.println("OUT ERROR: 567uyt.876.gy: " + urlConnection.getResponseMessage());
                        return sb.toString();

                        //TODO
                    }

                } catch (IOException ex) {
                    Logger.getLogger(DHISMappingAdapter.class.getName()).log(Level.SEVERE, "error occured in resolver", ex);

                }
                System.out.println(sb.toString());
                return sb.toString();
            }


}
