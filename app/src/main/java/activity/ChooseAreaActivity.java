package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;

import java.util.ArrayList;
import java.util.List;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

/**
 * Created by LXF on 2016/11/9.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=0;
    public static final int LEVEL_COUNTY=0;
    private ProgressDialog mProgressDialog;
    private TextView titleText;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private CoolWeatherDB mCoolWeatherDB;
    private List<String>dataList=new ArrayList<String>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;//城市列表
    private List<County> countyList;//县列表

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        mListView= (ListView) findViewById(R.id.list_view);
        titleText= (TextView) findViewById(R.id.title_text);
        mAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        mCoolWeatherDB=CoolWeatherDB.getInstance(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                if (currentLevel==LEVEL_PROVINCE)
                {
                    selectedProvince=provinceList.get(index);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(index);
                    queryCounties();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到，再到服务器查询
     */
    private void queryProvinces() {

        provinceList=mCoolWeatherDB.loadProvince();
        if (provinceList.size()>0)
        {
            dataList.clear();
            for (Province province:provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询选中省的城市，优先从数据库查询，如果没有查询到，再到服务器查询
     */
    private void queryCities() {

        cityList=mCoolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size()>0)
        {
            dataList.clear();
            for (City city:cityList)
            {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    /**
     * 查询选中城市的县，优先从数据库查询，如果没有查询到，再到服务器查询
     */
    private void queryCounties() {

        countyList=mCoolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size()>0)
        {
            dataList.clear();
            for (County county:countyList)
            {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else {
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     */
    private void queryFromServer(final String code, final String type) {

        String address;
        if (!TextUtils.isEmpty(code))
        {
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address="http://www.weather.com.cn/data/list3/city.xml";

        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if ("province".equals(type))
                {
                    result= Utility.handleProvinceResponse(mCoolWeatherDB,response);
                }else if ("city".equals(type))
                {
                    result=Utility.handleCityResponse(mCoolWeatherDB,
                            response,selectedProvince.getId());
                }else if ("county".equals(type))
                {
                    result=Utility.handleCountyResponse(mCoolWeatherDB,
                            response,selectedCity.getId());
                }
                if (result)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败"
                                ,Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void closeProgressDialog() {
        if (mProgressDialog!=null)
        {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog==null)
        {
            mProgressDialog=new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载·····");
            mProgressDialog.setCanceledOnTouchOutside(false);

        }
        mProgressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_COUNTY)
        {
            queryCities();
        }else if (currentLevel==LEVEL_CITY)
        {
            queryProvinces();
        }else{
            finish();
        }
    }
}
