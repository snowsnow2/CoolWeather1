package util;

import android.text.TextUtils;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;

/**
 * Created by LXF on 2016/11/9.
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean 
    handleProvinceResponse(CoolWeatherDB coolWeatherDB,
                           String response)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allProvinces=response.split(",");
            if (allProvinces!=null&&allProvinces.length>0)
            {
                for(String p:allProvinces)
                {
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceName(array[0]);
                    province.setProvinceCode(array[1]);
                    //将解析出来的数据存到数据库对应的表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的城市级数据
     */
    public synchronized static boolean
    handleCityResponse(CoolWeatherDB coolWeatherDB,
                           String response,int provinceId)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allCities=response.split(",");
            if (allCities!=null&&allCities.length>0)
            {
                for(String p:allCities)
                {
                    String[] array=p.split("\\|");
                    City city=new City();
                    city.setCityName(array[0]);
                    city.setCityCode(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存到数据库对应的表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public synchronized static boolean
    handleCountyResponse(CoolWeatherDB coolWeatherDB,
                       String response,int cityId)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allCounties=response.split(",");
            if (allCounties!=null&&allCounties.length>0)
            {
                for(String p:allCounties)
                {
                    String[] array=p.split("\\|");
                    County county=new County();
                    county.setCountyName(array[0]);
                    county.setCountyCode(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据存到数据库对应的表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

}
