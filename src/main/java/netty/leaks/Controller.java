package netty.leaks;

import java.util.List;

import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public class Controller {

    public Mono<ServerResponse> operation(final ServerRequest request) {
        return ServerResponse.ok().bodyValue(List.of());
    }

    public static RouterFunctions.Builder routes(final RouterFunctions.Builder router, final Controller controller) {
        return router.GET("/operation", controller::operation);
    }
}
