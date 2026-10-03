package io.a2aspring.web;

import org.a2aproject.sdk.server.TransportMetadata;
import org.a2aproject.sdk.spec.TransportProtocol;

/** Advertises the JSON-RPC transport implemented by this Spring MVC starter. */
public final class JsonRpcTransportMetadata implements TransportMetadata {
    @Override
    public String getTransportProtocol() {
        return TransportProtocol.JSONRPC.asString();
    }
}
