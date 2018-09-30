package pkg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Crawler {

   private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

   private static final String URL_STARTING_POINT = "https://raw.githubusercontent.com/OnAssignment/compass-interview/master/data.json";

   private final Set<String> links;

   public Crawler() {
      links = getStartingLinks();
   }

   private Set<String> getStartingLinks() {
      final Set<String> links = new LinkedHashSet<>();
      try {
         final String jsonText = Jsoup.connect(URL_STARTING_POINT).ignoreContentType(true).execute().body();
         final JsonParser jsonParser = new JsonParser();
         final JsonObject jsonObject = jsonParser.parse(jsonText).getAsJsonObject();
         final JsonArray jsonLinks = jsonObject.getAsJsonArray("links");
         for (JsonElement jsonLink : jsonLinks) {
            links.add(jsonLink.getAsString());
         }
      }
      catch (IOException e) {
         LOG.error("Exception thrown when trying to get initial links", e);
      }
      return links;
   }

   public void printStartingLinks() {
      for (String link : links) {
         LOG.debug(link);
      }
   }
}
