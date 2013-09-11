package org.aap.monitoring;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SolrManager {
    public static SolrServer solrServer = new HttpSolrServer("http://localhost:8983/solr");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    public void insertDocument(ResultSet result) {
        try {
            SolrInputDocument inputDocument = new SolrInputDocument();
            inputDocument.addField("src", result.getString("src"));
            inputDocument.addField("url", result.getString("url"));
            inputDocument.addField("title", result.getString("title"));
            inputDocument.addField("date", result.getDate("publishedDate"));
            inputDocument.addField("image_url", result.getString("imageUrl"));
            inputDocument.addField("content", result.getString("content"));
            inputDocument.addField("author", result.getString("author"));
            inputDocument.addField("category", result.getString("category"));
            inputDocument.addField("comments", result.getString("comments"));
            inputDocument.addField("country", result.getString("country"));
            inputDocument.addField("city", result.getString("city"));
            inputDocument.addField("commentcount", result.getInt("commentCount"));
            solrServer.add(inputDocument);
            solrServer.commit();
        } catch (Exception e) {
            System.err.println("Exception aa gayi baap. Ab kya hoga ??");
            e.printStackTrace();
        }
    }

    public void insertFraudyDocument(String url, String content, Date date) {
        try {
            SolrInputDocument inputDocument = new SolrInputDocument();
            inputDocument.addField("src", "dummysrc");
            inputDocument.addField("url", url);
            inputDocument.addField("title", "dummytitle");
            inputDocument.addField("date", date);
            inputDocument.addField("image_url", "dummy_image_url");
            inputDocument.addField("content", content);
            inputDocument.addField("author", "dummy_author");
            inputDocument.addField("category", "dummy_category");
            inputDocument.addField("comments", "dummy_comments");
            inputDocument.addField("country", "dummy_country");
            inputDocument.addField("city", "dummy_city");
            inputDocument.addField("commentcount", 55);
            solrServer.add(inputDocument);
            solrServer.commit();
            System.out.println("Added a doc");
        } catch (Exception e) {
            System.err.println("Exception aa gayi baap. Ab kya hoga ??");
            e.printStackTrace();
        }
    }


    private SolrQuery getQueryForKeywords(String keywords) {
        return new SolrQuery().setQuery("content:" + keywords);
    }

    private SolrQuery getQueryForKeywords(String keywords, Date startDate, Date endDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateQuery = "date:" + "[" + df.format(startDate) + " TO " + df.format(endDate) + "]";
        return new SolrQuery().addFilterQuery(dateQuery).setQuery("content:" + keywords);
    }

    public List<Article> getArticlesForKeywords(String keywords) throws SolrServerException {
        QueryResponse response = solrServer.query(getQueryForKeywords(keywords));
        return getArticles(response);
    }

    public List<Article> getArticlesForKeywords(String keywords, Date startDate, Date endDate) throws SolrServerException {
        QueryResponse response = solrServer.query(getQueryForKeywords(keywords, startDate, endDate));
        return getArticles(response);
    }

    private List<Article> getArticles(QueryResponse response) {
        List<Article> result = new ArrayList<Article>();
        SolrDocumentList object = (SolrDocumentList) response.getResponse().get("response");
        for (SolrDocument doc : object) {
            Article article = Article.getArticleFrom(doc);
            result.add(article);
        }
        return result;
    }

    public Map<String, Map<String, Integer>> getNumArticlesForKeywordsAndDate(String keywords, Date start, Date end) throws SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(keywords);
        solrQuery.setFacet(true);
        solrQuery.setRows(0);

        solrQuery.set("facet.method", "enum");
        solrQuery.set("facet.limit", "-1");
        solrQuery.addFacetPivotField("date,source");
        solrQuery.addFacetPivotField("source,date");

        QueryResponse response = solrServer.query(solrQuery);
        NamedList<List<PivotField>> pivots = response.getFacetPivot();
        List<PivotField> dateSource = pivots.get("date,source");

        Map<String, Map<String, Integer>> results = new HashMap<String, Map<String, Integer>>();
        for (PivotField dateOnly : dateSource) {
            String dateString = (String) dateOnly.getValue();
            if (results.get(dateString) != null ) results.put(dateString, new HashMap<String, Integer>());
            for (PivotField source: dateOnly.getPivot()) {
                results.get(dateString).put((String) source.getValue(), source.getCount());
            }
        }
        /*
        List<PivotField> sourceDate = pivots.get("source, date");
        for (PivotField sourceOnly : sourceDate) {
            String sourceString = (String) sourceOnly.getValue();
            if (results.get(sourceString) != null ) results.put(sourceString, new HashMap<String, Integer>());
            for (PivotField date: sourceOnly.getPivot()) {
                results.get(sourceString).put((String) date.getValue(), date.getCount());
            }
        }*/

        return results;
    }
}
