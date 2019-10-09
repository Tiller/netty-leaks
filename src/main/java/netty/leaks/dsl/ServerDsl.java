package netty.leaks.dsl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.WebExceptionHandler;

public class ServerDsl extends AbstractDsl {

    private final Consumer<ServerDsl> dsl;

    private ServerDsl(Consumer<ServerDsl> dsl) {
        this.dsl = dsl;
    }

    @Override
    public void initialize(final GenericApplicationContext context) {
        super.initialize(context);
        this.dsl.accept(this);
        registerServerReactiveResourceFactory();
        registerReactiveWebServerFactory();
        registerExceptionHandler();
    }

    public ServerDsl router(Function<RouterFunctions.Builder, RouterFunction<ServerResponse>> routerDsl) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        context
                .registerBean(BeanDefinitionReaderUtils.uniqueBeanName(RouterFunction.class.getName(), context),
                        RouterFunction.class, () -> routerDsl.apply(builder));
        return this;
    }

    private void registerReactiveWebServerFactory() {
        context.registerBean(NettyReactiveWebServerFactory.class, () -> {
            NettyReactiveWebServerFactory serverFactory = new NettyReactiveWebServerFactory();
            serverFactory.setServerCustomizers(refs(NettyServerCustomizer.class));
            serverFactory.setResourceFactory(ref(ReactorResourceFactory.class));
            return serverFactory;
        });
    }

    private void registerServerReactiveResourceFactory() {
        context.registerBean("serverReactiveResourceFactory", ReactorResourceFactory.class, ReactorResourceFactory::new);
    }

    private void registerExceptionHandler() {
        context.registerBean("webExceptionHandler", WebExceptionHandler.class, () -> {
            final DefaultErrorWebExceptionHandler handler = new DefaultErrorWebExceptionHandler(ref(ErrorAttributes.class), ref(ResourceProperties.class),
                    ref(ServerProperties.class).getError(), context());
            handler.setMessageWriters(ref(ServerCodecConfigurer.class).getWriters());
            handler.setMessageReaders(ref(ServerCodecConfigurer.class).getReaders());
            return handler;
        });
    }

    public ServerDsl enable(WebSocketDsl dsl) {
        dsl.initialize(context);
        return this;
    }

    public static ServerDsl server(Consumer<ServerDsl> dsl) {
        return new ServerDsl(dsl);
    }

    public static WebSocketDsl websocket(Consumer<WebSocketDsl> dsl) {
        return new WebSocketDsl(dsl);
    }

    public static final class WebSocketDsl extends AbstractDsl {

        private final Consumer<WebSocketDsl> dsl;

        private final Map<String, WebSocketHandler> map = new HashMap<>();

        private WebSocketDsl(Consumer<WebSocketDsl> dsl) {
            this.dsl = dsl;
        }

        public WebSocketDsl endpoint(String path, WebSocketHandler handler) {
            this.map.put(path, handler);
            return this;
        }

        @Override
        public void initialize(final GenericApplicationContext context) {
            super.initialize(context);
            this.dsl.accept(this);
            registerWebSocketHandlerAdapter();
            registerHandlerMapping();
        }

        private void registerHandlerMapping() {
            context.registerBean("webSocketMapping", HandlerMapping.class, () -> {
                final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
                mapping.setUrlMap(map);
                mapping.setOrder(10);
                return mapping;
            });
        }

        private void registerWebSocketHandlerAdapter() {
            context.registerBean("handlerAdapter", WebSocketHandlerAdapter.class, () -> new WebSocketHandlerAdapter());
        }
    }
}
