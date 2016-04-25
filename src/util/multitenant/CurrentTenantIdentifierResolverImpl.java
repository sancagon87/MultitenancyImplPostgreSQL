package util.multitenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

	public static ThreadLocal<String> _tenantIdentifier = new ThreadLocal<String>();
	public static String DEFAULT_TENANT_ID = "multitenant_master";

	@Override
	public String resolveCurrentTenantIdentifier() {
		System.out.println("from inside resolveCurrentTenantIdentifier....");
		String tenantId = _tenantIdentifier.get();

		if (tenantId == null)
			tenantId = DEFAULT_TENANT_ID;

		System.out.println("threadlocal tenant id =" + tenantId);
		return tenantId;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}