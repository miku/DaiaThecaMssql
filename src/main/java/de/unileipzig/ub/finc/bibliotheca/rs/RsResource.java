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

package de.unileipzig.ub.finc.bibliotheca.rs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.gbv.ws.daia.Availability;
import de.gbv.ws.daia.Daia;
import de.gbv.ws.daia.DaiaAvailability;
import de.gbv.ws.daia.Document;
import de.gbv.ws.daia.Item;
import de.gbv.ws.daia.Label;
import de.gbv.ws.daia.Message;
import de.gbv.ws.daia.SimpleElement;
import de.unileipzig.ub.finc.bibliotheca.config.BibliothecaConfig;
import de.unileipzig.ub.finc.bibliotheca.db.Db;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;

/**
 * REST Web Service
 *
 * @author Polichronis Tsolakidis <tsolakidis@ub.uni-leipzig.de>
 */
@Path("")
public class RsResource {

    private static final Logger logger = Logger.getLogger( RsResource.class );
    
//    @Context
//    private UriInfo context;
    @Context
    private ServletContext context;
    @Context
    private HttpServletResponse    response;
    
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a new instance of RsResource
     */
    public RsResource() {}

    /**
     * Retrieve data for several ppn.
     * 
     * @param isil Institution isil
     * @param recordIds Comma separated list of ppn.
     * @return JSON string data
     * @throws java.io.IOException
     */
    @GET
    @Produces("application/json; charset=utf-8")
    @Path("{isil}/ppn/{recordIds}")
    public String data( @PathParam("isil") String isil, @PathParam("recordIds") String recordIds) throws IOException {
        
        BibliothecaConfig config = getConfig(isil);
        if( config == null ) {
            response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, "Config for isil '" + isil + "' not found.");
            return "";
        }
        String[] rids = recordIds.split(",");
        try ( Connection con = getDbConnection( config.getJdbcJndiName() ); ) {
            Map<String, Object> data = Db.getData( con, rids);
            return gson.toJson(data);
        } catch( Exception e) {
            logger.fatal( "Error while get data", e);
            response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
            
        }
        return null;
    }

    /**
     * Fetches the DAIA document for a ppn.
     * 
     * @param isil Institution isil
     * @param recordId PPN
     * @return JSON string data
     * @throws java.lang.Exception
     */
    @GET
    @Produces("application/xml")
    @Path("{isil}/daia/{ppn}")
    public String daia( @PathParam("isil") String isil, @PathParam("ppn") String recordId) throws Exception {
        
        BibliothecaConfig config = getConfig(isil);
        if( config == null ) return daiaMessage( config, HttpURLConnection.HTTP_INTERNAL_ERROR, "Config for isil '" + isil + "' not found.");
        try ( Connection con = getDbConnection( config.getJdbcJndiName() ); ) {
            
            Map<String, Object> data = Db.getData( con, new String[]{recordId});
            
            if( data != null && !data.isEmpty() ) {
                
                Map<String,Object> docMap = (Map<String,Object>) data.get("docs");
                if( docMap == null || docMap.isEmpty() ) return daiaMessage( config, HttpURLConnection.HTTP_NOT_FOUND, "No document found.");
                
                Document daiaDoc = new Document();
                daiaDoc.setId("finc:" + isil + ":" + recordId);
                daiaDoc.setHref( config.getCatalogueURL() + "Record/" + recordId);

                List<Object> objList = daiaDoc.getMessageOrItem();

                for( Map.Entry<String, Object> e : docMap.entrySet() ) {
                    String ppn = e.getKey();
                    Map<String,Object> ex = (Map<String,Object>) e.getValue();
                    List<Map<String,Object>> exl = (List<Map<String,Object>>) ex.get("exemplars");
                    for( Map<String, Object> get : exl) {
                        Item item = new Item();
                        
                        String barcode = (String) get.get("barcode");
                        Label label = new Label();
                        label.setContent( barcode != null ? barcode : "missing barcode" );
                        item.setLabel( label );
                        
                        item.setId( "finc:" + recordId + ":(" + isil + "):" + get.get("barcode") );
                        item.setHref( config.getCatalogueURL() + "Record/" + recordId );
                        String status = (String) get.get("status");
                        Map<String, Integer> availability = config.getAvailability();
                        List<Availability> list = item.getAvailableOrUnavailable();
                        if( status != null && availability.containsKey(status) && availability.get(status) == 1) {
                            Item.Available available = new Item.Available();
                            available.setService( DaiaAvailability.LOAN.toString() );
                            list.add( available );
                        } else {
                            Item.Unavailable unavailable = new Item.Unavailable();
                            list.add(unavailable);
                        }
                        objList.add(item);
                    }
                }
            
                Daia daia = new Daia();
                String daiaVersion = context.getInitParameter("DAIA_VERSION");
                daia.setVersion( daiaVersion == null ? "0.5" : daiaVersion );

                GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
                XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
                daia.setTimestamp(xgcal);

                SimpleElement se = new SimpleElement();
                se.setContent( config.getInstitutionName() );
                se.setHref( config.getInstitutionURL() );
                daia.setInstitution( se );
                List<Document> docList = daia.getDocument();
                docList.add(daiaDoc);
                JAXBContext jc = JAXBContext.newInstance( Daia.class );
                Marshaller m = jc.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, "http://ws.gbv.de/daia/ http://ws.gbv.de/daia/daia.xsd");
                StringWriter sw = new StringWriter();
                m.marshal( daia, sw);
                
                return sw.toString();
            }
        } catch( Exception e) {
            logger.fatal( "Error while create daia xml", e);
            return daiaMessage( config, HttpURLConnection.HTTP_INTERNAL_ERROR, "Backend error: " + e.getMessage());
        }

        return daiaMessage(config, HttpURLConnection.HTTP_NOT_FOUND, "No documents found");
    }
    
    /**
     * List of defined isils.
     * 
     * @return JSON string data.
     * @throws java.io.IOException
     */
    @GET
    @Produces("application/json; charset=utf-8")
    @Path("isils")
    public String isils() throws IOException {
        String isilsParam = context.getInitParameter("isils");
        if( isilsParam == null ) {
            isilsParam = "";
            response.sendError( HttpURLConnection.HTTP_NOT_FOUND, "List of isils not found. Correct yout config!");
        }
        String[] isils = isilsParam.split(",");
        return gson.toJson(isils);
    }
    
    /**
     * Show the config for an isil.
     * 
     * @param isil The isil
     * @return JSON string data
     * @throws java.io.IOException
     */
    @GET
    @Produces("application/json; charset=utf-8")
    @Path("isil/{isil}")
    public String isil( @PathParam("isil") String isil ) throws IOException {
        BibliothecaConfig conf = getConfig( isil );
        if( conf == null ) {
            response.sendError( HttpURLConnection.HTTP_NOT_FOUND, "Isil not found: " + isil);
        }
        return gson.toJson( conf );
    }
    
    /**
     * Generates an example configuration.
     * 
     * @return JSON string data
     */
    @GET
    @Produces("application/json; charset=utf-8")
    @Path("createconfig")
    public String createConfig() {
        BibliothecaConfig conf = new BibliothecaConfig();
        conf.setInstitutionName( "Example library name");
        conf.setIsil( "EXAMPLE-ISIL-1");
        conf.setInstitutionURL( "https://my.example.library.org/");
        conf.setCatalogueURL( "https://my.example.library.org/catalogue");
        conf.setDescription( "Example configuration for finc Bibiotheca service");
        conf.setAvailability( new HashMap<String, Integer>(){{
            put("T", 0);
            put("H", 0);
            put("J", 0);
            put("D", 0);
            put("I", 0);
            put("Z", 0);
            put("K", 0);
            put("G", 0);
            put("v", 0);
            put("U", 0);
            put("B", 0);
            put("M", 0);
            put("P", 1);
            put("E", 0);
            put("e", 1);
            put("V", 1);
        }});
        conf.setJdbcJndiName("jdbc/myname");
        return gson.toJson( conf );
    }
    
    /**
     * Gets an open SQL connection.
     * 
     * @param jndiName Lookup name.
     * @return SQL connection or <i>null</i> if not found
     */
    private static Connection getDbConnection( String jndiName ) {
        try {
            InitialContext c = new InitialContext();
            DataSource ds = (DataSource)c.lookup( "java:/comp/env/" + jndiName );
            if( ds != null ) return ds.getConnection();
        } catch (Exception ex) {
            logger.fatal( "Error get sql connection for jndi name '" + jndiName + "'", ex);
        }
        return null;
    }
    
    /**
     * Gets the configuration of an isil.
     * 
     * @param isil The isil of the institution.
     * @return Bilbliotheca config or <i>null</i> if not found.
     */
    private BibliothecaConfig getConfig( String isil ) {
        
        String isilParam = context.getInitParameter( isil );
        if( isilParam == null ) return null;

        try ( FileInputStream fi = new FileInputStream( new File(isilParam)); ) {
            try ( InputStreamReader isr = new InputStreamReader(fi); ) {
                BibliothecaConfig conf = gson.fromJson( isr, BibliothecaConfig.class );
                return conf;
            }
        } catch (Exception ex) {
            logger.fatal( null, ex);
        }
        return null;
    }
    
    /**
     * Creates an error message in JSON format.
     * 
     * @param status Status code.
     * @param errorMessage The message.
     * @return JSON string data
     */
    private String errorMessage( Integer status, String errorMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("errorcode", status);
        map.put("errormessage", errorMessage);
        return gson.toJson(map);
    }
    
    /**
     * Creates a DAIA XML message.
     * 
     * @param status Status code
     * @param errorMessage Message strign
     * @return XML data
     */
    private String daiaMessage( BibliothecaConfig config, Integer status, String errorMessage) throws Exception {
        Daia daia = new Daia();
        String daiaVersion = context.getInitParameter("DAIA_VERSION");
        daia.setVersion( daiaVersion == null ? "0.5" : daiaVersion );
        Message message = new Message();
        message.setContent( errorMessage );
        message.setErrno( BigInteger.valueOf( new Long( status) ) );
        daia.getMessage().add(message);

        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        daia.setTimestamp(xgcal);

        SimpleElement se = new SimpleElement();
        se.setContent( config == null ? "not found" : config.getInstitutionName() );
        se.setHref( config == null ? "not found" :  config.getInstitutionURL() );
        daia.setInstitution( se );
        JAXBContext jc = JAXBContext.newInstance( Daia.class );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, "http://ws.gbv.de/daia/ http://ws.gbv.de/daia/daia.xsd");
        StringWriter sw = new StringWriter();
        m.marshal( daia, sw);
        return sw.toString();
    }
    
}
