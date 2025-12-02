package com.example.provapoo3.dao;

import com.example.provapoo3.model.Disciplina;
import com.example.provapoo3.model.Professor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

import static com.example.provapoo3.utils.JPAUtil.getEntityManager;

public class DisciplinaDAO extends GenericDAOImpl<Disciplina, Long> {

    public DisciplinaDAO() {
        super();
    }

    public Disciplina findByIdWithProfessores(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT d FROM Disciplina d " +
                                    "LEFT JOIN FETCH d.professores " +
                                    "LEFT JOIN FETCH d.curso " +
                                    "WHERE d.id = :id", Disciplina.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Disciplina> findAllWithProfessoresAndCurso() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT d FROM Disciplina d " +
                                    "LEFT JOIN FETCH d.professores " +
                                    "LEFT JOIN FETCH d.curso", Disciplina.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Disciplina disciplina) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            Disciplina managedDisciplina = em.find(Disciplina.class, disciplina.getId());
            managedDisciplina.setNome(disciplina.getNome());
            managedDisciplina.setDescricao(disciplina.getDescricao());
            managedDisciplina.setCurso(em.merge(disciplina.getCurso()));

            managedDisciplina.getProfessores().clear();
            for (Professor professor : disciplina.getProfessores()) {
                managedDisciplina.getProfessores().add(em.merge(professor));
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Disciplina disciplina) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            Disciplina managedDisciplina = em.find(Disciplina.class, disciplina.getId());
            if (managedDisciplina != null) {
                // Remove associações com professores
                for (Professor professor : managedDisciplina.getProfessores()) {
                    professor.getDisciplinas().remove(managedDisciplina);
                }
                managedDisciplina.getProfessores().clear();

                // Remove turmas associadas
                em.createQuery("DELETE FROM Turma t WHERE t.disciplina.id = :id")
                        .setParameter("id", managedDisciplina.getId())
                        .executeUpdate();

                em.remove(managedDisciplina);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}