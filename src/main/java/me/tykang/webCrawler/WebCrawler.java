package me.tykang.webCrawler;

import me.tykang.webCrawler.domain.CommentInfo;
import me.tykang.webCrawler.domain.MovieInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class WebCrawler extends TimerTask {

    private final static Logger log = LoggerFactory.getLogger(WebCrawler.class);
    private String URL;
    private Integer LastNumber;
    private ArrayList<MovieInfo> MOVIE_INFO_LIST;
    private final String TimeFormat="yyyy.MM.dd";

    public WebCrawler(String url, Integer lastNumber, ArrayList<MovieInfo> movieInfoList){
        this.URL=url;
        this.LastNumber=lastNumber;
        this.MOVIE_INFO_LIST=movieInfoList;
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


    public ArrayList<CommentInfo> webCrawlering(){
        Document doc=null;
        SimpleDateFormat dt=new SimpleDateFormat(TimeFormat);

        ArrayList<CommentInfo> commentInfoList=new ArrayList<>();
        for(int page=1; page<=LastNumber; page++){

            try {
                doc= Jsoup.connect(URL+page).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements elements = doc.select("div.article").select("tr");

            int i=1;
            CommentInfo commentInfo=new CommentInfo();
            for(Element el : elements.select("td")){

                if (el.text().equals("")){
                    continue;
                }

                if(i%4==1){
                    commentInfo.setId(Long.parseLong(el.text()));
                }else if(i%4==2){
                    commentInfo.setScore(Double.parseDouble(el.text()));
                }else if(i%4==3) {
                    for(MovieInfo movieInfo : MOVIE_INFO_LIST){
                        if (el.text().contains(movieInfo.getTitle())){
                            String comment= el.text();
                            comment=comment.substring(movieInfo.getTitle().length(),el.text().indexOf("신고"));
                            commentInfo.setTitile(movieInfo.getTitle());
                            commentInfo.setComment(comment);
//                            System.out.println(movieInfo.getTitle()+" | "+comment);

                        }else{
                            continue;
                        }
                    }
                    commentInfo.setCommentWithTitle(el.text());
                }else{
                    String[] writerAndDate=el.text().split(" ");
                    commentInfo.setWriter(writerAndDate[0]);
                    Date commentDate=null;
                    try {
                        commentDate=dt.parse(writerAndDate[1]);
                        commentInfo.setCommentDate(commentDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    commentInfoList.add(commentInfo);
                    commentInfo=new CommentInfo();
                }

                i++;
            }
        }
        return commentInfoList;
    }

}
