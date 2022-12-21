package com.itheima;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.core.http.HttpClient;
import com.aliyun.core.http.HttpMethod;
import com.aliyun.core.http.ProxyOptions;
import com.aliyun.httpcomponent.httpclient.ApacheAsyncHttpClientBuilder;
import com.aliyun.sdk.service.dysmsapi20170525.models.*;
import com.aliyun.sdk.service.dysmsapi20170525.*;
import com.google.gson.Gson;
import darabonba.core.RequestConfiguration;
import darabonba.core.client.ClientOverrideConfiguration;
import darabonba.core.utils.CommonUtil;
import darabonba.core.TeaPair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//import javax.net.ssl.KeyManager;
//import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@SpringBootTest
class ReggieTakeOutApplicationTests {

	@Test
	public void sendMsgTest(){
		StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
				.accessKeyId("LTAI5tHAUGeWXBNTrYFY8qJX")
				.accessKeySecret("n0eVDIIlgsJShnY0aY9HFxtOIijly5")
				//.securityToken("<your-token>") // use STS token
				.build());

		// Configure the Client
		AsyncClient client = AsyncClient.builder()
				.region("cn-hangzhou") // Region ID
				//.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
				.credentialsProvider(provider)
				//.serviceConfiguration(Configuration.create()) // Service-level configuration
				// Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
				.overrideConfiguration(
						ClientOverrideConfiguration.create()
								.setEndpointOverride("dysmsapi.aliyuncs.com")
						//.setConnectTimeout(Duration.ofSeconds(30))
				)
				.build();

		// Parameter settings for API request
		SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
				.signName("阿里云短信测试")
				.templateCode("SMS_154950909")
				.phoneNumbers("13232802715")
				.templateParam("{\"code\":\"1234\"}")
				// Request-level configuration rewrite, can set Http request parameters, etc.
				// .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
				.build();

		// Asynchronously get the return value of the API request
		CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
		// Synchronously get the return value of the API request
		//SendSmsResponse resp = response.get();
		//System.out.println(new Gson().toJson(resp));
		// Asynchronous processing of return values
        /*response.thenAccept(resp -> {
            System.out.println(new Gson().toJson(resp));
        }).exceptionally(throwable -> { // Handling exceptions
            System.out.println(throwable.getMessage());
            return null;
        });*/

		// Finally, close the client
		client.close();
	}

}
