package netty.leaks.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

public class BeanDsl extends AbstractDsl {

    private final Consumer<BeanDsl> dsl;

    BeanDsl(Consumer<BeanDsl> dsl) {
        this.dsl = dsl;
    }

    @Override
    public void initialize(final GenericApplicationContext context) {
        super.initialize(context);
        this.dsl.accept(this);
    }

    public <T> BeanDsl bean(Class<T> beanClass) {
        final String beanName = BeanDefinitionReaderUtils.uniqueBeanName(beanClass.getName(), context);
        context.registerBean(beanName, beanClass);
        return this;
    }

    public <T> BeanDsl bean(Class<T> beanClass, Supplier<T> supplier) {
        final String beanName = BeanDefinitionReaderUtils.uniqueBeanName(beanClass.getName(), context);
        context.registerBean(beanName, beanClass, supplier);
        return this;
    }

    public <T> BeanDsl bean(Class<T> beanClass, String name, String scope, Boolean isLazyInit, Boolean isPrimary, Boolean isAutowireCandidate, String initMethodName, String destroyMethodName) {
        final BeanDefinitionCustomizer[] customizers = customizers(scope, isLazyInit, isPrimary, isAutowireCandidate, initMethodName, destroyMethodName);
        return bean(beanClass, name, customizers);
    }

    public <T> BeanDsl bean(Class<T> beanClass, String initMethodName, String destroyMethodName) {
        final String beanName = BeanDefinitionReaderUtils.uniqueBeanName(beanClass.getName(), context);
        final BeanDefinitionCustomizer[] customizers = customizers(null, null, null, null, initMethodName, destroyMethodName);
        return bean(beanClass, beanName, customizers);
    }

    public <T> BeanDsl bean(Class<T> beanClass, Supplier<T> supplier, String initMethodName, String destroyMethodName) {
        final String beanName = BeanDefinitionReaderUtils.uniqueBeanName(beanClass.getName(), context);
        final BeanDefinitionCustomizer[] customizers = customizers(null, null, null, null, initMethodName, destroyMethodName);
        return bean(beanClass, supplier, beanName, customizers);
    }

    public <T> BeanDsl bean(Class<T> beanClass, Supplier<T> supplier, String name, String scope, Boolean isLazyInit, Boolean isPrimary, Boolean isAutowireCandidate, String initMethodName,
            String destroyMethodName) {
        final BeanDefinitionCustomizer[] customizers = customizers(scope, isLazyInit, isPrimary, isAutowireCandidate, initMethodName, destroyMethodName);
        return bean(beanClass, supplier, name, customizers);
    }

    public <T> BeanDsl bean(Class<T> beanClass, String name, BeanDefinitionCustomizer... customizers) {
        context.registerBean(name, beanClass, customizers);
        return this;
    }

    public <T> BeanDsl bean(Class<T> beanClass, Supplier<T> supplier, String name, BeanDefinitionCustomizer... customizers) {
        context.registerBean(name, beanClass, supplier, customizers);
        return this;
    }

    private BeanDefinitionCustomizer[] customizers(final String scope, final Boolean isLazyInit, final Boolean isPrimary, final Boolean isAutowireCandidate, final String initMethodName,
            final String destroyMethodName) {
        List<BeanDefinitionCustomizer> customizers = new ArrayList<>(6);
        if (!StringUtils.isEmpty(scope)) {
            customizers.add(b -> b.setScope(scope));
        }

        if (isLazyInit != null) {
            customizers.add(b -> b.setLazyInit(isLazyInit));
        }

        if (isPrimary != null) {
            customizers.add(b -> b.setPrimary(isPrimary));
        }

        if (isAutowireCandidate != null) {
            customizers.add(b -> b.setAutowireCandidate(isAutowireCandidate));
        }

        if (!StringUtils.isEmpty(initMethodName)) {
            customizers.add(b -> b.setInitMethodName(initMethodName));
        }

        if (!StringUtils.isEmpty(destroyMethodName)) {
            customizers.add(b -> b.setDestroyMethodName(destroyMethodName));
        }

        final BeanDefinitionCustomizer[] customizersArr;
        if (customizers.isEmpty()) {
            customizersArr = new BeanDefinitionCustomizer[0];
        } else {
            // since late updates of OpenJDK 6 this call was intrinsified, making the performance of the empty array version the same and sometimes
            // even better, compared to the pre-sized version
            customizersArr = customizers.toArray(new BeanDefinitionCustomizer[0]);
        }

        return customizersArr;
    }
}
