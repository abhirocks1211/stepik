package org.stepic.droid.web;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.User;
import org.stepic.droid.util.SharedPreferenceHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Api implements IApi {
    Context mContext;

    @Inject
    public Api(Context context) {
        mContext = context;
        MainApplication.component(context).inject(this);
    }

    @Inject
    IConfig mConfig;

    @Inject
    IHttpManager mHttpManager;

    @Inject
    SharedPreferenceHelper mSharedPreferencesHelper;

    @Override
    public IStepicResponse authWithLoginPassword(String username, String password) {
        Bundle params = new Bundle();
        params.putString("grant_type", mConfig.getGrantType());
        params.putString("username", username);
        params.putString("password", password);

        String url = mConfig.getBaseUrl() + "/oauth2/token/";

        String json = null;
        try {
            json = mHttpManager.post(url, params);
        } catch (IOException i) {

            int ignore = 123456789;
            //ignore
            //Too many follow-up requests: 21 when incorrect user/password
        }

        Gson gson = new GsonBuilder().create();

        return gson.fromJson(json, AuthenticationStepicResponse.class);
    }

    @Override
    public IStepicResponse signUp(String firstName, String secondName, String email, String password) {

        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("first_name", firstName);
        innerObject.addProperty("last_name", secondName);
        innerObject.addProperty("email", email);
        innerObject.addProperty("password", password);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("user", innerObject);


        String url = mConfig.getBaseUrl() + "/api/users/";
        //todo implement registration

        String json = null;
        try {
            json = mHttpManager.postJson(url, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int test = 9000;
        return null;
    }

    @Override
    public CoursesStepicResponse getEnrolledCourses(int page) {
        Bundle params = new Bundle();
        params.putString("enrolled", "true");
        return getCourses(params, page);
    }

    @Override
    public CoursesStepicResponse getFeaturedCourses(int page) {
        Bundle params = new Bundle();
        params.putString("is_featured", "true");
        CoursesStepicResponse stepicResponse = getCourses(params, page);
        if (stepicResponse == null || stepicResponse.getCourses() == null) return null;
        List<Course> courses = stepicResponse.getCourses();
        filterActiveAndSoonCourses(courses);
        return stepicResponse;
    }

    private void filterActiveAndSoonCourses(List<Course> courses) {
        //todo: optimize this
        List<Course> filteredCourses = new ArrayList<>();
        DateTime now = DateTime.now();
        try {
            for (Course courseItem : courses) {
                DateTime deadLine = courseItem.getEndDateTime();
                if (deadLine == null || deadLine.isAfter(now))
                    filteredCourses.add(courseItem);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        courses.clear();
        courses.addAll(filteredCourses);
    }

    @Override
    public Profile getUserProfile() {
        updateToken();
        String url = mConfig.getBaseUrl() + "/api/stepics/1";
        Bundle params = new Bundle();

        String json = null;
        try {
            json = mHttpManager.get(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (json == null) return null;
        JsonElement jElement = new JsonParser().parse(json);//bottle neck
        JsonObject jObject = jElement.getAsJsonObject();
        JsonArray jsonArray = jObject.getAsJsonArray("profiles");

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Profile>>() {
        }.getType();

        List<Profile> profiles = gson.fromJson(jsonArray.toString(), listType);
        if (profiles == null || profiles.isEmpty()) return null;
        return profiles.get(0);
    }

    @Override
    public List<User> getUsers(long[] userIds) {
        updateToken();
        String baseUrl = mConfig.getBaseUrl() + "/api/users/";

        List<User> users = new ArrayList<>();

        for (long userId : userIds) {
            String json = null;
            String url = baseUrl + userId;
            try {
                json = mHttpManager.get(url, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (json == null) continue;

            JsonElement jElement = new JsonParser().parse(json);//bottle neck
            JsonObject jObject = jElement.getAsJsonObject();
            JsonArray jsonArray = jObject.getAsJsonArray("users");
            Type listType = new TypeToken<List<User>>() {
            }.getType();

            Gson gson = new Gson();
            List<User> oneUserAtList = gson.fromJson(jsonArray.toString(), listType);

            for (User userItem : oneUserAtList) {
                users.add(userItem);
            }
        }

        return users;
    }


    private CoursesStepicResponse getCourses(Bundle params, int page) {
        updateToken();

        String url = mConfig.getBaseUrl() + "/api/courses/";

        params.putInt("page", page);

        String json = null;
        try {
            json = mHttpManager.get(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (json == null) return null;
        JsonElement jElement = new JsonParser().parse(json);//bottle neck
        JsonObject jObject = jElement.getAsJsonObject();
        JsonArray jsonArray = jObject.getAsJsonArray("courses");
        JsonObject metaObject = jObject.getAsJsonObject("meta");


        Gson gson = new Gson();
        Type listType = new TypeToken<List<Course>>() {
        }.getType();

        List<Course> courseList = gson.fromJson(jsonArray.toString(), listType);
        Meta meta = gson.fromJson(metaObject, Meta.class);

        return new CoursesStepicResponse(courseList, meta);
    }


    private void updateToken() {
        AuthenticationStepicResponse response = mSharedPreferencesHelper.getAuthResponseFromStore();
        Bundle params = new Bundle();
        params.putString("grant_type", mConfig.getRefreshGrantType());
        params.putString("refresh_token", response.getRefresh_token());


        String url = mConfig.getBaseUrl() + "/oauth2/token/";

        String json = null;
        try {
            json = mHttpManager.post(url, params);
        } catch (IOException i) {

            int ignore = 123456789;
            //ignore
            //Too many follow-up requests: 21 when incorrect user/password
        }

        if (json != null) {
            Gson gson = new GsonBuilder().create();
            AuthenticationStepicResponse newResp = gson.fromJson(json, AuthenticationStepicResponse.class);
            mSharedPreferencesHelper.storeAuthInfo(newResp);
        }

    }

}