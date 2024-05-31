package app.dao;


import app.model.ToDo;
import app.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ToDoDAO {
    private static ToDoDAO instance;
    private static EntityManagerFactory emf;
public ToDoDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static ToDoDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            ToDoDAO.emf = emf;
            instance = new ToDoDAO(ToDoDAO.emf);
        }
        return instance;
    }

    public ToDoDAO() {
    }
    // As a user, I want to see all the events/workshops that are going to be held.

    public List<ToDo> getAllToDos() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT e FROM ToDo e", ToDo.class).getResultList();

    }
    public List<ToDo> getAllToDosByID(int user_id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ToDo> query = em.createQuery(
                    "SELECT t FROM ToDo t JOIN t.user u WHERE u.id = :user_id", ToDo.class);
            query.setParameter("user_id", user_id);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ToDo> getToDosByDate(Date date) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ToDo> query = em.createQuery("SELECT e FROM ToDo e WHERE e.Date = :date", ToDo.class);
            query.setParameter("date", date);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    public List<ToDo> getToDosByDateAndUsername(String username, LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("Querying for username: " + username + " on date: " + date);
            TypedQuery<ToDo> query = em.createQuery(
                    "SELECT t FROM ToDo t JOIN t.users u WHERE u.username = :username AND t.Date = :date",
                    ToDo.class
            );
            query.setParameter("username", username);
            query.setParameter("date", date);
            List<ToDo> results = query.getResultList();
            System.out.println("Found to-dos: " + results.size());
            return results;
        } finally {
            em.close();
        }
    }

    public List<ToDo> getToDosByDateAndId(int id, LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {

            TypedQuery<ToDo> query = em.createQuery(
                    "SELECT t FROM ToDo t JOIN t.user u WHERE u.id = :id AND t.Date = :date",
                    ToDo.class
            );
            query.setParameter("id", id);
            query.setParameter("date", date);
            List<ToDo> results = query.getResultList();
            System.out.println("Found to-dos: " + results.size());
            return results;
        } finally {
            em.close();
        }
    }




    public ToDo getToDoById(int id) {
        EntityManager em = emf.createEntityManager();
        return em.find(ToDo.class, id);
    }

    public List<ToDo> getToDoByUserId(int user_id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ToDo> query = em.createQuery(
                    "SELECT t FROM ToDo t WHERE t.user.id = :user_id", ToDo.class);
            query.setParameter("user_id", user_id);
            return query.getResultList();
        } finally {
            em.close();
        }
    }





    public List<ToDo> getToDoByStatus(String status) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM ToDo e WHERE e.Status = :status";
            TypedQuery<ToDo> query = em.createQuery(jpql, ToDo.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ToDo create(ToDo toDo) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(toDo);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return toDo;
    }
    public ToDo read(int id) {
        EntityManager em = emf.createEntityManager();
        return em.find(ToDo.class, id);
    }


    public ToDo update(ToDo toDo) {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(toDo);
        em.getTransaction().commit();
        return toDo;
    }


    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ToDo toDo = em.find(ToDo.class, id);
        em.remove(toDo);
        em.getTransaction().commit();
    }

    public void addToDoToUser(ToDo toDo, User userAdd) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User user= em.find(User.class, userAdd.getId());
        user.addToDo(toDo);
        em.getTransaction().commit();
    }

    public void removeToDOFromUser(ToDo toDo, User userRemove) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User user= em.find(User.class, userRemove.getId());
        user.removeToDo(toDo);
        em.getTransaction().commit();
    }


    public List<User> getRegistrationsForToDoById(int toDoId) {
        EntityManager em = emf.createEntityManager();
        try {

            String jpql = "SELECT DISTINCT u FROM User u JOIN FETCH u.roles JOIN FETCH u.toDos e WHERE e.ToDoId = :toDoId";
            List<User> users = em.createQuery(jpql, User.class)
                    .setParameter("toDoId", toDoId)
                    .getResultList();
            return users;
        } finally {
            em.close();
        }
    }

    public long getRegistrationsCountById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) " +
                    "FROM User u " +
                    "JOIN u.toDos e " +
                    "WHERE e.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.getSingleResult();
            return count;
        } catch (NoResultException e) {
            System.out.println(e);
            return 0; // If there's no such event, returning count as 0
        } finally {
            em.close();
        }
    }

    public void addUserToToDo(int id, int toDoId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            ToDo toDo = em.find(ToDo.class, toDoId);

            if (user != null && toDo != null) {
                // Add the user to the toDo
                toDo.getUsers().add(user);

                // Also update the user's side of the relationship if it's bidirectional
                user.getToDos().add(toDo);
                em.merge(user);
                em.merge(toDo);
            } else {
                System.out.println("User or toDo not found");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error adding user to event", e);
        } finally {
            em.close();
        }
    }



    public void removeUserToDo(int id, int toDoId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            ToDo toDo = em.find(ToDo.class, toDoId);

            if (user != null && toDo != null) {

                toDo.getUsers().remove(user);
                user.getToDos().remove(toDo);

                em.merge(user);
                em.merge(toDo);
            } else {
                System.out.println("User or toDo not found");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error removing user from event", e);
        } finally {
            em.close();
        }
    }
    public ToDo findByIdAndDate(int id, LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM ToDo t WHERE t.id = :id AND t.Date = :date", ToDo.class)
                    .setParameter("id", id)
                    .setParameter("date", date)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }


}

