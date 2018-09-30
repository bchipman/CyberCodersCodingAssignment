package pkg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Crawler {

   private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

   private final String startingPointUrl;

   private final Queue<String> linksQueue;

   private final Set<String> visitedLinks;

   private final CrawlerStats crawlerStats;

   public Crawler(final String startingPointUrl) {
      this.startingPointUrl = startingPointUrl;
      this.linksQueue = new UniqueQueue<>();
      this.visitedLinks = new HashSet<>();
      crawlerStats = new CrawlerStats();
      getStartingLinks();
   }

   private void getStartingLinks() {
      try {
         final String jsonText = Jsoup.connect(startingPointUrl).ignoreContentType(true).execute().body();
         final JsonParser jsonParser = new JsonParser();
         final JsonObject jsonObject = jsonParser.parse(jsonText).getAsJsonObject();
         final JsonArray jsonLinks = jsonObject.getAsJsonArray("links");
         for (final JsonElement jsonLink : jsonLinks) {
            linksQueue.add(jsonLink.getAsString());
         }
      }
      catch (IOException e) {
         LOG.error("Exception thrown when trying to get initial links from JSON URL", e);
      }
   }

   public void crawl() {
      final long startTimeMillis = System.currentTimeMillis();
      while (!linksQueue.isEmpty()) {
         final String link = linksQueue.remove();
         try {
            if (visitedLinks.contains(link)) {
               LOG.trace("Skipping this link since it was already visited: " + link);
               continue;
            }
            visitedLinks.add(link);

            final Document document = Jsoup.connect(link).get();
            final Elements linkElements = document.select("a[href]");
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

   private void addPageLinksToQueue(final Elements linkElements, final String currentLink) {
      for (final Element linkElement : linkElements) {
         final String newLink = createAbsoluteUrlFromRelative(currentLink, linkElement.attr("href"));
         if (newLink != null && !linksQueue.contains(newLink) && !visitedLinks.contains(newLink)) {
            LOG.trace("Adding this link to queue: " + newLink);
            linksQueue.add(newLink);
         }
      }
   }

   private String createAbsoluteUrlFromRelative(final String currentUrl, final String relativeUrl) {
      try {
         final URL url = new URL(currentUrl);
         return url.getProtocol() + "://" + url.getHost() + relativeUrl;
      }
      catch (MalformedURLException e) {
         LOG.error("Unable to create absolute URL", e);
         return null;
      }
   }

   public void printStartingLinks() {
      for (final String link : linksQueue) {
         LOG.debug(link);
      }
   }

   public void printStats() {
      LOG.info(crawlerStats.toString());
   }
}
