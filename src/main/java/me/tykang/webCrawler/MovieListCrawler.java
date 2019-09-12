package me.tykang.webCrawler;

import me.tykang.webCrawler.domain.MovieInfo;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MovieListCrawler {

    private final static Logger log = LoggerFactory.getLogger(MovieListCrawler.class);
    private String MovieListURL;
    private final String timeFormat="yyyy.MM.dd";

    public MovieListCrawler(String movieListURL){
        this.MovieListURL=movieListURL;
    }

    public ArrayList<MovieInfo> getMovieList(){

        Document doc=null;

        try {
            doc= Jsoup.connect(MovieListURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements elements = doc.select("ul.lst_detail_t1");

        ArrayList<MovieInfo> movieInfoList=new ArrayList<>();

        SimpleDateFormat dt=new SimpleDateFormat(timeFormat);

        for(Element el : elements.select("li")){

            String title=el.select("dt.tit").select("a").text();

            String webMovieInfo=el.select("dl.info_txt1").text();
            webMovieInfo=webMovieInfo.replace("개봉 감독","|");

            String[] movieInfoArr=webMovieInfo.split("\\|");
            String sReleaseDate=movieInfoArr[2].trim();
            Date releaseDate= null;
            try {
                releaseDate = dt.parse(sReleaseDate);
                Date toDay = new Date();
                Date toDate = DateUtils.truncate(toDay, Calendar.DATE);
                if(releaseDate.getMonth() < toDate.getMonth()){
                    continue;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            MovieInfo movieInfo=new MovieInfo();
            movieInfo.setTitle(title);
            movieInfo.setReleaseDate(releaseDate);
            movieInfoList.add(movieInfo);
        }

        return movieInfoList;


    }


}
