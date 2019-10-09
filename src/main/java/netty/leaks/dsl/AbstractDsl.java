package netty.leaks.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;

public abstract class AbstractDsl implements ApplicationContextInitializer<GenericApplicationContext> {

    protected GenericApplicationContext context;

    @Override
    public void initialize(final GenericApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext context() {
        return context;
    }

    public <T> T ref(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public <T> T ref(String name, Class<T> beanClass) {
        return context.getBean(name, beanClass);
    }

    public <T> List<? extends T> refs(Class<T> beanClass) {
        return context.getBeansOfType(beanClass).entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T> List<? extends T> refs(ResolvableType baseType) {
        String[] names = context.getBeanNamesForType(baseType);
        return Arrays.stream(names).map(it -> (T) context.getBean(it)).collect(Collectors.toList());
    }

    public <T> T property(final String name, Class<T> targetType) {
        return context.getEnvironment().getRequiredProperty(name, targetType);
    }

    public <T> Optional<T> tryProperty(final String name, final Class<T> targetType) {
        return Optional.ofNullable(context.getEnvironment().getProperty(name, targetType));
    }

    public <T> T property(final String name, final Class<T> targetType, final T defaultValue) {
        return context.getEnvironment().getProperty(name, targetType, defaultValue);
    }

    public int property(final String name, final int defaultValue) {
        return property(name, Integer.class, defaultValue);
    }

    public String property(final String name) {
        return context.getEnvironment().getRequiredProperty(name);
    }

    public String property(final String name, final String defaultValue) {
        return context.getEnvironment().getProperty(name, defaultValue);
    }
}
