package com.pigeon.driver;

import android.content.Context;

/**
 * Created by Chalitta Khampachua on 21-Jul-17.
 */

public class Config {

    private static Config mInstance = null;

    protected Config(){

    }

    public static synchronized Config getInstance(){
        if(null == mInstance){
            mInstance = new Config();
        }
        return mInstance;
    }

    public float getDisplayDensity(Context context){
        float density = context.getResources().getDisplayMetrics().density;
        //Log.d(TAG, "size image density : " + density);
        float m = 1;
        if(density == 2.0){   //default => 2 , density/2; //320dpi
            m = 1;
            return m;
        }else if(density ==  1.0){  //160dpi
            m = (float) 0.5;
            return m;
        }
        else if(density ==  0.75){  //120dpi
            m = (float) 0.375;
            return m;
        }
        else if(density ==  1.5){   //240dpi
            m = (float) 0.75;
            return m;
        }
        else if(density ==  3.0){   //480dpi
            m = (float) 1.5;
            return m;
        }
        else if(density ==  4.0){   //640dpi
            m = (float) 2;
            return m;
        }
        return m;
    }

}
