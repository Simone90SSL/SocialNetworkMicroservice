package data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.mongodb.TweetDocument;
import sample.data.neo4j.FollowingData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by simonecaldaro on 11/09/2017.
 */
@SpringBootApplication
@ComponentScan({"data", "repository", "transaction", "controller"})
@EntityScan({ "sample.data.neo4j", "BOOT-INF.classes.sample.data.neo4j" })
@EnableNeo4jRepositories(basePackages = "repository.neo4j")
@EnableMongoRepositories(basePackages = "repository.mongodb")
@EnableTransactionManagement
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private TweetDocumentRepository tweetDocumentRepository;
    @Autowired
    private UserNodeRepository userNodeRepository;
    @Autowired
    private HashTagNodeRepository hashTagNodeRepository;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            /*
            String fileName = "/Users/simonecaldaro/LAUREA_MAGISTRALE_IN_INGEGNERIA_INFORMATICA/Tesi/SNBRS/tmp.txt";
            //read file into stream, try-with-resources
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

                stream.forEach(line -> {
                    long a = Long.parseLong(line.split(",")[0]);
                    long b = Long.parseLong(line.split(",")[1]);
                    userNodeRepository.deleteFollow(a, b);
                    userNodeRepository.addFollow(a, b);
                    ;});

            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            String fileName = "/Users/simonecaldaro/LAUREA_MAGISTRALE_IN_INGEGNERIA_INFORMATICA/Tesi/SNBRS/tmp2.txt";
            //read file into stream, try-with-resources
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

                stream.forEach(line -> {
                    System.out.println(line);
                    long a = Long.parseLong(line.split(",")[0]);
                    String b = line.split(",")[1];
                    int c = Integer.parseInt(line.split(",")[2]);
                    userNodeRepository.deleteTags(a, b);
                    userNodeRepository.addTags(a, b, c);
                    ;});

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
