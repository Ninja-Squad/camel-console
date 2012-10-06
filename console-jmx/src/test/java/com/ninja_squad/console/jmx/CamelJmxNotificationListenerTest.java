package com.ninja_squad.console.jmx;

import org.joda.time.DateTime;
import org.junit.Test;

import javax.management.Notification;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CamelJmxNotificationListenerTest {

    @Test
    public void handleNotification() throws Exception {
        //building a notification
        Map<String, Object> userData = new HashMap<String, Object>();
        String timestamp = new DateTime(2012, 10, 4, 8, 5, 43).toString();
        userData.put("TimeStamp", timestamp);
        Map<String, String> headers = new HashMap<String, String>();
        String breadcrumbId = "ID-MacBook-Pro-de-Cedric-Exbrayat-local-64124-1349330736710-0-1";
        headers.put("breadcrumbid", breadcrumbId);
        userData.put("Headers", headers);
        String body = "route1 - 1";
        userData.put("Body", body);
        String exchangeId = "ID-MacBook-Pro-de-Cedric-Exbrayat-local-64124-1349330736710-0-2";
        userData.put("ExchangeId", exchangeId);
        String destination = "mock:result";
        userData.put("EndpointURI", destination);
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("CamelCreatedTimestamp", timestamp);
        String source = "direct://route1";
        properties.put("CamelToEndpoint", source);
        userData.put("Properties", properties);


        Notification notification = new Notification("TraceNotification", "exchange", 0, System.currentTimeMillis(), body);
                    notification.setUserData(userData);

        //listener
        CamelJmxNotificationListener listener = spy(new CamelJmxNotificationListener());
        listener.handleNotification(notification, null);

        //then
        CamelJmxNotification expectedNotification = new CamelJmxNotification();
        expectedNotification.setBody(body);
        expectedNotification.setDestination(destination);
        expectedNotification.setExchangeId(exchangeId);
        expectedNotification.setHeaders(headers);
        expectedNotification.setBreadcrumbId(breadcrumbId);
        expectedNotification.setProperties(properties);
        expectedNotification.setSource(source);
        expectedNotification.setTimestamp(new DateTime(2012, 10, 4, 8, 5, 43));

        verify(listener).storeNotification(expectedNotification);
    }

}
