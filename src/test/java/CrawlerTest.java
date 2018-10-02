import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pkg.Crawler;
import pkg.CrawlerStats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author Brian Chipman
 *
 * This class contains unit tests for {@link Crawler} class.
 */
public class CrawlerTest {

   private static final Logger LOG = LoggerFactory.getLogger(CrawlerTest.class);

   private static JsonObject STARTING_LINKS_JSON;

   private static final String URL_1 = "http://www.test.com/1";

   private static final String URL_2 = "http://www.test.com/2";

   private static final String URL_3 = "http://www.test.com/3";

   private static final String URL_4 = "http://www.test.com/abc/4";

   private static final String URL_5 = "http://www.test.com/abc/5";

   private static final String URL_6 = "http://www.test.com/abc/6";

   private static final String REL_URL_4 = "/abc/4";

   private static final String REL_URL_5 = "/abc/5";

   private static final String REL_URL_6 = "/abc/6";

   private static class MockCrawler extends Crawler {
      public MockCrawler(final String startingPointUrl) {
         super(startingPointUrl);
      }

      @Override
      protected String getJsonStringFromUrl(final String url) {
         STARTING_LINKS_JSON = new JsonObject();
         final JsonArray jsonArray = new JsonArray();
         jsonArray.add(URL_1);
         jsonArray.add(URL_2);
         jsonArray.add(URL_3);
         jsonArray.add(URL_3);
         jsonArray.add("badlink1");
         jsonArray.add("badlink2");
         jsonArray.add("badlink2");
         STARTING_LINKS_JSON.add("links", jsonArray);
         return STARTING_LINKS_JSON.toString();
      }

      @Override
      protected Elements getLinkElementsFromUrl(final String url) throws HttpStatusException {
         final Elements elements = LINK_TO_ELEMENTS_MAP.get(url);
         if (elements != null) {
            return elements;
         }
         throw new HttpStatusException("Bad link!", 500, url);
      }

      @Override
      public void addPageLinksToQueue(final Elements linkElements, final String currentLink) {
         super.addPageLinksToQueue(linkElements, currentLink);
      }

      @Override
      public String createAbsoluteUrlFromRelative(final String currentUrl, final String relativeUrl) {
         return super.createAbsoluteUrlFromRelative(currentUrl, relativeUrl);
      }

      public Queue<String> getLinksQueue() {
         return linksQueue;
      }

      public Set<String> getVisitedLinks() {
         return visitedLinks;
      }

      public void setVisitedLinks(final Set<String> visitedLinks) {
         this.visitedLinks = visitedLinks;
      }

      public CrawlerStats getCrawlerStats() {
         return crawlerStats;
      }
   }

   private static Map<String, Elements> LINK_TO_ELEMENTS_MAP = new HashMap<>();

   private MockCrawler crawler;

   /**
    * This method constructs a Map of URLs to Elements used to help mock the {@link Crawler} class for testing.
    */
   @BeforeClass
   public static void beforeClass() {
      addElementsToMapHelper(URL_1, REL_URL_4);
      addElementsToMapHelper(URL_2, REL_URL_4, REL_URL_5);
      addElementsToMapHelper(URL_3, REL_URL_5, REL_URL_6);
      addElementsToMapHelper(URL_4, REL_URL_4, REL_URL_5);
      addElementsToMapHelper(URL_5, REL_URL_4, REL_URL_6);
      addElementsToMapHelper(URL_6, REL_URL_4, REL_URL_5, REL_URL_6);
      // 1 -> 4
      // 2 -> 4, 5
      // 3 -> 5, 6
      // 4 -> 4, 5
      // 5 -> 4, 6
      // 6 -> 4, 5, 6
   }

