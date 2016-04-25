package util.multitenant;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class DBUtil {

	private static String DB_PREFIX = "multitenant";
	private static Map<String, Session> sesiones = new HashMap<>();

	public static Session crearSession(String tenant) {
		SessionFactory factory = new Configuration().configure().buildSessionFactory();
		Session session = factory.withOptions().tenantIdentifier(DB_PREFIX + "_" + tenant).openSession();
		return session;
	}
	
	public static Session getSession(String tenant) {
		return sesiones.get(tenant);
	}
	
	public static void closeSession(String tenant) {
		Session sesion = sesiones.get(tenant);
		sesion.getSessionFactory().close();
		sesiones.remove(tenant);
	}

	public static void crearBase(String nombre) {
		ConnectionProvider conProv = new ConnectionProviderImpl("");
		try {
			conProv.getConnection().createStatement().execute("CREATE DATABASE IF NOT EXISTS " + nombre);
			conProv.getConnection().close();
			try {
				List<String> queries = getQueriesFromSQLFile("META-INF/tablas.sql");
				int count = 0;
				for (String q : queries) {
					System.out.println("Ejecutando Query: " + q);
					if (count % 4 == 0)
						conProv = new ConnectionProviderImpl(nombre);

					conProv.getConnection().createStatement().execute(q);
					conProv.getConnection().close();
					count++;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e) {
			throw new HibernateException(
					"MultiTenantConnectionProvider::Could not alter JDBC connection to specified schema [" + nombre
							+ "]",
					e);
		}
	}

	public static void modificarBase(String nombre) {
		try {

			System.out.println("Modificando clave `av_usuarioscompartidos`...");

			ConnectionProvider conProv = new ConnectionProviderImpl(nombre);
			conProv.getConnection().createStatement().execute(
					"ALTER TABLE `av_usuarioscompartidos` ADD KEY `FK_usucomp` (`usuariosCompartidos_idUsuario`) USING BTREE;");
			conProv.getConnection().close();

			conProv = new ConnectionProviderImpl(nombre);
			conProv.getConnection().createStatement()
					.execute("ALTER TABLE `av_usuarioscompartidos` DROP KEY `UK_6fs67215r98r91xul672y3a8k`;");
			conProv.getConnection().close();

			conProv = new ConnectionProviderImpl(nombre);
			conProv.getConnection().createStatement().execute(
					"ALTER TABLE `usuario_avcompartidos` ADD KEY `FK_usucomp` (`AVcompartidos_idAV`) USING BTREE;");
			conProv.getConnection().close();

			conProv = new ConnectionProviderImpl(nombre);
			conProv.getConnection().createStatement()
					.execute("ALTER TABLE `usuario_avcompartidos` DROP KEY `UK_31aoc7x2q3fbs7vhqawyw82sm`;");
			conProv.getConnection().close();

			System.out.println("clave modificada con éxito");

		} catch (SQLException e) {
			throw new HibernateException(
					"MultiTenantConnectionProvider::Could not alter JDBC connection to specified schema [" + nombre
							+ "]",
					e);
		}
	}

	public static List<String> getQueriesFromSQLFile(String url) throws FileNotFoundException {

		InputStream in = DBUtil.class.getClassLoader().getResourceAsStream(url);
		Scanner input;
		List<String> lineas = new ArrayList<>();
		List<String> queries = new ArrayList<>();

		input = new Scanner(in, "utf-8");

		while (input.hasNext()) {
			// or to process line by line
			lineas.add(input.nextLine());
		}

		String aux = "";
		String auxCom = "";
		boolean comentario = false;

		for (String l : lineas) {
			if (!l.startsWith("--") && (!comentario)) {
				if (!l.startsWith("/*") || (l.startsWith("/*") && l.contains("40101 SET NAMES"))) {
					aux += l;
					if (l.endsWith(";")) {
						queries.add(aux);
						aux = "";
					}
				} else {
					if (!l.endsWith("*/") && !l.endsWith("*/;")) {
						comentario = true;
					}
				}
			} else if (l.endsWith("*/") || l.endsWith("*/;")) {
				comentario = false;
			}
		}
		input.close();

		return queries;
	}

	public static void eliminarTenant(String tenant) {
		ConnectionProvider conProv = new ConnectionProviderImpl("");
		try {
			conProv.getConnection().createStatement().execute("DROP DATABASE " + DB_PREFIX + "_" + tenant);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
