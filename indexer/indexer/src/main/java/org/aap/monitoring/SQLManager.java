package org.aap.monitoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SQLManager {

    Connection con = null;
    SolrManager solrManager;

    public SQLManager(SolrManager solrManager) throws SQLException {
        //String url = "jdbc:mysql://in-cia-dev00.in.walmartlabs.com:3306/hjp";
        String url = "jdbc:mysql://66.175.223.5:3306/AAP";
        String user = "root";
        String password = "aapmysql00t";
        con = DriverManager.getConnection(url, user, password);
        this.solrManager = solrManager;
    }

    //yy-mm-dd
    public void triggerIndexer(String dateString) {
        ResultSet rs = null;
        Statement st = null;
        try {
            st = con.createStatement();
            rs = st.executeQuery("SELECT * from ARTICLE_TBL where publishedDate=" + dateString + ";");
            while (rs.next()) {
                this.solrManager.insertDocument(rs);
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(this.getClass().getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(this.getClass().getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}