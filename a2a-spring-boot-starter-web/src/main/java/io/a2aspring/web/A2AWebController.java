package io.a2aspring.web;

import io.a2aspring.core.A2ARuntime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(A2ARuntime.class)
public class A2AWebController {
    private final JsonRpcRequestDispatcher dispatcher;
    private final ServerCallContextFactory contextFactory;

    public A2AWebController(JsonRpcRequestDispatcher dispatcher,
                            ServerCallContextFactory contextFactory) {
        this.dispatcher = dispatcher;
        this.contextFactory = contextFactory;
    }

    @PostMapping(value = "/a2a", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jsonRpc(@RequestBody String request,
                                          jakarta.servlet.http.HttpServletRequest servletRequest) throws Exception {
        String response = dispatcher.dispatch(request, contextFactory.create(servletRequest));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }
}
