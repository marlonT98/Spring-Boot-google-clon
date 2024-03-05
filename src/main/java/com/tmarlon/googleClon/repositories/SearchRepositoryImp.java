package com.tmarlon.googleClon.repositories;

import com.tmarlon.googleClon.entities.WebPage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class SearchRepositoryImp implements SearchRepository {


    @PersistenceContext
    EntityManager entityManager;//aqui tendra la conexion a la base de datos


    @Override//nos devuelve una webPage a partir de la url que buscamos en la base de datos
    public WebPage getByUrl(String url) {

        String query = "FROM WebPage WHERE url = :url";
        List<WebPage> list = entityManager.createQuery(query)
                .setParameter("url", url)
                .getResultList();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public List<WebPage> getLinksToindex() {
        String query = "FROM WebPage WHERE title is null AND description is null";
        return entityManager.createQuery(query)
                .setMaxResults(100)
                .getResultList();


    }

    @Transactional
    @Override
    public List<WebPage> search(String textSearch) {

        String query = "FROM WebPage WHERE description like :textSearch";
        return entityManager.createQuery(query)
                .setParameter("textSearch", "%" + textSearch + "%")
                .getResultList();

    }

    @Transactional
    @Override
    public void save(WebPage webPage) {

        entityManager.merge(webPage);

    }

    @Override
    public boolean exist(String url) {

        return getByUrl(url) != null;
    }


}
