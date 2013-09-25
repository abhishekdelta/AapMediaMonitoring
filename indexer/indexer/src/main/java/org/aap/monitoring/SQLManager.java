package org.aap.monitoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SQLManager {

    private static Logger LOG = LoggerFactory.getLogger(SQLManager.class);
    Connection con = null;
    SolrManager solrManager;
    String url = "jdbc:mysql://66.175.223.5:3306/AAP";
    String user = "root";
    String password = "aapmysql00t";

    public SQLManager() throws SQLException {
        this.solrManager = new SolrManager();
    }

    //yy-mm-dd
    public void triggerIndexer(String dateString) throws SQLException {
        int currSize = -1;
        int start = 0;
        int resultSize = 1000;
        int count=0, failedCount=0;
        while(currSize != 0){
            currSize = 0;
            ResultSet rs = null;
            Statement st = null;
            if(con == null){
                getConn();
            }
            try {
                String query = "SELECT * from ARTICLE_TBL" ;
                if(!StringUtils.isBlank(dateString)){
                    query += " where publishedDate > " + dateString ;
                }
                query += " order by publishedDate limit " + start + ", " + resultSize  + ";";
                st = con.createStatement();
                rs = st.executeQuery(query);
                
                List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
                while (rs.next()) {
                    currSize++;
                    try {
                    	SolrInputDocument solrDoc = solrManager.toSolrDocument(rs);
                    	inputDocuments.add(solrDoc);
                        solrManager.insertDocuments(inputDocuments);
                        count++;
                    } catch (Exception e) {
                        LOG.error("Failed to create document", e);
                        failedCount++;
                    }
                    count++;
                }
                start = start + currSize;
            } catch (SQLException ex) {
                LOG.error(ex.getMessage(), ex);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                } catch (SQLException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        LOG.info("Indexer trigger: indexed successfully: " + count + ", failed: " + failedCount +   " documents for trigger: " + dateString);
    }

    /** Gives list of synonyms including the query itself**/
    public List<String> getSynonyms(final String query) throws SQLException {
        ResultSet rs = null;
        Statement st = null;
        if(con == null){
            getConn();
        }

        String sqlQuery = "select * from AAP_LIST;";
        st = con.createStatement();
        rs = st.executeQuery(sqlQuery);
        while (rs.next()) {
            try {
                String name = rs.getString("name");
                String synonymString = rs.getString("synonyms");
                if (!StringUtils.isBlank(synonymString)) {
                    String[] synonymsArray = synonymString.split(",");
                    List<String> synonyms = Lists.newArrayList(synonymsArray);
                    if (query.equalsIgnoreCase(name)) {
                        synonyms.add(query);
                        return synonyms;
                    } else {
                        int count = synonyms.size();
                        Iterables.filter(synonyms, new Predicate<String>() {
                            public boolean apply(String arg0) {
                                return !arg0.equalsIgnoreCase(query);
                            }
                        });
                        if (count != synonyms.size()) {
                            synonyms.add(query);
                            return synonyms;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to find synonyms",e);
            }
        }
        return Lists.newArrayList(query);
    }

    public void getConn() throws SQLException {
        con = DriverManager.getConnection(url, user, password);
    }

    public void closeConn() throws SQLException {
        if (con != null) {
            con.close();
        }
    }
}
