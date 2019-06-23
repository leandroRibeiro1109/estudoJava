package com.b2wdigital.mavenproject1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author leandro.pribeiro
 */
@WebServlet(name = "Coletor", urlPatterns = {"/Coletor"})
public class Coletor extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            String queryString = request.getQueryString();

            String codigoItem = null;
            String[] splitted = queryString.split("&");
            for (int i = 0; i < splitted.length; i++) {
                String[] keyValueSplitted = splitted[i].split("=");
                String key = keyValueSplitted[0];
                if (key.equalsIgnoreCase("codigoItem")) {
                    codigoItem = keyValueSplitted[1];
                }
            }

            response.setContentType("application/json;charset=UTF-8");
            OutputStream outputStream = response.getOutputStream();
            consultarItem(codigoItem, outputStream);
            outputStream.close();
        } catch (Exception ex) {
            Logger.getLogger(Coletor.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(Coletor.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private static void consultarItem(String codigoItem, OutputStream outputStream) throws SQLException, IOException {
        OracleDataSource dataSource = new OracleDataSource();

        dataSource.setURL("jdbc:oracle:thin:@//localhost:1521/ORCL11G.US.ORACLE.COM");
        dataSource.setUser("wlasa");
        dataSource.setPassword("wlasa");

        try (Connection connection = dataSource.getConnection()) {
//            executeSelect(connection);
            try (CallableStatement callableStatement = connection.prepareCall("{call pc_clt_sfe084.processa(?,?)}")) {
                callableStatement.setString("piidean", codigoItem);
                callableStatement.registerOutParameter("ponomeitem", java.sql.Types.VARCHAR);
                //callableStatement.registerOutParameter("p_id_terceiro", java.sql.Types.NUMERIC);

                callableStatement.execute();

                String nome = callableStatement.getString("ponomeitem");
                //BigDecimal idTerceiro = callableStatement.getBigDecimal("p_id_terceiro");

               // String outputMessage = "{\"Ean:\"" + codigoItem + "\", \"nome\":" + nome + "\", \"codigoTerceiro\":" + idTerceiro.toString() + "}";
                String outputMessage = "{\"Ean:\"" + codigoItem + "\", \"nome\":" + nome  + "}";
                outputStream.write(outputMessage.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Coletor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
