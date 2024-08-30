/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.mirabilia.giz;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import com.mirabilia.giz.converter.DataProcessor;
import com.mirabilia.giz.sormas.SormasDataProcessor;
import com.mirabilia.giz.util.RemoteAccess;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.ExceptError;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareMappingAdapter extends UntypedActor {

    private final MediatorConfig config;
    private ActorRef requestHandler;
    private ActorRef respondTo;
    private RemoteAccess remoteAccess;


    public MiddlewareMappingAdapter(MediatorConfig config) {
        this.config = config;
        this.remoteAccess = new RemoteAccess(config);
    }

    private DataProcessor getProcessor() {
        return new DataProcessor(
                (Map<String, String>) config.getDynamicConfig().get("mappings-datasets"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-dataelements"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-orgunits"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-programs")
        );
    }

    private Map<String, String> copyHeaders(Map<String, String> headers) {
        Map<String, String> copy = new HashMap<>();
        copy.put("content-type", "application/xml");
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
                (String) config.getDynamicConfig().get("target-scheme"),
                (String) config.getDynamicConfig().get("target-host"),
                ((Double) config.getDynamicConfig().get("target-port")).intValue(),
                originalRequest.getPath(),
                body,
                copyHeaders(originalRequest.getHeaders()),
                originalRequest.getParams()
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, getSelf());
    }

    private void forwardRetrievedData(MediatorHTTPRequest originalRequest, String body) {
        MediatorHTTPRequest newRequest = new MediatorHTTPRequest(
                requestHandler,
                getSelf(),
                "Forward Request",
                "POST",
                (String) config.getDynamicConfig().get("target-scheme"),
                (String) config.getDynamicConfig().get("target-host"),
                ((Double) config.getDynamicConfig().get("target-port")).intValue(),
                originalRequest.getPath(), //TODO check to make sure this works
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

//        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
//        String authStringEnc = new String(authEncBytes);

        if (msg instanceof MediatorHTTPRequest) {
            System.out.println(msg.toString() + " gggggggggggggggggg");
//            "Authorization", "Basic " + authStringEnc
        }

        if (msg instanceof MediatorHTTPRequest) { //inbound request
            requestHandler = ((MediatorHTTPRequest) msg).getRequestHandler();
            respondTo = ((MediatorHTTPRequest) msg).getRespondTo();
            String method = ((MediatorHTTPRequest) msg).getMethod();
            String endpointPath = ((MediatorHTTPRequest) msg).getPath();

            // Check if polling is configure for this adapter
            if (method.equalsIgnoreCase("GET") && endpointPath.equalsIgnoreCase("/dhis/api/dataValueSets")) {

//                String rawSormasData = remoteAccess.retrieveRemoteData((String) config.getDynamicConfig().get("originator_apiendpoint"));

//                String transposedData = SormasDataProcessor.transposeSomrasData(rawSormasData);
                String transposedData = SormasDataProcessor.transposeSomrasData("");

                // forward the data to RIPHSS System !important to pass the retrieved and cleaned data here
                forwardRetrievedData((MediatorHTTPRequest) msg, getProcessor().dataProcess(transposedData));
            } else {
                processRequest((MediatorHTTPRequest) msg);
            }
        } else if (msg instanceof MediatorHTTPResponse) {
            respondTo.tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            unhandled(msg);
        }
    }

}
