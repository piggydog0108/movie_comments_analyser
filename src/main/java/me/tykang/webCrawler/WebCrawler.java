package me.tykang.webCrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;

public class WebCrawler extends TimerTask {

    private final static Logger log = LoggerFactory.getLogger(WebCrawler.class);
    private String URL;
    private Integer LastNumber;

    public WebCrawler(String url, Integer lastNumber){
        this.URL=url;
        this.LastNumber=lastNumber;
    }

    @Override
    public void run() {
        Document doc=null;

        log.debug("Thead id =" + Thread.currentThread().getId());

        for(int page=1; page<=LastNumber; page++){

            try {
                doc= Jsoup.connect(URL+page).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements elements = doc.select("div.article");

            for(Element el : elements.select("tr")){
                String commentId=el.select("td.ac num").text();
                String titleAndComments=el.select("td.title").text();
                titleAndComments=titleAndComments.replace(" 신고","").replace(":","");

                String[] arrTitle=titleAndComments.split(" ");
                String title=arrTitle[0];
                String comments="";

                for(int i=1; i<arrTitle.length; i++){
                    comments+=arrTitle[i].concat(" ");
                }

                String titleMasterData=el.select("td.num").text();
                String[] arr=titleMasterData.split(" ");
                String date=arr[arr.length-1];
                if (!(date.equals("")&&title.equals("")&&comments.equals(""))) {
                    System.out.println(commentId+" || "+date+" || " + title + " || "+ comments);
                }
            }
        }
    }


    public void webCrawlering(String url, Integer lastNumber){
        Document doc=null;

        for(int page=1; page<=LastNumber; page++){

            try {
                doc= Jsoup.connect(URL+page).get();
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

}
