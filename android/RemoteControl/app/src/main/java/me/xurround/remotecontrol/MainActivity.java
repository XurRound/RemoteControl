package me.xurround.remotecontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.remotefairy.model.CodeProcessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import me.xurround.remotecontrol.fragments.ListFragment;
import me.xurround.remotecontrol.utils.NetworkUtils;
import me.xurround.remotecontrol.utils.RequestBuilder;
import me.xurround.remotecontrol.utils.RequestResultListener;

public class MainActivity extends AppCompatActivity
{
    private static String API_URL = "https://api.appnimator.com/staging/ircodes/";

    private ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Settings.loadSettings(getApplicationContext());

        selectCategory();
    }

    private void selectCategory()
    {
        NetworkUtils.performGetRequest(API_URL + "brands.php", RequestBuilder.EMPTY_REQUEST, new RequestResultListener()
        {
            @Override
            public void onSuccess(String response)
            {
                runOnUiThread(() ->
                {
                    HashMap<String, String[]> devices = new HashMap<>();
                    try
                    {
                        JSONObject jsonObject = new JSONObject(response);
                        Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext())
                        {
                            String key = keys.next();
                            JSONArray brands = jsonObject.getJSONArray(key);
                            String[] names = new String[brands.length()];
                            for (int i = 0; i < brands.length(); i++)
                                names[i] = brands.getString(i);
                            devices.put(key, names);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    ArrayList<String> categories = new ArrayList<>(devices.keySet());
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, categories);
                    listFragment = new ListFragment(categoryAdapter, (parent, view, position, id) ->
                    {
                        String selectedCategory = categories.get(position);
                        String[] brands = devices.get(selectedCategory);
                        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, brands);
                        listFragment = new ListFragment(brandAdapter, (parent1, view1, position1, id1) -> getModels(selectedCategory, brands[position1]));
                        setFragment(listFragment);
                    });
                    setFragment(listFragment);
                });
            }

            @Override
            public void onFail()
            {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void getModels(String selectedCategory, String selectedBrand)
    {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.add("model", "allModels");
        requestBuilder.add("brand", selectedBrand);
        requestBuilder.add("type", selectedCategory);
        requestBuilder.add("version", "380");

        NetworkUtils.performGetRequest(API_URL + "models_gen.php", requestBuilder.build(), new RequestResultListener()
        {
            @Override
            public void onSuccess(String response)
            {
                runOnUiThread(() ->
                {
                    try
                    {
                        JSONArray dataArray = new JSONArray(response);
                        ArrayList<String> models = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++)
                            models.add(dataArray.getJSONObject(i).getString("model"));
                        ArrayAdapter<String> modelsAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, models);
                        listFragment = new ListFragment(modelsAdapter, (parent2, view2, position2, id2) ->
                        {
                            try
                            {
                                JSONObject data = dataArray.getJSONObject(position2);
                                String identifier = data.getString("id");
                                getCodes(identifier);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
                        setFragment(listFragment);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFail()
            {
                runOnUiThread(() ->
                {
                    Toast.makeText(getApplicationContext(), ":(", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void getCodes(String identifier)
    {
        RequestBuilder finalReqBuilder = new RequestBuilder();
        finalReqBuilder.add("id", identifier);
        finalReqBuilder.add("layout_type", "phone-vertical");
        finalReqBuilder.add("grid_size", "24");

        NetworkUtils.performGetRequest(API_URL + "v2/codes.php", finalReqBuilder.build(), new RequestResultListener()
        {
            @Override
            public void onSuccess(String response)
            {
                try
                {
                    JSONArray cmdsData = new JSONObject(response).getJSONArray("items");
                    ArrayList<String> cmds = new ArrayList<>();
                    for (int i = 0; i < cmdsData.length(); i++)
                        cmds.add(cmdsData.getJSONObject(i).getString("function"));
                    ArrayAdapter<String> finalAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, cmds);
                    listFragment = new ListFragment(finalAdapter, (parent3, view3, position3, id3) ->
                    {
                        try
                        {
                            JSONObject item = cmdsData.getJSONObject(position3);
                            String b64code = item.getString("code1");
                            String encKey = item.getString("encryption_key");
                            String decoded = CodeProcessor.process(b64code, encKey);
                            String[] clearDec = decoded.replace(" ", "").split(",");
                            int[] code = new int[clearDec.length];
                            for (int i = 0; i < clearDec.length; i++)
                                code[i] = Integer.parseInt(clearDec[i]);
                            code = convertCyclesToIntervals(code);
                            StringBuilder builder = new StringBuilder();
                            for (int part : code)
                                builder.append(part).append("|");
                            sendControlX(builder.toString(), clearDec[0]);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    });
                    setFragment(listFragment);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail()
            {
                Toast.makeText(getApplicationContext(), ":(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendControlX(String code, String freq)
    {
        RequestBuilder irBuilder = new RequestBuilder();
        irBuilder.add("code", code);
        irBuilder.add("freq", freq);

        NetworkUtils.performPostRequest("http://" + Settings.IP_ADDRESS + "/raw", irBuilder.build(), new RequestResultListener()
        {
            @Override
            public void onSuccess(String response)
            {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onFail()
            {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), ":(", Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.setIpAddress)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Set device IP-address");
            builder.setMessage("Type address below:");
            EditText input = new EditText(getApplicationContext());
            input.setTextColor(Color.BLACK);
            input.setText(Settings.IP_ADDRESS);
            builder.setView(input);
            builder.setPositiveButton("Set", (dialog, which) ->
            {
                Settings.saveSettings(getApplicationContext(), input.getText().toString());
                Toast.makeText(getApplicationContext(), "Successfully set", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }
        return true;
    }

    private int[] convertCyclesToIntervals(int[] cycles)
    {
        int[] intervals = new int[cycles.length - 1];
        int k = 1000000 / cycles[0];
        for (int i = 1; i < intervals.length; i++)
            intervals[i - 1] = cycles[i] * k;
        return intervals;
    }

    private void setFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}