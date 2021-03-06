/**
 * Copyright 2016 BlazeMeter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.bamboo.plugin.api;

import com.google.common.collect.LinkedHashMultimap;
import com.blazemeter.bamboo.plugin.TestStatus;
import java.util.Collection;
import java.util.Map;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface Api {

    String AUTHORIZATION = "Authorization";
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    MediaType TEXT = MediaType.parse("text/plain; charset=ISO-8859-1");
    String ACCEPT = "Accept";
    String CONTENT_TYPE = "Content-type";
    String APP_JSON = "application/json";
    String APP_JSON_UTF_8 = "application/json; charset=UTF-8";
    String PROXY_AUTHORIZATION = "Proxy-Authorization";
    String APP_KEY = "jnk100x987c06f4e10c4";

    TestStatus masterStatus(String id);

    int getTestMasterStatusCode(String id);

    HashMap<String, String> startTest(String testId, boolean collection) throws JSONException, IOException;

    JSONObject stopTest(String testId) throws IOException, JSONException;

    void terminateTest(String testId) throws IOException;

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> testsMultiMap() throws IOException, MessagingException;

    LinkedHashMultimap<String, String> collectionsMultiMap(int workspaceId) throws IOException, MessagingException;

    JSONObject user() throws IOException, JSONException;

    JSONObject getCIStatus(String sessionId) throws JSONException, IOException;

    boolean active(String testId);

    String retrieveJUNITXML(String sessionId) throws IOException;

    JSONObject retrieveJtlZip(String sessionId) throws IOException, JSONException;

    List<String> getListOfSessionIds(String masterId) throws IOException, JSONException;

    JSONObject generatePublicToken(String sessionId) throws IOException, JSONException;

    String getServerUrl();

    boolean notes(String note, String masterId) throws Exception;

    boolean properties(JSONArray properties, String sessionId) throws Exception;

    boolean verifyCredentials();

    HashMap<Integer, String> accounts();

    HashMap<Integer, String> workspaces();

    Map<String, Collection<String>> getTestsMultiMap();

    boolean collection(String testId) throws Exception;
}

