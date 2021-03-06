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

package de.unileipzig.ub.finc.bibliotheca.config;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Config holder class.
 * 
 * @author <a href="mailto:tsolakidis@ub.uni-leipzig.de">Polichronis Tsolakidis</a>
 */
public class BibliothecaConfig {

    @SerializedName("institutionurl")
    private String institutionURL;
    @SerializedName("catalogueurl")
    private String catalogueURL;
    private String isil;
    @SerializedName("institutionname")
    private String institutionName;
    private String description;
    private Map<String, Integer> availability;
    @SerializedName("jdbcjndiname")
    private String jdbcJndiName;

    /**
     * @return the isil
     */
    public String getIsil() {
        return isil;
    }

    /**
     * @param isil the isil to set
     */
    public void setIsil(String isil) {
        this.isil = isil;
    }

    /**
     * @return the name
     */
    public String getInstitutionName() {
        return institutionName;
    }

    /**
     * @param institutionName
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the availability
     */
    public Map<String, Integer> getAvailability() {
        return availability;
    }

    /**
     * @param availability the availability to set
     */
    public void setAvailability(Map<String, Integer> availability) {
        this.availability = availability;
    }

    /**
     * @return the jdbcJndiName
     */
    public String getJdbcJndiName() {
        return jdbcJndiName;
    }

    /**
     * @param jdbcJndiName the jdbcJndiName to set
     */
    public void setJdbcJndiName(String jdbcJndiName) {
        this.jdbcJndiName = jdbcJndiName;
    }

    /**
     * @return the institutionURL
     */
    public String getInstitutionURL() {
        return institutionURL;
    }

    /**
     * @param institutionURL the institutionURL to set
     */
    public void setInstitutionURL(String institutionURL) {
        this.institutionURL = institutionURL;
    }

    /**
     * @return the catalogueURL
     */
    public String getCatalogueURL() {
        return catalogueURL;
    }

    /**
     * @param catalogueURL the catalogueURL to set
     */
    public void setCatalogueURL(String catalogueURL) {
        this.catalogueURL = catalogueURL;
    }
    

}
