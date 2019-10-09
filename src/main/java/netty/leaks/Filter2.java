package netty.leaks;

import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public class Filter2 implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return Mono.subscriberContext().flatMap(ctx -> {
            if ("value".equals(ctx.get("test"))) {
                return next.handle(request);
            }

            return ServerResponse.badRequest().build();
        });
    }
}
