package netty.leaks.dsl;

import java.util.function.Function;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

public class SecurityDsl extends AbstractDsl {

    private final Function<ServerHttpSecurity, SecurityWebFilterChain> dsl;

    private SecurityDsl(Function<ServerHttpSecurity, SecurityWebFilterChain> dsl) {
        this.dsl = dsl;
    }

    @Override
    public void initialize(final GenericApplicationContext context) {
        super.initialize(context);
        context.registerBean("springSecurityFilterChain", SecurityWebFilterChain.class, () -> {
            final ServerHttpSecurity httpSecurity = ref(ServerHttpSecurity.class);
            return this.dsl.apply(httpSecurity);
        });
    }

    public static SecurityDsl security(Function<ServerHttpSecurity, SecurityWebFilterChain> dsl) {
        return new SecurityDsl(dsl);
    }
}