   /**
    * This is a helper method for {@link #beforeClass} to help construct a Map of URLs to Elements used to help mock
    * the {@link Crawler} class for testing.
    *
    * @param pageUrl String representing current page URL.
    * @param urlsOnPage String[] containing all links found on the current page.
    */
   private static void addElementsToMapHelper(final String pageUrl, final String... urlsOnPage) {
      final Elements elements = new Elements();
      for (final String urlOnPage : urlsOnPage) {
         final Attributes attributes = new Attributes();
         attributes.put("href", urlOnPage);
         final Element element = new Element(Tag.valueOf("a"), pageUrl, attributes);
         elements.add(element);
      }
      LINK_TO_ELEMENTS_MAP.put(pageUrl, elements);
   }

   /**
    * Create a fresh {@link MockCrawler} object before each test is run.
    */
   @Before
   public void before() {
      crawler = new MockCrawler(null);
   }

   /**
    * Tests the {@link Crawler#getStartingLinks()} method by confirming the links in the STARTING_LINKS_JSON are the
    * same as in linksQueue.
    */
   @Test
   public void testGetStartingLinks_01() {
      JsonArray links = STARTING_LINKS_JSON.getAsJsonArray("links");
      Set<String> linksSet = new HashSet<>();
      for (JsonElement link : links) {
         linksSet.add(link.getAsString());
      }
      Assert.assertTrue(linksSet.size() < links.size());
      Assert.assertEquals(linksSet.size(), crawler.getLinksQueue().size());
   }

   /**
    * Tests the {@link Crawler#getStartingLinks()} method by confirming no duplicate links exist in the linksQueue
    * object.
    */
   @Test
   public void testGetStartingLinks_02() {
      final Set<String> seenLinks = new HashSet<>();
      for (final String link : crawler.getLinksQueue()) {
         Assert.assertTrue(seenLinks.add(link));
      }
   }

   /**
    * Tests the {@link Crawler#addPageLinksToQueue(Elements, String)} method by adding some URLs and making sure the
    * linksQueue object is updated appropriately.
    */
   @Test
   public void testAddPageLinksToQueue_01() {

      // confirm there are links in linksQueue and no links in visitedLinks
      final int startLinksQueueSize = crawler.getLinksQueue().size();
      Assert.assertTrue(startLinksQueueSize > 0);
      Assert.assertEquals(0, crawler.getVisitedLinks().size());

      // try to add URL_4 and URL_5 to linksQueue
      crawler.addPageLinksToQueue(LINK_TO_ELEMENTS_MAP.get(URL_1), URL_1);
      crawler.addPageLinksToQueue(LINK_TO_ELEMENTS_MAP.get(URL_4), URL_4);

      // confirm URL_4 and URL_5 were added
      Assert.assertTrue(crawler.getLinksQueue().contains(URL_4));
      Assert.assertTrue(crawler.getLinksQueue().contains(URL_5));

      // confirm URL_6 was not added
      Assert.assertFalse(crawler.getLinksQueue().contains(URL_6));

      // confirm visitedLinks didn't change
      Assert.assertEquals(0, crawler.getVisitedLinks().size());

      // confirm linksQueue size increased
      Assert.assertTrue(crawler.getLinksQueue().size() > startLinksQueueSize);
   }

   /**
    * Tests the {@link Crawler#addPageLinksToQueue(Elements, String)} method by adding some URLs and making sure the
    * linksQueue object is updated appropriately.
    */
   @Test
   public void testAddPageLinksToQueue_02() {

      // confirm there are links in linksQueue and no links in visitedLinks
      int startLinksQueueSize = crawler.getLinksQueue().size();
      Assert.assertTrue(startLinksQueueSize > 0);
      Assert.assertEquals(0, crawler.getVisitedLinks().size());

      // set visited links to include URL_4 and URL_5
      crawler.setVisitedLinks(new HashSet<>(Arrays.asList(URL_4, URL_5)));
      Assert.assertEquals(2, crawler.getVisitedLinks().size());

      // try to add URL_4, URL_5, and URL_6 to linksQueue
      crawler.addPageLinksToQueue(LINK_TO_ELEMENTS_MAP.get(URL_1), URL_1);
      crawler.addPageLinksToQueue(LINK_TO_ELEMENTS_MAP.get(URL_4), URL_4);
      crawler.addPageLinksToQueue(LINK_TO_ELEMENTS_MAP.get(URL_5), URL_5);

      // confirm URL_4 and URL_5 were not added
      Assert.assertFalse(crawler.getLinksQueue().contains(URL_4));
      Assert.assertFalse(crawler.getLinksQueue().contains(URL_5));

      // confirm URL_6 was added
      Assert.assertTrue(crawler.getLinksQueue().contains(URL_6));
   }

