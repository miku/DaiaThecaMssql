<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>University Leipzig - Bibliotheca service</title>
        <style type="text/css">
            H1 {
                padding-left: 5px;
                padding-right: 5px;
                padding-bottom: 8px;
                border-bottom: 1px solid gray;
            }
            table {
                background-color: #EFEFFF;
                font-size: 11pt;
                border: 1px solid gray;
            }
            caption {
                font-size: 12pt;
                border: 1px solid gray;
                padding: 3px;
                background-color: #EFEFFF;
                font-weight: bold;
                margin: 0px 0px 1px 0px;
            }
            th {
                padding: 3px;
                border: 2px solid gray;
            }
            td {
                border: 1px solid gray;
                padding: 3px;
            }
        </style>
    </head>
    <body>
        <h1>University Leipzig - Bibliotheca service</h1>
        <table>
            <caption>Available services</caption>
            <thead>
                <tr>
                    <th>Service</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>rs/isils</a></td>
                    <td>List available isils</td>
                </tr>
                <tr>
                    <td>rs/isil/{isil}</td>
                    <td>Show the configuration of an isil</td>
                </tr>
                <tr>
                    <td>rs/{isil}/ppn/{recordId}</td>
                    <td>Get data for a comma separated list of ppn's.</td>
                </tr>
                <tr>
                    <td>rs/createconfig</td>
                    <td>Show an example config</td>
                </tr>
                <tr>
                    <td>rs/{isil}/daia/{recordId}</td>
                    <td>Get daia for a ppn</td>
                </tr>
            </tbody>
        </table>
    </body>
</html>
