package me.tykang.webCrawler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.tykang.webCrawler.domain.CommentInfo;
import me.tykang.webCrawler.domain.MovieInfo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WebCrawler extends TimerTask {

    private final static Logger log = LoggerFactory.getLogger(WebCrawler.class);
    private String URL;
    private Integer LastNumber;
    private ArrayList<MovieInfo> MOVIE_INFO_LIST;
    private final String TimeFormat="yyyy.MM.dd";

    public static Long LastCommentId=null;
    private final KafkaProducer<String, String> KafkaProducer;


    public WebCrawler(String url, Integer lastNumber, ArrayList<MovieInfo> movieInfoList, Long lastCommentId, String producerConfigFilePath){
        this.URL=url;
        this.LastNumber=lastNumber;
        this.MOVIE_INFO_LIST=movieInfoList;
        this.LastCommentId=lastCommentId;

        Properties producerProperties=new Properties();
        try (InputStream propStream = new FileInputStream(producerConfigFilePath)) {
            producerProperties.load(propStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.KafkaProducer = new KafkaProducer<>(producerProperties);
    }

    @Override
    public void run() {
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
                    String info=el.text();
                    commentInfo.setId(Long.parseLong(info));
                }else if(i%4==2){
                    String info=el.text();
                    commentInfo.setScore(Double.parseDouble(info));
                }else if(i%4==3) {
                    for(MovieInfo movieInfo : MOVIE_INFO_LIST){
                        String info=el.text();
                        if (info.contains(movieInfo.getTitle())){
                            String comment= info;
                            comment=comment.substring(movieInfo.getTitle().length(),info.indexOf("신고"));
                            commentInfo.setTitile(movieInfo.getTitle());
                            commentInfo.setComment(comment);
                        }else{
                            continue;
                        }
                    }
                }else{
                    String info=el.text();
                    String[] writerAndDate=info.split(" ");
                    commentInfo.setWriter(writerAndDate[0]);
                    Date commentDate=null;
                    Long lCommentDate=null;
                    try {
                        commentDate=dt.parse(writerAndDate[1]);
                        lCommentDate=commentDate.getTime();
                        commentInfo.setCommentDate(lCommentDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if((LastCommentId==null) || commentInfo.getId()>LastCommentId){
                        commentInfoList.add(commentInfo);
                    }

                    commentInfo=new CommentInfo();
                }

                i++;
            }
        }

        if(commentInfoList.size()!=0){
            LastCommentId=getLastCommentId(commentInfoList);
        }

        ObjectMapper mapper =new ObjectMapper();
        String commentData=null;
        try {
            commentData=mapper.writeValueAsString(commentInfoList);
            System.out.println(commentData);
//            KafkaProducer.send(new ProducerRecord<String, String>("test-topic",commentData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }



    }

    public Long getLastCommentId(ArrayList<CommentInfo> commentInfoList){
        Long lastCommentId=commentInfoList.get(0).getId();
        return lastCommentId;
    }

    public ArrayList<ArrayList<String>> webCrawlering(){
        Document doc=null;
        SimpleDateFormat dt=new SimpleDateFormat(TimeFormat);

        ArrayList<CommentInfo> commentInfoList=new ArrayList<>();
        ArrayList<ArrayList<String>> movieCommentInfoMapList=new ArrayList<>();
        for(int page=1; page<=LastNumber; page++){

            try {
                doc= Jsoup.connect(URL+page).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements elements = doc.select("div.article").select("tr");

            int index=1;
            String info;

            CommentInfo commentInfo=new CommentInfo();

            ArrayList<String> movieCommentInfoList=new ArrayList<>();

            for(Element el : elements.select("td")){

                info=el.text();
                if (info.equals("")){
                    continue;
                }
                System.out.println(info);
                movieCommentInfoList.add(info);


                if(index%4==0){
                    movieCommentInfoMapList.add(movieCommentInfoList);
                    movieCommentInfoList=new ArrayList<>();
                }

                index++;
            }
        }


        ArrayList<Integer> recentMovieIdx=new ArrayList<>();
        for(int i=0; i<MOVIE_INFO_LIST.size(); i++){
            for(int j=0; j<movieCommentInfoMapList.size(); j++){
                if (movieCommentInfoMapList.get(j).get(2).contains(MOVIE_INFO_LIST.get(i).getTitle())){
                    recentMovieIdx.add(j);
                }
            }
        }

        ArrayList<ArrayList<String>> movieInfoList=new ArrayList<>();
        for(int i=0; i<recentMovieIdx.size(); i++){
            movieInfoList.add(movieCommentInfoMapList.get(recentMovieIdx.get(i)));
        }

        for(int i=0; i<movieInfoList.size(); i++){

            CommentInfo commentInfo=new CommentInfo();

            //id
            String sId=movieInfoList.get(i).get(0);
            Long id=Long.parseLong(sId);
            if((LastCommentId==null) || id>LastCommentId){
                commentInfo.setId(id);
            }else{
                continue;
            }

            //score
            String sScore=movieInfoList.get(i).get(1);
            Double score=Double.parseDouble(sScore);
            commentInfo.setScore(score);

            //comment title
            String commentWithTitle=movieInfoList.get(i).get(2);
            for(MovieInfo movieInfo : MOVIE_INFO_LIST){
                if (commentWithTitle.contains(movieInfo.getTitle())){
                    String comment= commentWithTitle;
                    comment=comment.substring(movieInfo.getTitle().length(),commentWithTitle.indexOf("신고"));
                    commentInfo.setTitile(movieInfo.getTitle());
                    commentInfo.setComment(comment);
                }
            }

            //writer and date
            String sWriterAndDate=movieInfoList.get(i).get(3);
            String[] writerAndDate=sWriterAndDate.split(" ");
            commentInfo.setWriter(writerAndDate[0]);
            Date commentDate=null;
            Long lCommentDate=null;
            try {
                commentDate=dt.parse(writerAndDate[1]);
                lCommentDate=commentDate.getTime();
                commentInfo.setCommentDate(lCommentDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            commentInfoList.add(commentInfo);
        }

        ObjectMapper objectMapper=new ObjectMapper();
        String commentInfoJson=null;
        try {
            commentInfoJson=objectMapper.writeValueAsString(commentInfoList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(commentInfoJson);
        return movieInfoList;

    }

}
