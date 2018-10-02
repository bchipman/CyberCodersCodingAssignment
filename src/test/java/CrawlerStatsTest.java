import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pkg.CrawlerStats;

/**
 * @author Brian Chipman
 *
 * This class contains unit tests for {@link CrawlerStats} class.
 */
public class CrawlerStatsTest {

   private CrawlerStats crawlerStats;

   @Before
   public void before() {
      crawlerStats = new CrawlerStats();
   }

   @Test
   public void testIncrement_01() {
      Assert.assertEquals(0, crawlerStats.getSuccessfulRequestCount());
      Assert.assertEquals(0, crawlerStats.getFailedRequestCount());
      Assert.assertEquals(0, crawlerStats.getTotalRequestCount());
      crawlerStats.increment(true);
      crawlerStats.increment(false);
      crawlerStats.increment(true);
      Assert.assertEquals(2, crawlerStats.getSuccessfulRequestCount());
      Assert.assertEquals(1, crawlerStats.getFailedRequestCount());
      Assert.assertEquals(3, crawlerStats.getTotalRequestCount());
   }
}
