package com.swisscom.camunda.connector.secretprovider;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;

import io.camunda.connector.api.secret.SecretProvider;

/**
 * The Class HashicorpVaultProvider.
 *
 * @author Daniele
 */
public class HashicorpVaultProvider implements SecretProvider {

	/** The vault endpoint. */
	VaultEndpoint vaultEndpoint = null;
	
	/** The vault template. */
	VaultTemplate vaultTemplate = null;
	
	/** The basepath. */
	String basepath;

	/**
	 * Instantiates a new hashicorp vault provider.
	 */
	public HashicorpVaultProvider() {

		// not spring managed!
		Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			vaultEndpoint = new VaultEndpoint();
			vaultEndpoint.setHost(properties.getProperty("vault.host"));
			vaultEndpoint.setPort(Integer.parseInt(properties.getProperty("vault.port")));
			vaultEndpoint.setScheme(properties.getProperty("vault.scheme"));
			vaultTemplate = new VaultTemplate(vaultEndpoint,
					new TokenAuthentication(properties.getProperty("vault.token")));
			basepath = properties.getProperty("vault.basepath");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Gets the secret.
	 *
	 * @param name the name
	 * @return the secret
	 */
	@Override
	public String getSecret(String name) {

		// Write a secret
		// Map<String, String> data = new HashMap<>();
		// data.put("password", "Hashi123");
		// vaultTemplate.opsForVersionedKeyValue("kv").put(basepath+name, data);

		Versioned<Map<String, Object>> secret = vaultTemplate.opsForVersionedKeyValue("kv").get(name);
		if (secret != null && secret.hasData()) {
			return (String) secret.getData().get("password");
		} else {
			return null;
		}
	}

}
