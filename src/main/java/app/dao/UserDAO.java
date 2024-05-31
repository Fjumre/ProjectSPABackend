package app.dao;

import app.exceptions.EntityNotFoundException;
import app.model.Role;
import app.model.User;
import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Set;

public class UserDAO implements ISecurityDAO {
    private EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }


    @Override
    public User createUser(String username, String password, String email, Integer phoneNumber) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Create a new user
            User user = new User(username, password, email, phoneNumber);

            // Ensure the 'USER' role exists and is retrieved or created
            Role userRole = em.createQuery("SELECT r FROM Role r WHERE r.rolename = :rolename", Role.class)
                    .setParameter("rolename", "user")
                    .getResultStream()
                    .findFirst()
                    .orElseGet(() -> {
                        Role newRole = new Role("user");
                        em.persist(newRole);
                        return newRole;
                    });

            // Assign the 'USER' role to the new user
            user.addRole(userRole);

            // Persist the new user
            em.persist(user);

            // Commit the transaction
            transaction.commit();

            return user;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw e;  // or handle the exception in a way that suits your application
        } finally {
            em.close();
        }
    }


    //    public User verifyUser(String username, String password) throws EntityNotFoundException {
//        EntityManager em = emf.createEntityManager();
//        User user = em.find(User.class, username);
//
//    }
        @Override
        public User UpdateUser (String username, String password, Set<Role> roles){
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            User user = new User(username, password, roles);
            Role userRole = em.find(Role.class, "user");
            if (userRole == null) {
                userRole = new Role("user");
                em.persist(userRole);
            }
            user.addRole(userRole);
            em.merge(user);
            em.getTransaction().commit();
            em.close();
            return user;
        }


        @Override
        public User UpdatePassword (User user, String newPassword){
            // Update the user's password and return the updated user
            String salt = BCrypt.gensalt();
            user.setPassword(BCrypt.hashpw(newPassword, salt));
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
            em.close();
            return user;
        }
        @Override
        public User update (User user){

            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
            return user;
        }


        @Override
        public List<User> getAlleUser () {
            EntityManager em = emf.createEntityManager();
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();

        }


        @Override
        public User getUserById ( int id){
            EntityManager em = emf.createEntityManager();
            return em.find(User.class, id);
        }


    public User verifyUser(String username, String password) throws EntityNotFoundException {
        EntityManager em = emf.createEntityManager();
        try {
            // Using JPQL to query by username
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();


            if (!user.verifyUser(password)) {
                throw new EntityNotFoundException("Wrong password");
            }
            return user;
        } catch (NoResultException e) {
             throw new EntityNotFoundException("No user found with that username: " + username);
        } finally {
            em.close();
        }
    }

    @Override
        public User verifyUserForReset (String email, String password) throws EntityNotFoundException {
            EntityManager em = emf.createEntityManager();
            User user = em.find(User.class, email);
            em.close();
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email);
            if (!user.verifyUser(password))
                throw new EntityNotFoundException("Wrong password");
            return user;

        }

//    public static void Main(String[] args) {
//        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
//        UserDAO dao = new UserDAO(emf);
//        User user = dao.createUser("4hh", "1234", "fff@r.com", 55456633);
//
////        System.out.println(user.getUsername());
//        try {
//            User verifiedUser = dao.verifyUser("4hh", "1234");
//            System.out.println(verifiedUser.getName());
//
//            Role verifiRole= dao.createRole("admin");
//
//
//            User updatedUser = dao.addRoleToUser("Bibi", "instructor");
//            System.out.println("Role added to user: " + updatedUser.getName());
//        } catch (EntityNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


        @Override
        public Role createRole (String role){
            EntityManager em = emf.createEntityManager();
            Role existingRole = em.find(Role.class, role);
            if (existingRole != null) {
                return existingRole;
            }
            // If the role doesn't exist, create and persist a new Role object
            Role newRole = new Role(role);

            try {
                em.getTransaction().begin();
                em.persist(newRole);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback(); // Rollback in case of an exception
                // Handle or rethrow the exception as appropriate for your application
                throw new RuntimeException("Failed to create role due to: " + e.getMessage(), e);
            }

            return newRole;
        }


        @Override
        public User addRoleToUser (String username, String rolename){

            EntityManager em = emf.createEntityManager();

            User user;
            try {
                em.getTransaction().begin();
                user = em.find(User.class, username);
                Role role = em.find(Role.class, rolename);

                user.addRole(role); // Modify the collection in the managed entity

                em.merge(user); // Ensure changes are cascaded to the database

                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Failed to add role to user due to: " + e.getMessage(), e);
            }

            return user;

        }

        @Override
        public void deleteUser ( int id){
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            User user = em.find(User.class, id);
            em.remove(user);
            em.getTransaction().commit();
        }


    public User findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }


}
