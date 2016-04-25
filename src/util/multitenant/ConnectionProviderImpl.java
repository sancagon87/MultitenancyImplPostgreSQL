package util.multitenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class ConnectionProviderImpl implements ConnectionProvider {
	
	private final BasicDataSource basicDataSource = new BasicDataSource();
	
	private static Map<String, String> ENV;
	private static String SQL_HOST = "localhost";
	private static String SQL_PORT = "5432";
	private static String USER = "postgres";
	private static String PASS = "admin";
	
	public ConnectionProviderImpl(String database){
		
		if( ENV == null )
			ENV = System.getenv();
		/*
		if( SQL_HOST == null )
			SQL_HOST = ENV.get("OPENSHIFT_MYSQL_DB_HOST");
		
		if( SQL_PORT == null )
			SQL_PORT = ENV.get("OPENSHIFT_MYSQL_DB_PORT");
		*/
		
		//this should be read from properties file		
		//basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		basicDataSource.setDriverClassName("org.postgresql.Driver");
		
		//basicDataSource.setUrl("jdbc:mysql://localhost:3306/"+database);
		//basicDataSource.setUrl("jdbc:mysql://" + SQL_HOST + ":" + SQL_PORT + "/" + database); // es para la nube
		basicDataSource.setUrl("jdbc:postgresql://" + SQL_HOST + ":" + SQL_PORT + "/" + database); // es para la nube
		basicDataSource.setUsername(USER);
		basicDataSource.setPassword(PASS);
		basicDataSource.setInitialSize(1);
		basicDataSource.setMaxTotal(1);
		
	}
	
	@Override
	public boolean isUnwrappableAs(Class arg0) {
		return false;
	}
	
	@Override
	public <t> t unwrap(Class<t> arg0) {
		return null;
	}
	
	@Override
	public void closeConnection(Connection arg0) throws SQLException {
		arg0.close();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return basicDataSource.getConnection();
	}
	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}
	
	public void close() {
		try {
			basicDataSource.close();
		} catch (SQLException e) {
			System.out.println("ConnectionProviderImpl:close() --> Error al cerrar DataSource");
			e.printStackTrace();
		}
	}

}
