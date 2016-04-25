package util.multitenant;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;




public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

	private ConnectionProvider connectionProvider = new ConnectionProviderImpl(CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID);

	@Override
	public boolean isUnwrappableAs(Class arg0) {
		return false;
	}
	
	@Override
	public <t> t unwrap(Class<t> arg0) {
		return null;
	}
	
	@Override
	public Connection getAnyConnection() throws SQLException {
		System.out.println("inside MultiTenantConnectionProvider::getAnyConnection");
		Connection conn = null;
		try {
			conn = connectionProvider.getConnection();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}
	
	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connectionProvider.closeConnection( connection );
	}
	
	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		final Connection newConnection;
		try {
			//connection.createStatement().execute( "USE " + tenantIdentifier ); //MySQL
			//PostgreSQL:
			releaseAnyConnection(connection);
			((ConnectionProviderImpl) connectionProvider).close();
			connectionProvider = new ConnectionProviderImpl(tenantIdentifier);
			newConnection = getAnyConnection(); 
			//PostgreSQL;
		}
		catch ( SQLException e ) {
			throw new HibernateException(
					"MultiTenantConnectionProvider::Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]",e);
		}
		//return connection; //MySQL
		return newConnection; //PostgreSQL
	}
	
	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		final Connection newConnection;
		try {
			//connection.createStatement().execute( "USE " + CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID ); //MySQL
			//PostgreSQL:
			releaseAnyConnection(connection);
			((ConnectionProviderImpl) connectionProvider).close();
			//PostgreSQL;
		}
		catch ( SQLException e ) {
			throw new HibernateException(
					"Could not alter JDBC connection to specified schema [" +
							tenantIdentifier + "]",e);
		}
	}
	
	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

}