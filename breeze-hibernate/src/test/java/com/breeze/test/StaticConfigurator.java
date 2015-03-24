package com.breeze.test;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class StaticConfigurator {

    private static SessionFactory sessionFactory;

    private StaticConfigurator() {
    }

    static {
        // configures settings from hibernate.cfg.xml
        Configuration configuration = new Configuration();
      
        try {
            // old code
            // sessionFactory = configuration.configure().buildSessionFactory();
            configuration.configure();
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                    configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            
        } catch (Exception e) {
            throw e;
        }

    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
