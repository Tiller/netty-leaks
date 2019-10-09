package netty.leaks;

import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;

public class Filter1 implements ConnectionObserver, HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public void onStateChange(Connection connection, State newState) {
        connection.channel().localAddress();
        connection.channel().remoteAddress();
        connection.channel().id().asShortText();
    }

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        request.exchange().getRequest().getRemoteAddress();
        request.exchange().getRequest().getId();
        request.headers().header("lala");

        return next.handle(request).subscriberContext(ctx -> ctx.put("test", "value"));
    }
}
