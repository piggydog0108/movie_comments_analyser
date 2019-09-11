package me.tykang.webCrawler;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);
    private static final String URL = "url";
    private static final String LOG_TIME_FORMAT = "timeFormat";
    private static final String KAFKA_PRODUCER_CONF = "kafkaProducerConf";
    private static final String LOG_PATH = "log4jConf";
    private static final String LAST_PAGE_NUMBER="lastPageNumber";
    private static final Options OPTIONS = new Options();

    public static void main(String[] args) {

        if (args.length <= 0) {
            args = new String[]{
                    "-url", "https://movie.naver.com/movie/point/af/list.nhn?&page=",
                    "-timeFormat", "yyyy-MM-dd_HHmmss.SSS",
                    "-kafkaProducerConf", "/Users/tykang/Dev/git/webCrawler/src/main/resources/producer.properties",
                    "-log4jConf", "/Users/tykang/Dev/git/webCrawler/src/main/resources/log4j.properties",
                    "-lastPageNumber","1"
            };
        }

        CommandLine commandLine = parseCommandLine(args);

        String url = commandLine.getOptionValue(URL);
        String timeFormat = commandLine.getOptionValue(LOG_TIME_FORMAT);
        String producerConfigPath = commandLine.getOptionValue(KAFKA_PRODUCER_CONF);
        String logConfigPath = commandLine.getOptionValue(LOG_PATH);
        String lastPageNumber=commandLine.getOptionValue(LAST_PAGE_NUMBER);

        Properties logProperties = new Properties();
        try {
            logProperties.load(new FileInputStream(logConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PropertyConfigurator.configure(logProperties);
        log.debug("{} file configured.", logConfigPath);

        Integer lastPageNum=Integer.parseInt(lastPageNumber);

//        Timer timer = new Timer("Timer-thread", false);
//        WebCrawler webCrawler=new WebCrawler(url, lastPageNum);
//        timer.scheduleAtFixedRate(webCrawler,1000, 1000 * 60);

        WebCrawler webCrawler = new WebCrawler(url, lastPageNum);
        webCrawler.webCrawlering(url,lastPageNum);

    }


    private static CommandLine parseCommandLine(String[] args) {

        Option url = new Option (URL, true, "comments url");
        Option logTimeFormat = new Option(LOG_TIME_FORMAT, true, "log time format");
        Option producerConfig = new Option(KAFKA_PRODUCER_CONF, true, "kafka producer config path");
        Option logConfig = new Option(LOG_PATH, true, "log config path");
        Option lastPageNumber=new Option(LAST_PAGE_NUMBER,true, "last page of movie comments");

        OPTIONS.addOption(url)
                .addOption(logTimeFormat)
                .addOption(producerConfig)
                .addOption(logConfig)
                .addOption(lastPageNumber);

        if (args.length < 5) {
            printUsageAndExit();
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(OPTIONS, args);
        } catch (ParseException | NumberFormatException e) {
            log.error(e.getMessage(), e);
            printUsageAndExit();
        }
        return cmdLine;
    }

    private static void printUsageAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("webCrawler", OPTIONS);
        System.exit(1);
    }


}
