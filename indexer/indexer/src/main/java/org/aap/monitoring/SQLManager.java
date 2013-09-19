package org.aap.monitoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLManager {

	private static Logger LOG = LoggerFactory.getLogger(SQLManager.class);
    Connection con = null;
    SolrManager solrManager;
    String url = "jdbc:mysql://66.175.223.5:3306/AAP";
    String user = "root";
    String password = "aapmysql00t";

    public SQLManager(SolrManager solrManager) throws SQLException {
        this.solrManager = solrManager;
    }

    
    //yy-mm-dd
    public void triggerIndexer(String dateString) throws SQLException {
    	if(con == null){
    		getConn();
    	}
        ResultSet rs = null;
        Statement st = null;
        try {
        	int count=0, failedCount=0;
        	String query = "SELECT * from ARTICLE_TBL" ;
        	if(!StringUtils.isBlank(dateString)){
        		query += " where publishedDate > " + dateString ;
        	}
        	query += ";";
        	
        	LOG.info(query);
            st = con.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                try {
					this.solrManager.insertDocument(rs);
					count++;
				} catch (Exception e) {
					LOG.error("Failed to index document");
					failedCount++;
				}
                count++;
            }
            LOG.info("Indexer trigger: indexed successfully: " + count + ", failed: " + failedCount +   " documents for trigger: " + dateString);
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
                LOG.info(ex.getMessage(), ex);
            }
        }
    }
    
    public void getConn() throws SQLException {
    	con = DriverManager.getConnection(url, user, password);
    }
    public void closeConn() throws SQLException{
    	if (con != null) {
            con.close();
        }
    }
}
