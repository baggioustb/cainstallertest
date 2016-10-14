package com.yangjun.cainstallertest;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectTLSWifi();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectTLSWifi() {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "YOUR_SSID";
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        wc.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.hiddenSSID = false;

        try {
            // Install certificate
            InputStream is = getResources().openRawResource(R.raw.test_ca);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(is);
            wc.enterpriseConfig.setCaCertificate(caCert);

            // install client key entry
            KeyStore pkcs12ks = KeyStore.getInstance("pkcs12");
            InputStream in = getResources().openRawResource(R.raw.test_client_cert);
            pkcs12ks.load(in, "your_password".toCharArray());
            Enumeration<String> aliases = pkcs12ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Log.d(TAG, "Processing alias " + alias);

                X509Certificate clientCert = (X509Certificate) pkcs12ks.getCertificate(alias);
                Log.d(TAG, clientCert.toString());

                PrivateKey key = (PrivateKey) pkcs12ks.getKey(alias, "your_password".toCharArray());
                Log.d(TAG, key.toString());
                wc.enterpriseConfig.setClientKeyEntry(key, clientCert);
                wc.enterpriseConfig.setIdentity("your_username"); //user name
            }

            // connect wifi
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            int netID = wifiManager.addNetwork(wc);
            wifiManager.saveConfiguration();
            wifiManager.enableNetwork(netID, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}