package com.breeze.hib;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class StaticConfigurator {

    private static SessionFactory sessionFactory;

    private StaticConfigurator() {
    }

    static {
        // configures settings from hibernate.cfg.xml
        Configuration configuration = new Configuration();
        try {
            sessionFactory = configuration.configure().buildSessionFactory();
            // sessionFactory = configuration.configure("/breeze/test/hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception e) {
            throw e;
        }

    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
