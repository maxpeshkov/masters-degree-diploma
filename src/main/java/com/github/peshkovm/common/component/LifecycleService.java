package com.github.peshkovm.common.component;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Evaluate lifecycle methods on all components implementing {@link LifecycleComponent}. */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LifecycleService extends AbstractLifecycleComponent implements BeanPostProcessor {
  private final List<LifecycleComponent> lifecycleQueue = new ArrayList<>();

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName)
      throws BeansException {
    if (bean instanceof LifecycleComponent) {
      LifecycleComponent lifecycleComponent = (LifecycleComponent) bean;
      lifecycleQueue.add(lifecycleComponent);
    }
    return bean;
  }

  @Override
  protected void doStart() {
    lifecycleQueue.forEach(LifecycleComponent::start);
  }

  @Override
  protected void doStop() {
    final int size = lifecycleQueue.size();
    for (int i = size - 1; i >= 0; i--) {
      LifecycleComponent component = lifecycleQueue.get(i);
      component.stop();
    }
  }

  @Override
  protected void doClose() {
    final int size = lifecycleQueue.size();
    for (int i = size - 1; i >= 0; i--) {
      LifecycleComponent component = lifecycleQueue.get(i);
      component.close();
    }
  }
}