   /**
    * Tests the {@link Crawler#createAbsoluteUrlFromRelative(String, String)} using relative URLs.
    */
   @Test
   public void testCreateAbsoluteUrlFromRelative_01() {
      Assert.assertEquals(URL_4, crawler.createAbsoluteUrlFromRelative(URL_1, REL_URL_4));
      Assert.assertEquals(URL_5, crawler.createAbsoluteUrlFromRelative(URL_2, REL_URL_5));
      Assert.assertEquals(URL_6, crawler.createAbsoluteUrlFromRelative(URL_3, REL_URL_6));
      Assert.assertEquals("http://www.google.com/testing/123",
          crawler.createAbsoluteUrlFromRelative("http://www.google.com", "/testing/123"));
      Assert.assertNull(crawler.createAbsoluteUrlFromRelative("/bad/url", REL_URL_4));
   }

   /**
    * Tests the {@link Crawler#createAbsoluteUrlFromRelative(String, String)} using absolute URLs.
    */
   @Test
   public void testCreateAbsoluteUrlFromRelative_02() {
      Assert.assertEquals(URL_4, crawler.createAbsoluteUrlFromRelative(URL_1, URL_4));
      Assert.assertEquals(URL_5, crawler.createAbsoluteUrlFromRelative(URL_2, URL_5));
      Assert.assertEquals(URL_6, crawler.createAbsoluteUrlFromRelative(URL_3, URL_6));
      Assert.assertEquals("http://www.google.com/testing/123",
          crawler.createAbsoluteUrlFromRelative("http://www.google.com", "http://www.google.com/testing/123"));
      Assert.assertNotNull(crawler.createAbsoluteUrlFromRelative("/bad/url", URL_4));
   }


   @Test
   public void testCrawl_01() {

      // confirm no crawler stats exist at start
      final CrawlerStats crawlerStats = crawler.getCrawlerStats();

      Assert.assertEquals(0, crawlerStats.getSuccessfulRequestCount());
      Assert.assertEquals(0, crawlerStats.getFailedRequestCount());
      Assert.assertEquals(0, crawlerStats.getTotalRequestCount());
      Assert.assertEquals(0L, crawlerStats.getCrawlTimeMillis());

      // confirm linksQueue not empty at start
      Assert.assertFalse(crawler.getLinksQueue().isEmpty());

      // confirm visited empty at start
      Assert.assertTrue(crawler.getVisitedLinks().isEmpty());

      // perform crawl
      crawler.crawl();

      // confirm linksQueue is empty at end
      Assert.assertTrue(crawler.getLinksQueue().isEmpty());

      // confirm visited empty not empty at end
      Assert.assertFalse(crawler.getVisitedLinks().isEmpty());

      // confirm crawler stats exist at end
      Assert.assertTrue(crawlerStats.getSuccessfulRequestCount() > 0);
      Assert.assertTrue(crawlerStats.getFailedRequestCount() > 0);
      Assert.assertTrue(crawlerStats.getTotalRequestCount() > 0);
      Assert.assertTrue(crawlerStats.getCrawlTimeMillis() > 0L);

      // confirm successful requests + failed requests = total requests
      Assert.assertEquals(crawlerStats.getTotalRequestCount(),
          crawlerStats.getSuccessfulRequestCount() + crawlerStats.getFailedRequestCount());
   }
}
