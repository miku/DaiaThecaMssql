#DaiaThecaMssql

Restful webservice implementation of the Document Availability Information API (DAIA) for Bibliotheca (using direct access to Bibliotheca's MsSQL-database)

DAIA - http://gbv.github.io/daiaspec

##Installation DaiaThecaMssql - Bibliotheca DAIA Service

###Requirements

The current code was tested with

* Tomcat 6
* Maven 3.0.5
* Bibliotheca 4

###Instructions

1. Change to folder DaiaThecaMssql and build WebArchive

        $ cd ./DaiaThecaMssql
        $ mvn -Prelease-profile install
        
2. Copy built WAR-archive into Tomcat webapps folder

        $ cp target/DaiaThecaMssql-1.0-SNAPSHOT.war ${catalina.base}/webbapps/DaiaThecaMssql.war

3. Copy MsSQL Database-Driver to ${catalina.base}/lib (is provided through Maven install - Step 1)
          
4. Configuration - context.xml

    * Create database pools
    Create a pool for each institution using unique identifiers (e.g. "MyPool").
    The used identifiers will be referenced in web.xml and the configuration-file.

            <Resource name="jdbc/MyPool"
             description="My MsSQL database pool"
             auth="Container"
             type="javax.sql.DataSource"
             maxActive="5"
             maxIdle="6"
             minIdle="1"
             maxWait="250"
             username="johndoe"
             password="secret"
             driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
             url="jdbc:sqlserver://127.0.0.1:1433;database=MYDB"
            />

    * Configure institution

        * Create list of existing institutions

                <Parameter name="isils" override="false" description="Comma separated list of available Bibliotheca isils"
                value="MYISIL-1,MYISIL-2"
                />

        * Create references for configuration files of existing institutions

                <Parameter name="MYISIL-1" override="false" 
                 description="Config file path for this isil in JSON format. Add them in the list of isils above."
                 value="${catalina.base}/conf/MYISIL1.conf"
                />
                <Parameter name="MYISIL-2" override="false" 
                 description="Config file path for this isil in JSON format. Add them in the list of isils above."
                 value="${catalina.base}/conf/MYISIL2.conf"
                />

        * Example of a configuration file "MYISIL1.conf" for an institution with Isil "MYISIL-1"

                {
                    "institutionurl": "https://my.example.library.org/",
                    "catalogueurl": "https://my.example.library.org/catalogue",
                    "isil": "MYISIL-1",
                    "institutionname": "Example library name",
                    "description": "Example configuration for finc Bibiotheca service",
                    "availability": {
                        "D": 0,
                        "E": 0,
                        "e": 1,
                        "G": 0,
                        "B": 0,
                        "M": 0,
                        "H": 0,
                        "I": 0,
                        "J": 0,
                        "K": 0,
                        "U": 0,
                        "T": 0,
                        "v": 0,
                        "V": 1,
                        "P": 1,
                        "Z": 0
                    },
                    "jdbcjndiname": "jdbc/MyPool"
                }

          The webservice at /rs/createconfig shows an exemplary configuration

5. Configuration - web.xml
        
    * Create references for database-pool
      Create references for each database-pool.

              <resource-ref>
                    <description>My DB Connection</description>
                    <res-ref-name>jdbc/MyPool</res-ref-name>
                    <res-type>javax.sql.DataSource</res-type>
                    <res-auth>Container</res-auth>
              </resource-ref>

6. Done - you can access the webservice at http://server-ip/Bibliotheca/
