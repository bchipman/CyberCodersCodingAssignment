package pkg;

public class Main {

   private static final String URL_STARTING_POINT = "https://raw.githubusercontent.com/OnAssignment/compass-interview/master/data.json";

   public static void main(final String[] args) {
      final Crawler crawler = new Crawler(URL_STARTING_POINT);
      crawler.printStartingLinks();
      crawler.crawl();
      crawler.printStats();
   }
}
