package com.dropbox.core;

import static org.testng.Assert.*;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;

// integration test utility class
public final class ITUtil {
    private static final String AUTH_INFO_FILE_PROPERTY = "com.dropbox.test.authInfoFile";
    private static final String HTTP_REQUESTOR_PROPERTY = "com.dropbox.test.httpRequestor";

    private static final Random RAND = new Random(0L);
    private static final long READ_TIMEOUT = TimeUnit.SECONDS.toMillis(20);
    private static final int MAX_RETRIES = 3;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH.mm.ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public static DbxRequestConfig.Builder newRequestConfig() {
        return DbxRequestConfig.newBuilder("sdk-integration-test")
            // enable auto-retry to avoid flakiness
            .withAutoRetryEnabled(MAX_RETRIES)
            .withUserLocaleFrom(Locale.US)
            .withHttpRequestor(newOkHttp3Requestor());
    }

    public static OkHttp3Requestor newOkHttp3Requestor() {
        okhttp3.OkHttpClient httpClient = OkHttp3Requestor.defaultOkHttpClient().newBuilder()
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();
        return new OkHttp3Requestor(httpClient);
    }

    public static StandardHttpRequestor newStandardHttpRequestor() {
        StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder()
            .withReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();
        return new StandardHttpRequestor(config);
    }

    private static class AuthContainer {
        public static final DbxAuthInfo AUTH = loadAuth();

        private static DbxAuthInfo loadAuth() {
            String path = System.getProperty(AUTH_INFO_FILE_PROPERTY);


            if (path == null) {
                fail("System property \"" + AUTH_INFO_FILE_PROPERTY + "\" not set.");
            }
            try {
                return DbxAuthInfo.Reader.readFromFile(path);
            } catch (JsonReader.FileLoadException ex) {
                fail("Error reading auth info from \"" + path + "\"", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public static DbxAuthInfo getAuth() {
        return AuthContainer.AUTH;
    }

    public static DbxClientV1 newClientV1() {
        return newClientV1(newRequestConfig());
    }

    public static DbxClientV1 newClientV1(DbxRequestConfig.Builder config) {
        return newClientV1(config.build());
    }

    public static DbxClientV1 newClientV1(DbxRequestConfig config) {
        DbxAuthInfo auth = getAuth();
        return new DbxClientV1(
            config,
            auth.getAccessToken(),
            auth.getHost()
        );
    }

    public static DbxClientV2 newClientV2() {
        return newClientV2(newRequestConfig());
    }

    public static DbxClientV2 newClientV2(DbxRequestConfig.Builder config) {
        return newClientV2(config.build());
    }

    public static DbxClientV2 newClientV2(DbxRequestConfig config) {
        DbxAuthInfo auth = getAuth();
        return new DbxClientV2(
            config,
            auth.getAccessToken(),
            auth.getHost()
        );
    }

    private static final class RootContainer {
        public static String ROOT = "/test/dropbox-sdk-java/" + format(new Date());
    }

    public static String root(Class<?> clazz) {
        return RootContainer.ROOT + "/" + clazz.getCanonicalName();
    }

    public static String path(Class<?> clazz, String path) {
        if (!path.startsWith("/") && !path.isEmpty()) {
            path = "/" + path;
        }
        return root(clazz) + path;
    }

    public static String format(Date date) {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(UTC);
        return format.format(date);
    }

    public static byte [] randomBytes(int num) {
        byte [] data = new byte[num];
        RAND.nextBytes(data);
        return data;
    }

    public static byte [] toBytes(String content) {
        return content.getBytes(UTF8);
    }

    @BeforeSuite
    @AfterSuite(alwaysRun=true)
    public static void deleteRoot() throws Exception {
        // always use standard HTTP requestor for delete root
        DbxClientV2 client = newClientV2(
            newRequestConfig()
                .withHttpRequestor(newStandardHttpRequestor())
        );
        try {
            client.files().delete(RootContainer.ROOT);
        } catch (DeleteErrorException ex) {
            if (ex.errorValue.isPathLookup() &&
                ex.errorValue.getPathLookupValue().isNotFound()) {
                // ignore
            } else {
                throw ex;
            }
        }
    }
}
