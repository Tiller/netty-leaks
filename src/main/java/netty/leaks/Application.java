package netty.leaks;

import static netty.leaks.dsl.SecurityDsl.security;
import static netty.leaks.dsl.ServerDsl.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.web.reactive.function.server.RequestPredicates;

import netty.leaks.dsl.AppDsl;
import reactor.netty.http.HttpProtocol;

@SpringBootApplication(exclude = {
        MongoReactiveDataAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        AppDsl.app(Application.class, app -> {
            app.beans(b -> {
                // b.bean(Filter1.class, Filter1::new);
                // b.bean(Filter2.class, Filter2::new);

                b.bean(Controller.class, Controller::new);

                // b.bean(NettyServerCustomizer.class, () -> s -> s.observe(b.ref(Filter1.class)));
                b.bean(NettyServerCustomizer.class, () -> s -> s.protocol(HttpProtocol.HTTP11, HttpProtocol.H2C));
            });

            app
                    .enable(server(s -> s
                            .router(r -> r
                                    // .filter(app.ref(Filter1.class))
                                    .nest(RequestPredicates.path("/tap"), b -> Controller
                                            .routes(b, app.ref(Controller.class))
                                            // .filter(app.ref(Filter2.class))
                                            .build())
                                    .build())));

            app.enable(security(http -> {
                http.cors().disable();
                http.csrf().disable();
                http
                        .authorizeExchange()
                        .anyExchange()
                        .permitAll();

                return http.build();
            }));
        }).run();
    }
}