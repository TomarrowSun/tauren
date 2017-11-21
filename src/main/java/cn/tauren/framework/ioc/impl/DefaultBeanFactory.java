/**
 * 深圳金融电子结算中心
 * Copyright (c) 1995-2017 All Rights Reserved.
 */
package cn.tauren.framework.ioc.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import cn.tauren.framework.exception.BeanCreationException;
import cn.tauren.framework.exception.BeanException;
import cn.tauren.framework.exception.BeanNotOfRequiredTypeException;
import cn.tauren.framework.exception.NoSuchBeanException;
import cn.tauren.framework.ioc.annotation.Bean;
import cn.tauren.framework.ioc.api.BeanFactory;
import cn.tauren.framework.ioc.api.ClassScanner;
import cn.tauren.framework.util.AssertUtil;
import cn.tauren.framework.util.ClassUtil;

/**
 * Bean工厂
 * <ul>
 *  <li>扫描指定目录下的Bean</li>
 *  <li>将带有{@link Bean}注解的类初始化</li>
 *  <li>初始化完毕后放入Map中</li>
 * </ul>
 * 如果Bean存在以下情况将不能被框架实例化：
 * <ul>
 *  <li>没有无参的构造方法</li>
 *  <li>构造方法是私有的</li>
 * </ul>
 * @author HuHui
 * @version $Id: DefaultBeanFactory.java, v 0.1 2017年11月16日 上午10:56:56 HuHui Exp $
 */
public class DefaultBeanFactory implements BeanFactory {

    /**
     * 存放类的实例的Map,即用于存储Bean的容器
     * key为类的name
     * value为实例对象
     */
    private final Map<String, Object>   nameContainer;

    private final Map<Class<?>, Object> typeContainer;

    /** 类扫描器 */
    private final ClassScanner          scanner;

    public DefaultBeanFactory(ClassScanner scanner) {
        nameContainer = new HashMap<String, Object>();
        typeContainer = new HashMap<Class<?>, Object>();
        this.scanner = scanner;
        initContainer();
    }

    @Override
    public Object getBean(String name) throws BeanException {
        Object object = nameContainer.get(name);
        if (object == null) {
            throw new NoSuchBeanException("no such bean named " + name);
        }
        return object;
    }

    @Override
    public Object getBean(Class<?> requiredType) throws BeanException {
        Object object = typeContainer.get(requiredType);
        if (object == null) {
            throw new NoSuchBeanException("no such bean's type is " + requiredType.getSimpleName());
        }
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) throws BeanException {
        Object object = nameContainer.get(name);
        if (object == null) {
            throw new NoSuchBeanException("no such bean named " + name);
        }

        if (!requiredType.isInstance(object)) {
            throw new BeanNotOfRequiredTypeException("bean name which is " + name + " is not match whit the type of " + requiredType.getSimpleName());
        }

        return (T) object;
    }

    @Override
    public Collection<Object> getBeans() {
        return nameContainer.values();
    }

    private void initContainer() {
        List<Class<?>> classes = scanner.getClassesByAnnotation(Bean.class);
        if (CollectionUtils.isNotEmpty(classes)) {
            for (Class<?> clazz : classes) {
                Bean beanAnno = clazz.getAnnotation(Bean.class);
                String beanName = ClassUtil.humpNaming(clazz.getSimpleName());

                //如果指定了bean的name则修改默认名称
                if (StringUtils.isNotBlank(beanAnno.value())) {
                    beanName = beanAnno.value();
                }

                AssertUtil.assertTrue(!nameContainer.containsKey(beanName), "类名重复");

                try {
                    Object instance = clazz.newInstance();
                    nameContainer.put(beanName, instance);
                    typeContainer.put(clazz, instance);
                } catch (Exception e) {
                    throw new BeanCreationException("初始化类失败", e);
                }
            }
        }
    }

}