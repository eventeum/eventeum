package net.consensys.eventeum.plugin.chain.service;

import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.PrivateTransactionManager;
import org.web3j.tx.gas.BesuPrivacyGasProvider;
import org.web3j.tx.response.PollingPrivateTransactionReceiptProcessor;
import org.web3j.utils.Base64String;
import org.web3j.utils.Restriction;

public class TestUtils {

  public static final Credentials creds =
      Credentials.create("9e5c50f9c8d81cadcdd53da98ecb466bdeb0e148b7e062b0d673938b3bcddbe8");
  public static final Base64String privateFrom =
      Base64String.wrap("AWnJrjKbB4sjFhjBWNvtsbHuDVwfpNeiluU+56KdEFQ=");
  public static final Base64String privacyGroupId =
      Base64String.wrap("UVZoQkxuTmxiR1l3TURBd01EQXdNREF3TURBd01EQXc=");
  public static final long chainId = 11111111;
  public static final Besu besu =
      Besu.build(
          new HttpService(
              "http://localhost:11018",
              new OkHttpClient.Builder()
                  .connectTimeout(10, TimeUnit.SECONDS)
                  .writeTimeout(10, TimeUnit.SECONDS)
                  .readTimeout(10, TimeUnit.SECONDS)
                  .build()));

  public static final BesuPrivacyGasProvider gasProvider =
      new BesuPrivacyGasProvider(BigInteger.valueOf(40000), BigInteger.valueOf(20000000));

  public static final PrivateTransactionManager privateManager =
      new PrivateTransactionManager(
          besu,
          creds,
          new PollingPrivateTransactionReceiptProcessor(
              besu, 1000, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH),
          chainId,
          privateFrom,
          privacyGroupId,
          Restriction.UNRESTRICTED);

  public static final FastRawTransactionManager publicManager =
      new FastRawTransactionManager(besu, creds, chainId);

  public static final int BLOCK_TIME_MS = 2000;

  public static final String EVENT_SPEC_HASH =
      "0x02f83fe6099cbcd06b097fe089934fc7c63df36c0daea35d2fb79e00019c7b13";

  public static final String PRIVATE_CONTRACT_ADDRESS =
      Keys.toChecksumAddress("0x18b5492ecbf4ecda07867912bc15665bad285d9e");

  public static final String PUBLIC_CONTRACT_ADDRESS =
      Keys.toChecksumAddress("0x658a18e951c5f8e6d1d2429ae671e6968d5718e2");

  public static final BigInteger CONTRACT_VERSION = BigInteger.ONE;
}
