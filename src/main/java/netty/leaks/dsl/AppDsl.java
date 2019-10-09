package netty.leaks.dsl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class AppDsl extends AbstractDsl {

    private final Class<?> source;
    private final Consumer<AppDsl> dsl;

    private AppDsl(Class<?> source, Consumer<AppDsl> dsl) {
        this.dsl = dsl;
        this.source = source;
    }

    @Override
    public void initialize(final GenericApplicationContext context) {
        super.initialize(context);
        dsl.accept(this);
        registerMessageSource();
    }

    private void registerMessageSource() {
        context.registerBean("messageSource", MessageSource.class, () -> {
            final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setFallbackToSystemLocale(false);
            messageSource.setBasename("classpath:i18n/messages");
            return messageSource;
        });
    }

    public ConfigurableApplicationContext run() {
        return run("", new String[0]);
    }

    public ConfigurableApplicationContext run(String profiles, String[] args) {
        return run(WebApplicationType.REACTIVE, profiles, args);
    }

    public ConfigurableApplicationContext run(WebApplicationType type, String profiles, String[] args) {
        final SpringApplicationBuilder app = new SpringApplicationBuilder().sources(source).initializers(this);
        if (!profiles.isEmpty()) {
            app.profiles(Arrays.stream(profiles.split(",")).map(String::trim).toArray(String[]::new));
        }
        return app
                .web(type)
                .contextClass(getContextClass(type))
                .run(args);
    }

    private Class<? extends ConfigurableApplicationContext> getContextClass(final WebApplicationType type) {
        switch (type) {
            case NONE:
                return GenericApplicationContext.class;
            case SERVLET:
                return ServletWebServerApplicationContext.class;
            case REACTIVE:
                return ReactiveWebServerApplicationContext.class;
            default:
                throw new RuntimeException("Unknown WebApplicationType " + type);
        }
    }

    public AppDsl enable(ApplicationContextInitializer<GenericApplicationContext> dsl) {
        dsl.initialize(context);
        return this;
    }

    public AppDsl beans(Consumer<BeanDsl> beanDsl) {
        new BeanDsl(beanDsl).initialize(context);
        return this;
    }

    public static <T> AppDsl app(Class<T> bootClass, Consumer<AppDsl> dsl) {
        return new AppDsl(bootClass, dsl);
    }
}
