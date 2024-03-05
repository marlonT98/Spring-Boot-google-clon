package com.tmarlon.googleClon.services;


import com.tmarlon.googleClon.entities.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SpiderService {

    @Autowired
    private SearchService searchService;


    public void indexWebPages() {

       List<WebPage> linksToindex = searchService.getLinksToindex();
        linksToindex.stream().parallel().forEach(webPage -> {

            try {
                indexWebPage(webPage);
            }catch (Exception e){
                System.out.println("e.getMessage() = " + e.getMessage());
            }
            
            
        });



    }

    private void indexWebPage(WebPage webPage) throws Exception{
        String url = webPage.getUrl();
        String content = getWebContent(url);

        if (content.isBlank()) {
            return;
        }

        indexAndSaveWebPage(content, webPage);
        String domain = getDomain(url);
        saveLinks(domain, content);
    }

    private String getDomain(String url) {

        String[] aux = url.split("/");
        return aux[0] + "//" + aux[2];


    }

    private void saveLinks(String domain, String content) {

        List<String> links = getLinks(domain, content);
        links.stream()
                .filter(link -> !searchService.exist(link))
                .map(link -> new WebPage(link))
                .forEach(webPage -> searchService.save(webPage));


    }

    public List<String> getLinks(String domain, String content) {

        List<String> links = new ArrayList<>();//creamos la lista de links

        //cuando nos pasan el contenido lo separamos a travaes del href
        String[] splitHref = content.split("href=\"");

        //de esta lista eliminamos el primero
        List<String> listHref = Arrays.asList(splitHref);
        //listHref.remove(0);

        //de esta lista recorremos
        listHref.forEach(strHref -> {

            //de este estring de href obtenmos el link
            String[] link = strHref.split("\"");
            //agremos a la lista de los links
            links.add(link[0]);

        });


        return cleanLinks(domain, links);//retornamos los links
    }

    private List<String> cleanLinks(String domain, List<String> links) {

        String[] excludeEstencions = new String[]{"css", "js", "json", "png", "woff2"};

        List<String> resultLinks = links.stream()

                .filter(link -> !Arrays.stream(excludeEstencions).anyMatch(extencion -> link.endsWith(extencion)))
                .map(link -> link.startsWith("/") ? domain + link : link)
                .filter(link->link.startsWith("http"))
                .collect(Collectors.toList());


        //con el set nos queda garantizado que no se repitira los liks
        List<String> uniqueLinks = new ArrayList<>();
        uniqueLinks.addAll(new HashSet<String>(resultLinks));

        return uniqueLinks;

    }


    private void indexAndSaveWebPage(String content, WebPage webPage) {

        String title = getTitle(content);
        String description = getDescription(content);

        webPage.setTitle(title);
        webPage.setDescription(description);
        searchService.save(webPage);


    }

    public String getTitle(String content) {

        String[] aux = content.split("<title>");
        String[] aux2 = aux[1].split("</title>");

        return aux2[0];


    }


    public String getDescription(String content) {

        String[] aux = content.split("<meta name=\"description\" content=\"");
        String[] aux2 = aux[1].split("\">");
        return aux2[0];


    }

    private String getWebContent(String link) {


        try {
            URL url = new URL(link);//le pasamos el link del parametro
            //de esta forma nos conectamos a la url(hacemos un cast)
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //2.-DESCARGANDO EL CONTENIDO
            //trae solo la cabecera de la pagina web para saber en que formato esta codeado
            String encoding = connection.getContentEncoding();

            InputStream inputStream = connection.getInputStream();
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream))
                    .lines();


            return lines.collect(Collectors.joining());

        } catch (IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());

        }

        return "";

    }
}
