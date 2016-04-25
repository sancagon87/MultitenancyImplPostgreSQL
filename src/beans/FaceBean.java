package beans;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.hibernate.Session;

import modelo.ObjetoBasico;
import util.multitenant.DBUtil;

@ManagedBean
@SessionScoped
public class FaceBean implements Serializable {
	
	public void persistir() {
		
		ObjetoBasico ob = new ObjetoBasico("Pepito");
		
		Session session = DBUtil.crearSession("master");
		try {
			session.beginTransaction();
			session.persist(ob);
			session.getTransaction().commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
		session.close();
		System.out.println("HOLA!");
		
	}

}
