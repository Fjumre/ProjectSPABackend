package app.config;

import app.model.ToDo;
import app.model.Role;
import app.model.User;
import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateConfig {
    private static EntityManagerFactory emf;
    private static boolean isIntegrationTest = false; // this flag is set for integration tests

    private static EntityManagerFactory buildEntityFactoryConfigDeployed() {
        try {
            Configuration configuration = new Configuration();

            Properties props = new Properties();
            props.put("hibernate.connection.url", System.getenv("CONNECTION_STR") + getDBName());
            props.put("hibernate.connection.username", System.getenv("DB_USERNAME"));
            props.put("hibernate.connection.password", System.getenv("DB_PASSWORD"));
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.archive.autodetection", "class");
            props.put("hibernate.current_session_context_class", "thread");
            props.put("hibernate.hbm2ddl.auto", "update"); // Automatically update schema
            return getEntityManagerFactory(configuration, props);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static EntityManagerFactory getEntityManagerFactory(Configuration configuration, Properties props) {
        configuration.setProperties(props);
        getAnnotationConfiguration(configuration);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
        SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
        return sf.unwrap(EntityManagerFactory.class);
    }

    public static void setTestMode(boolean isTest) {
        HibernateConfig.isIntegrationTest = isTest;
    }

    private static EntityManagerFactory emfTest;
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            if (System.getenv("DEPLOYED") != null) {
                emf = buildEntityFactoryConfigDeployed();
            } else {
                emf = createEMF(false);
            }
        }
        return emf;
    }

    public static EntityManagerFactory getEntityManagerFactoryForTest() {
        if (emfTest == null) {
            emfTest = createEMF(true);
        }
        return emfTest;
    }

    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(ToDo.class);
    }

    private static EntityManagerFactory createEMF(boolean forTest) {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            setBaseProperties(props);
            if (forTest || isIntegrationTest) {
                props = setTestProperties(props);
            } else {
                props = setDevProperties(props);
            }
            configuration.setProperties(props);
            getAnnotationConfiguration(configuration);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            return sf.unwrap(EntityManagerFactory.class);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getDBName() {
        return Utils.getPropertyValue("db.name", "properties-from-pom.properties");
    }

    private static Properties setBaseProperties(Properties props) {
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
        return props;
    }

    private static Properties setDeployedProperties(Properties props) {
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + getDBName());
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
        return props;
    }

    private static Properties setDevProperties(Properties props) {
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/" + getDBName());
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        return props;
    }

    private static Properties setTestProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
        props.put("hibernate.connection.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db");
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        props.put("hibernate.archive.autodetection", "class");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        return props;
    }
}
