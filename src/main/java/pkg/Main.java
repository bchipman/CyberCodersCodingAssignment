package pkg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Main {

   private static final Logger LOG = LoggerFactory.getLogger(Main.class);

   public static void main(final String[] args) {
      LOG.debug("Main.main()  args: " + Arrays.toString(args));
      final Crawler crawler = new Crawler();
      crawler.printStartingLinks();
   }
}
