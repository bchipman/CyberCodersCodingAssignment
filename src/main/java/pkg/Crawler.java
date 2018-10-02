package pkg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * @author Brian Chipman
 * This class implements the actual web crawler.
 */
public class Crawler {

   private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

   private static final JsonParser JSON_PARSER = new JsonParser();

   private String startingPointUrl;

   protected Queue<String> linksQueue = new UniqueQueue<>();

   protected Set<String> visitedLinks = new HashSet<>();

   protected final CrawlerStats crawlerStats = new CrawlerStats();

   public Crawler(final String startingPointUrl) {
      this.startingPointUrl = startingPointUrl;
      getStartingLinks();
   }

   /**
    * This method obtains the JSON data found at the starting point URL and adds each URL to the linksQueue object.
    * Note that since linksQueue is a custom UniqueQueue object, only unique URLs will be added; duplicate URLs will
    * be ignored.
    */
   private void getStartingLinks() {
      try {
         final String jsonText = getJsonStringFromUrl(startingPointUrl);
         final JsonObject jsonObject = JSON_PARSER.parse(jsonText).getAsJsonObject();
         final JsonArray jsonLinks = jsonObject.getAsJsonArray("links");
         for (final JsonElement jsonLink : jsonLinks) {
            linksQueue.add(jsonLink.getAsString());
         }
      }
      catch (IOException e) {
         LOG.error("Exception thrown when trying to get initial links from JSON URL", e);
      }
   }

   /**
    * This method iterates through all links in linksQueue, finding all links on a page and adding them back to
    * linksQueue if they have not been seen before and are not already in linksQueue.  Crawler stats are incremented
    * appropriately depending on if the link was able to be visited or not.
    */
   public void crawl() {
      final long startTimeMillis = System.currentTimeMillis();
      while (!linksQueue.isEmpty()) {
         final String link = linksQueue.remove();
         try {
            visitedLinks.add(link);

            final Elements linkElements = getLinkElementsFromUrl(link);
            LOG.debug("Successfully crawled to this link: " + link);
            crawlerStats.increment(true);
            addPageLinksToQueue(linkElements, link);
         }
         catch (final Exception e) {
            String logMessage = "Failed to crawl to this link (skipping and continuing): " + link;
            crawlerStats.increment(false);
            if (e instanceof HttpStatusException) {
               logMessage += " status code: " + ((HttpStatusException) e).getStatusCode();
               LOG.debug(logMessage);
            }
            else {
               LOG.warn(logMessage, e);
            }
         }
      }
      crawlerStats.setCrawlTimeMillis(System.currentTimeMillis() - startTimeMillis);
   }

   /**
    * This method iterates through the Elements object and adds any URL to the linksQueue object which is not already
    * in the linksQueue and has not already been visited.
    *
    * @param linkElements Elements object containing all a[href] objects found at the current loation.
    * @param currentLink String representing the current URL location.
    */
   protected void addPageLinksToQueue(final Elements linkElements, final String currentLink) {
      for (final Element linkElement : linkElements) {
         final String newLink = createAbsoluteUrlFromRelative(currentLink, linkElement.attr("href"));
         if (newLink != null && !linksQueue.contains(newLink) && !visitedLinks.contains(newLink)) {
            LOG.trace("Adding this link to queue: " + newLink);
            linksQueue.add(newLink);
         }
      }
   }

   /**
    * This method converts a relative URL to an absolute URL which is necessary for Jsoup to connect with.
    * If relativeUrl is already an absolute URL, relativeUrl will be returned.
    *
    * @param currentUrl String representing the current URL location
    * @param relativeUrl String representing a relative URL
    * @return String representing the absolute URL of the provided relative URL
    */
   protected String createAbsoluteUrlFromRelative(final String currentUrl, final String relativeUrl) {
      try {
         // try to create URL from relative URL.  If successful, relativeUrl is already an absolute URL.
         new URL(relativeUrl);
         return relativeUrl;
      }
      catch (MalformedURLException e) {
         LOG.trace("Unable to create absolute URL from relative URL", e);
      }

      try {
         // construct absolute URL from protocol and host of currentUrl + relativeUrl
         final URL url = new URL(currentUrl);
         return url.getProtocol() + "://" + url.getHost() + relativeUrl;
      }
      catch (MalformedURLException e) {
         LOG.error("Unable to create absolute URL", e);
         return null;
      }
   }

   /**
    * This is a convenience method to print all crawler statistics obtained.
    */
   public void printStats() {
      LOG.info(crawlerStats.toString());
   }

   /**
    * This method uses Jsoup to connect to the provided URL and return a string representation of the JSON object
    * located at the URL.
    * This code was extracted in order to be overriden in unit tests to make testing easier.
    *
    * @param url The URL for the JSON object
    * @return string representation of the JSON found at the URL
    * @throws IOException if fails to get the JSON string from the URL
    */
   protected String getJsonStringFromUrl(final String url) throws IOException {
      return Jsoup.connect(url).ignoreContentType(true).execute().body();
   }

   /**
    * This method uses Jsoup to connect to the provided URL and return a Jsoup Elements object containing all a[href]
    * objects found at the URL.
    * This code was extracted in order to be overriden in unit tests to make testing easier.
    *
    * @param url The URL to navigate to.
    * @return Jsoup Elements object containing all found a[href] objects (i.e. links).
    * @throws IOException
    */
   protected Elements getLinkElementsFromUrl(final String url) throws IOException {
      final Document document = Jsoup.connect(url).get();
      return document.select("a[href]");
   }

}
