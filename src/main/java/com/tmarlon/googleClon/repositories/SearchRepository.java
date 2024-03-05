package com.tmarlon.googleClon.repositories;

import com.tmarlon.googleClon.entities.WebPage;

import java.util.List;

public interface SearchRepository  {

    WebPage getByUrl(String url);

    List<WebPage> getLinksToindex();

    List<WebPage> search(String textSearch);


    void save(WebPage webPage);

    boolean exist(String link);

}
