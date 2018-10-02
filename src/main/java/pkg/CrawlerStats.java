package pkg;

import java.util.concurrent.TimeUnit;

/**
 * @author Brian Chipman
 *
 * This class holds the number of successful, failed, and total requests done by {@link Crawler}.
 */
public class CrawlerStats {

   private int successfulRequestCount;

   private int failedRequestCount;

   private int totalRequestCount;

   private long crawlTimeMillis;

   public CrawlerStats() {
   }

   public void increment(final boolean successful) {
      if (successful) {
         successfulRequestCount++;
      }
      else {
         failedRequestCount++;
      }
      totalRequestCount++;
   }

   public int getSuccessfulRequestCount() {
      return successfulRequestCount;
   }

   public int getFailedRequestCount() {
      return failedRequestCount;
   }

   public int getTotalRequestCount() {
      return totalRequestCount;
   }

   public long getCrawlTimeMillis() {
      return crawlTimeMillis;
   }

   public void setCrawlTimeMillis(final long crawlTimeMillis) {
      this.crawlTimeMillis = crawlTimeMillis;
   }

   @Override
   public String toString() {
      return "\n"
          + "  Total number of requests performed:  " + totalRequestCount + "\n"
          + "  Total number of successful requests: " + successfulRequestCount + "\n"
          + "  Total number of failed requests:     " + failedRequestCount + "\n"
          + "  Elapsed time for crawl (seconds):    " + TimeUnit.MILLISECONDS.toSeconds(crawlTimeMillis);
   }
}
