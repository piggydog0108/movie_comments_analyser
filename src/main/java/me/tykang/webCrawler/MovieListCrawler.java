package me.tykang.webCrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class MovieListCrawler {

    private final static Logger log = LoggerFactory.getLogger(MovieListCrawler.class);
    private String MovieListURL;

    public MovieListCrawler(String movieListURL){
        this.MovieListURL=movieListURL;
    }

    public void getMovieList(){

        Document doc=null;

        try {
            doc= Jsoup.connect(MovieListURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements elements = doc.select("div.article").select("tr");


        for(Element el : elements.select("td")){
            if (!el.text().equals("")){
                System.out.println(el.text());
            }
        }


    }


}
