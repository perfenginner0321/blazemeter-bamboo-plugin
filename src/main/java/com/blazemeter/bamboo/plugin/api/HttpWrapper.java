package com.blazemeter.bamboo.plugin.api;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HttpWrapper {
    private transient DefaultHttpClient httpClient = null;

    public HttpWrapper() {
        this.httpClient = new DefaultHttpClient();
//      TODO  this.logger.setDebugEnabled(false);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 90000);
        HttpConnectionParams.setSoTimeout(httpParams, 90000);
        this.httpClient.setParams(httpParams);
    }

    public HttpResponse httpResponse(String url, JSONObject data, Method method) throws IOException {
        if (StringUtils.isBlank(url)) return null;
     /* TODO   if (logger.isDebugEnabled())
            logger.debug("Requesting : " + url.substring(0,url.indexOf("?")+14));
     */   HttpResponse response = null;
        HttpRequestBase request = null;

        try {
            if (method == Method.GET) {
                request = new HttpGet(url);
            } else if (method == Method.POST) {
                request = new HttpPost(url);
                if (data != null) {
                    ((HttpPost) request).setEntity(new StringEntity(data.toString()));
                }
            } else if (method == Method.PUT) {
                request = new HttpPut(url);
                if (data != null) {
                    ((HttpPut) request).setEntity(new StringEntity(data.toString()));
                }
            }
            else {
                throw new RuntimeException("Unsupported method: " + method.toString());
            }
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json; charset=UTF-8");

            response = this.httpClient.execute(request);


            if (response == null || response.getStatusLine() == null) {
            /*TODO    if(logger.isDebugEnabled())
                    logger.debug("Erroneous response (Probably null) for url: \n", url);
            */    response = null;
            }
        } catch (Exception e) {
        /*TODO    if(logger.isDebugEnabled())
                logger.debug("Problems with creating and sending request: \n", e);
        */}
        return response;
    }


    public <T> T response(String url, JSONObject data, Method method, Class<T> returnType){
        T returnObj=null;
        JSONObject jo = null;
        String output = null;
        HttpResponse response = null;
        try {
            response = httpResponse(url, data, method);
            if (response != null) {
                output = EntityUtils.toString(response.getEntity());
              /*TODO  if (logger.isDebugEnabled())
                    logger.debug("Received object from server: " + output);
              */  if (output.isEmpty()) {
                    throw new IOException();
                }
                jo = new JSONObject(output);
            }
        } catch (IOException ioe) {
        /*TODO    if (logger.isDebugEnabled())
                logger.debug("Received empty response from server: ",ioe);
        */    return null;
        } catch (JSONException e) {
        /*TODO    if (logger.isDebugEnabled())
                logger.debug("ERROR decoding Json: ", e);
        */    returnType= (Class<T>) String.class;
            return returnType.cast(output);
        }

        try{
            returnObj=returnType.cast(jo);

        }catch (ClassCastException cce){
        /*TODO    if (logger.isDebugEnabled())
                logger.debug("Failed to parse response from server: ", cce);
        */    throw new RuntimeException(jo.toString());

        }
        return returnObj;
    }
}