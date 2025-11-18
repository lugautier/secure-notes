package com.securenotes.config;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Slf4j
public class JwtConfig {

  @Value("${jwt.private-key}")
  private String privateKeyString;

  @Value("${jwt.public-key}")
  private String publicKeyString;

  @Value("${jwt.expiration}")
  private long expiration;

  public PrivateKey getPrivateKey() {
    try {
      if (privateKeyString == null || privateKeyString.isEmpty()) {
        throw new IllegalArgumentException(
            "JWT private key is not configured. Set JWT_PRIVATE_KEY environment variable.");
      }
      PEMParser parser = new PEMParser(new StringReader(privateKeyString));
      Object keyObject = parser.readObject();
      parser.close();
      return new JcaPEMKeyConverter()
          .getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) keyObject);
    } catch (IOException e) {
      log.error("Failed to parse JWT private key", e);
      throw new RuntimeException("Failed to parse JWT private key", e);
    }
  }

  public PublicKey getPublicKey() {
    try {
      if (publicKeyString == null || publicKeyString.isEmpty()) {
        throw new IllegalArgumentException(
            "JWT public key is not configured. Set JWT_PUBLIC_KEY environment variable.");
      }
      PEMParser parser = new PEMParser(new StringReader(publicKeyString));
      Object keyObject = parser.readObject();
      parser.close();
      return new JcaPEMKeyConverter()
          .getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) keyObject);
    } catch (IOException e) {
      log.error("Failed to parse JWT public key", e);
      throw new RuntimeException("Failed to parse JWT public key", e);
    }
  }
}
