/*
 * Copyright (C) 2014 Project finc, finc@ub.uni-leipzig.de
 * Leipzig University Library, Project finc
 * http://www.ub.uni-leipzig.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * http://opensource.org/licenses/gpl-3.0.html GNU General Public License
 * http://finc.info Project finc
 */

package de.unileipzig.ub.finc.bibliotheca.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Several MsSQL database methods.
 * 
 * @author <a href="mailto:tsolakidis@ub.uni-leipzig.de">Polichronis Tsolakidis</a>
 */
public class Db {
    
    private static final Set<String> EXEMPLAR_COLUMNS = new LinkedHashSet(){{
        add("barcode");
        add("status");
        add("branch");
    }};
    private static final Set<String> TITLE_COLUMNS = new LinkedHashSet(){{
        add("recordId");
        add("isbn");
        add("medienstatus");
        add("signatur");
    }};
    
    /**
     * 
     * @param con Database connection
     * @param rids List of Record ids
     * @return Document data
     * @throws java.sql.SQLException 
     */
    public static Map<String,Object> getData( Connection con, String[] rids) throws SQLException {
        
        StringBuilder sb = new StringBuilder();
        for (String rid : rids) {
            sb.append( sb.length() == 0 ? "" : ",").append("?");
        }
        Map<String, Object> rm = new HashMap<>();

        try ( PreparedStatement stm = con.prepareCall(
            "SELECT m.EKZDB_IDENTNR AS recordId," +
                "m.ISBN AS isbn," +
                "m.MEDIENSTATUS AS medienstatus," +
                "m.SIGNATUR AS signatur," +
                "e.ZWEIGSTELLE AS branch, " +
                "e.BUCHUNGSNR AS barcode, " +
                "e.EXEMPLARSTATUS AS status " +
            "FROM MEDIEN m, EXEMPLAR e " +
            "WHERE m.EKZDB_IDENTNR IN(" + sb.toString() + ") " +
                "AND e.MEDIENNREX = m.MEDIENNR"
        ); ) {
            
            for( int i = 0; i < rids.length; i++ )
                stm.setString(i+1, rids[i]);
            try ( ResultSet rs = stm.executeQuery(); ) {
        
                ArrayList<String> cl = null;
                HashMap<String, Map<String,Object>> map = new HashMap<>();
        
                while( rs.next() ) {
                    String recordId = rs.getString("recordId");
                    Map<String, Object> ridEx = map.get(recordId);
                    if( ridEx == null ) {
                        ridEx = new HashMap<>();
                        map.put(recordId, ridEx);
                    }
                    List<Map<String,Object>> exList = (List<Map<String,Object>>) ridEx.get("exemplars");
                    if( exList == null ) {
                        exList = new ArrayList<>();
                        ridEx.put( "exemplars", exList);
                        for( String s : TITLE_COLUMNS ) {
                            ridEx.put( s, rs.getObject(s));
                        }
                    }
                    if( cl == null ) {
                        ResultSetMetaData md = rs.getMetaData();
                        int cc = md.getColumnCount();
                        cl = new ArrayList<>();
                        for( int i = 0; i < cc; i++)
                            cl.add( md.getColumnLabel( i + 1));
                    }
                    HashMap<String, Object> exemplar = new HashMap<>();
                    for( String label : EXEMPLAR_COLUMNS ) {
                        exemplar.put( label, rs.getObject(label) );
                    }
                    exList.add(exemplar);
                }
                rm.put("errorcode", new Integer(0));
                rm.put("count", map.size());
                rm.put("docs", map);
                return rm;
            }
        }
    }
}
