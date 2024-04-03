import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class JPMCTest {
    String userName = System.getenv("LT_USERNAME") == null ?
            "username" : System.getenv("LT_USERNAME"); //Add username here
    String accessKey = System.getenv("LT_ACCESS_KEY") == null ?
            "accessKey" : System.getenv("LT_ACCESS_KEY"); //Add accessKey here
    String credential = Credentials.basic(userName, accessKey);

    Boolean uploadApp = false;
    String appUrl = null;
    @BeforeSuite
    @Parameters({"UploadApp", "AppPath", "AppURL"})
    void beforeSuite(String uploadAppParam, String appPathParam, String appUrlParam){
        uploadApp = Boolean.parseBoolean(uploadAppParam);

        if(uploadApp){
            try {
                System.out.println("Uploading Application");
                appUrl = uploadApp(credential, appPathParam, "JPMC");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            appUrl = appUrlParam;
        }
    }

    @Test
    public void Test(ITestContext context) throws IOException {
        IOSDriver driver;
        String gridURL = "@mobile-hub.lambdatest.com/wd/hub";

        HashMap<String, Object> ltOptions = new HashMap<String, Object>();
        DesiredCapabilities capabilities = new DesiredCapabilities();

        ltOptions.put("deviceName", "iPhone 14");
        ltOptions.put("platformVersion","17.4");
        ltOptions.put("platformName", "ios");
        ltOptions.put("isRealMobile", false);
        ltOptions.put("privateCloud", true);
        ltOptions.put("name",context.getCurrentXmlTest().getName());
        ltOptions.put("app", appUrl);
        ltOptions.put("deviceOrientation", "PORTRAIT");
        ltOptions.put("console", true);
        ltOptions.put("network", false);
        ltOptions.put("visual", true);
        ltOptions.put("devicelog", true);
        ltOptions.put("w3c", true);
        capabilities.setCapability("LT:Options", ltOptions);

        System.out.println(ltOptions.toString());

        String hub = "https://" + userName + ":" + accessKey + gridURL;
        driver = new IOSDriver(new URL(hub), capabilities);

        //App specific code

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        wait.until(ExpectedConditions.elementToBeClickable(By.name("Allow Once)"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("Sign in"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("Enter your username"))).sendKeys("Chase Username");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("Enter your password "))).sendKeys("Chase Password");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("qaLogonButton"))).click();


        driver.quit();
    }

    private String uploadApp(String credential, String filePath, String appName) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build();


        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("appFile", filePath,
                        RequestBody.create(MediaType.parse("application/octet-stream"), new File(filePath)))
                .addFormDataPart("name",appName)
                .build();
        Request request = new Request.Builder()
                .url("https://manual-api.lambdatest.com/app/upload/virtualDevice")
                .method("POST", body)
                .addHeader("Authorization", credential)
                .build();

        String jsonStringResponse;
        try (Response response = client.newCall(request).execute()) {
            jsonStringResponse = response.body().string();
        }

        System.out.println("Uploaded App:");
        System.out.println(jsonStringResponse);

        JSONObject jsonObject = new JSONObject(jsonStringResponse);
        return jsonObject.get("app_url").toString();

    }
}
