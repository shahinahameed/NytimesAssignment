package com.example.shahina.nytimesassignment;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Download downloadDataAsync;
    ListView listView ;
    CustomListAdapter custadapter;
    ArrayList<HashMap<String, String>> newsList=new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //calling next activity and sending data to that
                Intent intent = new Intent(getApplicationContext(),DetailsActivity.class);
                intent.putExtra("details", newsList.get(position));
                startActivity(intent);
            }
        });
        //async task to fetch data through API
        downloadDataAsync = new Download(this);
        downloadDataAsync.execute();
    }

    public class Download extends AsyncTask<String, Integer, String> {

        public MainActivity activity;

        public Download(MainActivity mainActivity) {
            this.activity = mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show something like splash screen and meantime load data through API
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                //data download
                URL url = new URL("http://api.nytimes.com/svc/mostpopular/v2/mostviewed/all-sections/7.json?api-key=nGAxCEJJ6Gh9Kd1QlAwSzaB4fD0oAF1W");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                System.out.println("Exception in doinbackground"+e.toString());
                return e.toString();
            }

        }
        @Override
        protected void onPostExecute(String result) {

            try {
                //process data and store in hashmap
                final JSONObject jo = new JSONObject(result);
                String status=jo.getString("status");

                if(status.equalsIgnoreCase("ok")){

                    HashMap<String, String> newsData;

                    final JSONArray ja = new JSONArray(jo.getString("results"));

                    String[] maintitle=new String[ja.length()];

                    for(int i=0;i<ja.length();i++){
                        newsData=new HashMap<String, String>();

                        JSONArray media=new JSONArray(ja.getJSONObject(i).getString("media"));
                        JSONArray mediadata=new JSONArray(media.getJSONObject(0).getString("media-metadata"));

                        newsData.put("type",ja.getJSONObject(i).getString("type"));
                        newsData.put("source_name",ja.getJSONObject(i).getString("source"));
                        newsData.put("author",ja.getJSONObject(i).getString("byline"));
                        newsData.put("title",ja.getJSONObject(i).getString("title"));
                        newsData.put("description",ja.getJSONObject(i).getString("abstract"));
                        newsData.put("url",ja.getJSONObject(i).getString("url"));
                        newsData.put("urlToImage",mediadata.getJSONObject(0).getString("url"));
                        newsData.put("publishedAt",ja.getJSONObject(i).getString("published_date"));

                        newsList.add(newsData);
                        maintitle[i]=ja.getJSONObject(i).getString("title");

                    }
                    custadapter=new CustomListAdapter(activity,maintitle,newsList);
                    listView.setAdapter(custadapter);

                }else{
                    //Inform user about error
                    Toast.makeText(getApplicationContext(), "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}